# Relatório de Entrega Final - Projeto Link Sentinel

Este documento detalha as funcionalidades implementadas, os padrões de projeto adotados e as instruções para executar o sistema Link Sentinel.

---

## 1. Histórias (Funcionalidades) Implementadas

O projeto Link Sentinel implementou 4 funcionalidades não triviais, atendendo ao escopo mínimo exigido(2), que abrangem o gerenciamento, verificação e monitoramento de links para usuários.

### 1.1. Gerenciamento de Usuários (Registro e Login Seguro)

* **Descrição:** Permite que novos usuários criem uma conta no sistema e que usuários existentes façam login de forma segura para acessar suas funcionalidades. Inclui validações de dados (como unicidade de nome de usuário e e-mail) e tratamento seguro de senhas.
* **Histórias Implementadas:**
    * Como um usuário não registrado, eu quero me registrar no sistema, para que eu possa acessar as funcionalidades.
    * Como um usuário registrado, eu quero fazer login no sistema, para que eu possa acessar as funcionalidades.
* **Arquivos Relevantes:**
    * `User.java` (Modelo de usuário)
    * `UserRepository.java` (Repositório de dados)
    * `UserService.java` (Lógica de negócio de registro)
    * `SecurityService.java` (Lógica de segurança e autenticação)
    * `RegisterView.java` (Interface de registro)
    * `LoginView.java` (Interface de login)
* **Cenários BDD Relacionados:**
    * `docs/ddd/bdd/registro_usuario.feature`
    * `docs/ddd/bdd/login_usuario.feature`

### 1.2. Verificação de Links (Sob Demanda)

* **Descrição:** Permite que um usuário verifique instantaneamente o status e a acessibilidade de qualquer URL, exibindo o status HTTP, tempo de resposta e detalhes de falha. Suporta verificação com ou sem redirecionamentos.
* **Histórias Implementadas:**
    * Como um usuário, eu quero verificar a disponibilidade de um link, para saber se ele está online e funcionando.
    * Como um usuário, eu quero que o sistema me informe o status HTTP e detalhes de falha de um link.
    * Como um usuário, eu quero que o sistema lide com redirecionamentos ao verificar um link.
    * Como um usuário, eu quero que o sistema identifique e alerte sobre links com potencial de risco (phishing).
* **Arquivos Relevantes:**
    * `Link.java` (Modelo de link)
    * `CheckResult.java` (Resultado da verificação)
    * `LinkVerificationService.java` (Serviço de lógica de verificação)
    * `LinkVerifier.java`, `BaseHttpVerifier.java`, `RedirectVerifierDecorator.java` (Padrão Decorator)
    * `VerificationStrategy.java`, `BasicHttpStrategy.java`, `RedirectCheckStrategy.java` (Padrão Strategy)
    * `ThreatChecker.java` (Serviço de análise de ameaças)
    * `LinkCheckerView.java` (Interface de verificação)
* **Cenários BDD Relacionados:**
    * `docs/ddd/bdd/verificacao_link.feature`

### 1.3. Histórico e Visualização de Links

* **Descrição:** Permite que o usuário acesse um histórico detalhado de todas as verificações de seus links, visualize métricas como "Uptime" e gerencie o status de monitoramento (ativar/desativar). Permite também a limpeza do histórico.
* **Histórias Implementadas:**
    * Como um usuário, eu quero visualizar o histórico de um link, para acompanhar a performance.
    * Como um usuário, eu quero ativar/desativar o monitoramento de um link, para controlar quais links são verificados periodicamente.
    * Como um usuário, eu quero ver o percentual de uptime de um link, para ter uma visão rápida da sua disponibilidade.
    * Como um usuário, eu quero limpar o histórico de verificações de um link (ou todos os meus links), para manter meus dados organizados.
* **Arquivos Relevantes:**
    * `HistoryService.java` (Lógica de negócio do histórico)
    * `LinkService.java` (método `toggleLinkMonitoring`)
    * `HistoryView.java` (Interface de histórico)
    * `Link.java` (contém `checkResults` e `monitored`)
    * `CheckResult.java` (Detalhe de cada resultado)
* **Cenários BDD Relacionados:**
    * `docs/ddd/bdd/historico_monitoramento.feature`

### 1.4. Monitoramento Agendado de Links

* **Descrição:** Um processo em segundo plano que verifica periodicamente os links marcados como "monitorados". Ele rastreia falhas consecutivas e muda o status interno do link, notificando sobre alertas críticos de indisponibilidade ou recuperação.
* **Histórias Implementadas:**
    * Como um usuário, eu quero que meus links monitorados sejam verificados automaticamente em intervalos regulares, para ser alertado sobre indisponibilidades.
    * Como um usuário, eu quero ser notificado quando um link monitorado ficar offline por um período crítico.
    * Como um usuário, eu quero que o sistema identifique quando um link monitorado retorna ao status online.
* **Arquivos Relevantes:**
    * `MonitoringService.java` (Serviço principal de monitoramento agendado)
    * `LinkStatusObserver.java` (Interface do padrão Observer)
    * `LoggingObserver.java` (Implementação do Observer para logs)
    * `application.properties` (Configuração do agendamento)
    * `App.java` (`@EnableScheduling`)
* **Cenários BDD Relacionados:**
    * `docs/ddd/bdd/monitoramento_agendado.feature`

---

## 2. Padrões de Projeto Adotados

O projeto Link Sentinel adota e implementa **4 padrões de projeto**, atingindo o número ideal exigido para a entrega.

* **Strategy Pattern:**
    * **Descrição:** Define uma família de algoritmos, encapsula cada um e os torna intercambiáveis. O padrão Strategy permite que o algoritmo varie independentemente dos clientes que o utilizam.
    * **Aplicação no Projeto:** Utilizado na verificação de links para permitir diferentes formas de realizar o HTTP check (ex: com ou sem redirecionamento).
    * **Arquivos:**
        * `VerificationStrategy.java` (Interface Strategy)
        * `BasicHttpStrategy.java` (Estratégia Concreta - verificação básica)
        * `RedirectCheckStrategy.java` (Estratégia Concreta - verificação com redirecionamento)
        * `LinkVerificationService.java` (Contexto que utiliza as estratégias)

* **Decorator Pattern:**
    * **Descrição:** Anexa responsabilidades adicionais a um objeto dinamicamente. Os decoradores fornecem uma alternativa flexível à herança para estender a funcionalidade.
    * **Aplicação no Projeto:** Usado para adicionar a funcionalidade de seguir redirecionamentos à verificação HTTP básica.
    * **Arquivos:**
        * `LinkVerifier.java` (Componente - interface base)
        * `BaseHttpVerifier.java` (Componente Concreto - o verificador original)
        * `AbstractVerifierDecorator.java` (Decorator Abstrato)
        * `RedirectVerifierDecorator.java` (Decorator Concreto - adiciona redirecionamento)
        * Configurado em `AppConfig.java`.

* **Observer Pattern:**
    * **Descrição:** Define uma dependência um-para-muitos entre objetos para que, quando um objeto muda de estado, todos os seus dependentes sejam notificados e atualizados automaticamente.
    * **Aplicação no Projeto:** O `MonitoringService` atua como o "Subject" (assunto) que notifica os "Observers" (observadores) sobre mudanças no status de monitoramento dos links.
    * **Arquivos:**
        * `LinkStatusObserver.java` (Interface Observer)
        * `LoggingObserver.java` (Observer Concreto - registra mudanças de status em log)
        * `MonitoringService.java` (Subject - gerencia observadores e os notifica)

* **Singleton Pattern (Implícito/Via Spring):**
    * **Descrição:** Garante que uma classe tenha apenas uma instância e fornece um ponto de acesso global a ela.
    * **Aplicação no Projeto:** Os serviços do Spring (`@Service`, `@Component`) são, por padrão, singletons no contexto da aplicação. Isso significa que apenas uma instância de `UserService`, `SecurityService`, `LinkVerificationService`, `MonitoringService`, `HistoryService`, etc., é criada e reutilizada em toda a aplicação.

---

## 3. Armazenamento de Dados em Banco Relacional

O sistema Link Sentinel persiste seus dados em um banco de dados relacional.

* **Tecnologia:** Utiliza **JPA (Jakarta Persistence API)** com **Spring Data JPA** para o mapeamento objeto-relacional.
* **Banco de Dados:** Configurado para usar **H2 Database** em modo de arquivo (`jdbc:h2:file:./linksentineldb`), o que permite o armazenamento persistente dos dados de usuários, links e resultados de verificação.
* **Mapeamento Objeto-Relacional (ORM):** As entidades de domínio (`User`, `Link`, `CheckResult`) são anotadas com `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@OneToMany`, `@ManyToOne`, que definem como elas são mapeadas para tabelas no banco de dados. Os `Repositories` (ex: `UserRepository`, `LinkRepository`, `CheckResultRepository`) abstraem as operações de banco de dados, sendo gerenciados pelo Spring Data JPA.

---

## 4. Instruções de Execução do Projeto

Para executar o sistema Link Sentinel localmente, siga os passos abaixo:

### 4.1. Pré-requisitos

* **Java Development Kit (JDK) 17 ou superior**
* **Apache Maven** (instalado e configurado no PATH do sistema)

### 4.2. Como Executar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/JoaoPNobrega/LinkSentinela.git](https://github.com/JoaoPNobrega/LinkSentinela.git)
    cd LinkSentinela
    ```
2.  **Compile e Baixe Dependências:**
    * Abra o terminal na raiz do projeto (onde está o `pom.xml`).
    * Execute o comando Maven para construir o projeto e baixar todas as dependências:
        ```bash
        mvn clean install
        ```
        Aguarde até ver `BUILD SUCCESS`.
3.  **Iniciar a Aplicação Spring Boot:**
    * No mesmo terminal (na raiz do projeto), execute:
        ```bash
        mvn spring-boot:run
        ```
    * Alternativamente, você pode usar sua IDE (VS Code, IntelliJ IDEA, Eclipse) para rodar a classe principal `App.java`.

### 4.3. Acesso à Página Inicial

* Após a aplicação iniciar com sucesso (você verá logs no terminal indicando que o servidor Spring Boot está ativo), abra seu navegador.
* Acesse a URL padrão da aplicação:
    `http://localhost:8080/`
* Você será redirecionado para a `WelcomeView`, de onde poderá navegar para Login ou Registro.
