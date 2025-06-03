# language: pt

Funcionalidade: Registro de Usuário

  Como um usuário não registrado
  Eu quero me registrar no sistema
  Para que eu possa acessar as funcionalidades

  Cenário: Registro bem-sucedido com dados válidos
    Given que eu não estou registrado no sistema
    When eu preencho o formulário de registro com nome de usuário "novo_usuario", email "novo_usuario@email.com" e senha "SenhaSegura123"
    And eu submeto o formulário
    Then eu devo ser redirecionado para a página de login
    And uma mensagem de sucesso deve ser exibida "Registro realizado com sucesso. Faça login."

  Cenário: Registro falha com nome de usuário já existente
    Given que um usuário com o nome de usuário "usuario_existente" já está registrado
    When eu tento me registrar com o nome de usuário "usuario_existente", email "emaildiferente@email.com" e senha "NovaSenha123"
    Then o registro deve falhar
    And uma mensagem de erro deve ser exibida "Usuário já existe"

  Cenário: Registro falha com email já existente
    Given que um usuário com o email "email_existente@email.com" já está registrado
    When eu tento me registrar com o nome de usuário "outro_usuario", email "email_existente@email.com" e senha "SenhaUnica456"
    Then o registro deve falhar
    And uma mensagem de erro deve ser exibida "Email já existe"

  Cenário: Registro falha com senha muito curta
    Given que eu não estou registrado no sistema
    When eu preencho o formulário de registro com nome de usuário "usuario_curto", email "curto@email.com" e senha "123"
    And eu submeto o formulário
    Then o registro deve falhar
    And uma mensagem de erro deve ser exibida "Senha inválida."
    # O seu código não valida explicitamente "senha muito curta", mas "Senha inválida." seria uma mensagem genérica para falha de validação de senha.
    # Se você tiver uma validação mais específica para senha, ajuste a mensagem.