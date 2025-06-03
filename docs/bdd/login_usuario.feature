# language: pt

Funcionalidade: Login de Usuário

  Como um usuário registrado
  Eu quero fazer login no sistema
  Para que eu possa acessar as funcionalidades

  Cenário: Login bem-sucedido com credenciais válidas
    Given que eu sou um usuário registrado com nome de usuário "usuario_existente" e senha "SenhaSegura123"
    When eu preencho o formulário de login com nome de usuário "usuario_existente" e senha "SenhaSegura123"
    And eu submeto o formulário
    Then eu devo ser redirecionado para a página principal (/main)
    And uma mensagem de boas-vindas deve ser exibida "Bem-vindo, usuario_existente!"

  Cenário: Login falha com senha incorreta
    Given que eu sou um usuário registrado com nome de usuário "usuario_existente" e senha "SenhaCorreta123"
    When eu preencho o formulário de login com nome de usuário "usuario_existente" e senha "SenhaIncorreta"
    And eu submeto o formulário
    Then o login deve falhar
    And uma mensagem de erro deve ser exibida "Credenciais inválidas"

  Cenário: Login falha com usuário inexistente
    Given que não existe um usuário com o nome de usuário "usuario_inexistente"
    When eu preencho o formulário de login com nome de usuário "usuario_inexistente" e senha "QualquerSenha"
    And eu submeto o formulário
    Then o login deve falhar
    And uma mensagem de erro deve ser exibida "Credenciais inválidas"