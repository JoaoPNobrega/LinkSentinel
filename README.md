# Projeto Link Sentinel



Este repositório contém o sistema "Link Sentinel", uma aplicação web desenvolvida para permitir que usuários verifiquem e monitorem a disponibilidade de links na internet, registrando seus históricos e alertando sobre indisponibilidades.



---



## 📖 Domínio - Aplicativo de Monitoramento e Verificação de Links



O sistema "Link Sentinel" fornece uma plataforma para **Usuários** gerenciarem e acompanharem a acessibilidade de **URLs** na web. Ele centraliza o processo de **Verificação de Links**, mantém um **Histórico de Verificações** detalhado e oferece um **Monitoramento de Links** contínuo, notificando sobre status críticos.



---



## 🗣️ Linguagem Ubíqua



Para garantir uma comunicação clara e consistente em todo o projeto, definimos os seguintes termos e conceitos essenciais:



* **Usuário:** Representa a pessoa que utiliza o sistema para gerenciar e interagir com seus **Links**. Possui credenciais de acesso para **Autenticação** e **Registro**.

* **Link:** A URL específica que um **Usuário** adiciona ao sistema para ser verificada ou monitorada. Um **Link** é associado a um **Usuário** e pode ter múltiplos **Resultados de Verificação**.

* **Resultado de Verificação:** Um registro individual de uma **Verificação de Link**, capturando o status HTTP (sucesso/falha), o tempo de resposta e, se aplicável, a razão da falha. Cada **Resultado de Verificação** está diretamente ligado a um **Link**.

* **Verificação de Link:** A ação de realizar uma requisição HTTP para uma **URL** de um **Link** a fim de obter seu **Resultado de Verificação** atual. Este processo pode ser influenciado por diferentes **Estratégias de Verificação**.

* **Monitoramento de Link:** O processo automatizado e periódico de executar **Verificações de Link** para **Links** que o **Usuário** configurou para acompanhamento contínuo. Ele rastreia o estado do **Link** e pode gerar **Alertas Críticos Offline**.

* **Histórico de Verificações:** A funcionalidade que permite ao **Usuário** consultar e visualizar a série de **Resultados de Verificação** de um **Link** ao longo do tempo, incluindo o cálculo de **Uptime**.

* **Uptime:** Uma métrica expressa em porcentagem que indica a disponibilidade de um **Link**, calculada com base nos **Resultados de Verificação** registrados em um período.

* **Estratégia de Verificação:** Um algoritmo ou abordagem específica para realizar uma **Verificação de Link**. Exemplos incluem a `BasicHttpStrategy` (verificação simples) e a `RedirectCheckStrategy` (que segue redirecionamentos HTTP).

* **Alerta Crítico Offline:** Uma condição acionada pelo **Monitoramento de Link** quando um **Link** falha em um número consecutivo de **Verificações**, indicando uma indisponibilidade persistente que requer atenção.

* **Registro de Usuário:** O processo de criação de uma nova conta de **Usuário** no sistema, envolvendo validações de dados e armazenamento seguro da senha.

* **Autenticação de Usuário (Login):** O processo pelo qual um **Usuário** existente prova sua identidade para obter acesso às funcionalidades do sistema.



---



## 🚀 Documentação Externa



Este projeto conta com documentação complementar para detalhar seus requisitos, design e fluxo de trabalho.



* **Mapa de Histórias do Usuário e Personas:** [Mapa e Persona no FIGMA](https://www.figma.com/design/Orl7CzfHlF94jRdjmoyc6h/Multiple-Personas-Template--Community-?node-id=0-1&t=24F7MOtopWJ4K4Vb-1)

* **Protótipos de Baixa Fidelidade:** [Protótipo no FIGMA](https://www.figma.com/design/fJZdsUoj1KpcO9ZPP96tDq/Untitled?node-id=0-1&t=jrGv1wF5LAGg9TXs-1)

* **Modelo(s) do(s) Subdomínio(s) com Context Mapper (CML):** [docs/ddd/context-mapper](https://github.com/JoaoPNobrega/LinkSentinela/tree/main/docs/ddd/context-mapper)



---



## 👥 Grupo



* João Pedro Araújo Nóbrega

* Tiago emilio rodrigues de Abreu
