# language: pt

Funcionalidade: Verificação de Link

  Como um usuário autenticado
  Eu quero verificar a disponibilidade de um link imediatamente
  Para saber se ele está online e funcionando

  Cenário: Link online e acessível (Status 200 OK)
    Given que eu estou logado como "usuario_teste"
    And que eu acesso a página de "Verificar Link"
    When eu preencho o campo URL com "https://www.google.com"
    And eu clico no botão "Verificar"
    Then o sistema deve exibir o status "ONLINE" para "https://www.google.com"
    And o tempo de resposta deve ser menor que 500 milissegundos
    And o resultado da verificação deve ser salvo no histórico do link

  Cenário: Link offline (Status 404 Not Found)
    Given que eu estou logado como "usuario_teste"
    And que eu acesso a página de "Verificar Link"
    When eu preencho o campo URL com "https://www.siteinexistente.com/pagina-nao-encontrada"
    And eu clico no botão "Verificar"
    Then o sistema deve exibir o status "OFFLINE" para "https://www.siteinexistente.com/pagina-nao-encontrada"
    And a razão da falha deve ser "NOT_FOUND" ou "Client Error"
    And o resultado da verificação deve ser salvo no histórico do link

  Cenário: Verificação com redirecionamento (seguindo o redirect)
    Given que eu estou logado como "usuario_teste"
    And que eu acesso a página de "Verificar Link"
    And que o sistema está configurado para seguir redirecionamentos via RedirectCheckStrategy
    When eu preencho o campo URL com "http://site.com.br" que redireciona para "https://www.site.com.br"
    And eu clico no botão "Verificar"
    Then o sistema deve exibir o status "ONLINE" para a URL final "https://www.site.com.br"
    And o resultado da verificação deve ser salvo no histórico do link, referente à URL final

  Cenário: Link com erro de conexão (Timeout ou DNS)
    Given que eu estou logado como "usuario_teste"
    And que eu acesso a página de "Verificar Link"
    When eu preencho o campo URL com "http://dominio-inexistente-xyz.com"
    And eu clico no botão "Verificar"
    Then o sistema deve exibir o status "OFFLINE"
    And a razão da falha deve ser "TIMEOUT" ou "UNKNOWN_HOST"
    And o resultado da verificação deve ser salvo no histórico do link

  Cenário: Análise de risco identifica link como malicioso
    Given que eu estou logado como "usuario_teste"
    And que eu acesso a página de "Verificar Link"
    When eu preencho o campo URL com "http://site.malicioso.exemplo"
    And eu clico no botão "Verificar"
    And o ThreatChecker identifica a URL como uma ameaça
    Then o sistema deve exibir um alerta de segurança
    And o resultado da verificação deve incluir a indicação de risco "MALICIOUS" ou "HIGH_RISK"