# language: pt

Funcionalidade: Monitoramento Agendado de Links

  Como um usuário
  Eu quero que meus links sejam verificados automaticamente em intervalos regulares
  Para ser alertado sobre indisponibilidades sem precisar verificar manualmente

  Cenário: Link monitorado permanece online após verificação agendada
    Given que eu estou logado como "usuario_monitorador"
    And que eu tenho um link "https://www.meusite.com" registrado e configurado como monitorado
    And que o status interno do link está como "OK"
    When o sistema executa a verificação agendada para o link "https://www.meusite.com"
    Then o sistema deve registrar um novo CheckResult com status "ONLINE" para "https://www.meusite.com"
    And o status interno do link "https://www.meusite.com" deve permanecer "OK"

  Cenário: Link monitorado fica offline e atinge alerta crítico
    Given que eu estou logado como "usuario_monitorador"
    And que eu tenho um link "https://www.sitedaempresa.com" registrado e configurado como monitorado
    And que o link "https://www.sitedaempresa.com" já falhou em 2 verificações consecutivas (consecutiveDownCount = 2)
    When o sistema executa a próxima verificação agendada para o link "https://www.sitedaempresa.com"
    And a verificação resulta em "OFFLINE"
    Then o sistema deve registrar um novo CheckResult com status "OFFLINE" para "https://www.sitedaempresa.com"
    And o status interno do link "https://www.sitedaempresa.com" deve ser atualizado para "ALERTA_CRITICO_OFFLINE"
    And o LoggingObserver deve registrar a notificação de alerta crítico

  Cenário: Link offline retorna a ficar online e tem alerta resetado
    Given que eu estou logado como "usuario_monitorador"
    And que eu tenho um link "https://www.sitedaempresa.com" com status interno "ALERTA_CRITICO_OFFLINE"
    When o sistema executa a próxima verificação agendada para o link "https://www.sitedaempresa.com"
    And a verificação resulta em "ONLINE"
    Then o sistema deve registrar um novo CheckResult com status "ONLINE" para "https://www.sitedaempresa.com"
    And o status interno do link "https://www.sitedaempresa.com" deve retornar para "OK"
    And o LoggingObserver deve registrar a notificação de recuperação
    And o consecutiveDownCount do link deve ser resetado para 0

  Cenário: Sistema agendador inicia ao ligar
    Given que o aplicativo Link Sentinel foi inicializado
    When o MonitoringService é ativado pelo Spring context
    Then o agendador de verificações periódicas deve iniciar a cada 60 segundos
    And todos os links marcados como "monitorado" devem ser carregados para o processo de monitoramento