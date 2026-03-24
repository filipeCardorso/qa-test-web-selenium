# QA Test - Web Automation (Selenium + Java)

Automacao de testes end-to-end para a funcionalidade de pesquisa do [Blog do Agi](https://blogdoagi.com.br/), desenvolvida como parte de um teste tecnico para QA.

## Sumario

- [Sobre o Projeto](#sobre-o-projeto)
- [Analise da Aplicacao](#analise-da-aplicacao)
- [Cenarios de Teste](#cenarios-de-teste)
- [Arquitetura e Design Patterns](#arquitetura-e-design-patterns)
- [Stack Tecnologica](#stack-tecnologica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pre-requisitos](#pre-requisitos)
- [Configuracao e Execucao](#configuracao-e-execucao)
- [CI/CD](#cicd)
- [Relatorios](#relatorios)
- [Decisoes Tecnicas](#decisoes-tecnicas)

---

## Sobre o Projeto

Este projeto automatiza os cenarios mais relevantes da funcionalidade de pesquisa do Blog do Agi. A escolha da pesquisa como foco se deve ao fato de ser a principal ferramenta de navegacao orientada a conteudo do blog, impactando diretamente a experiencia do usuario.

A automacao valida que a pesquisa retorna resultados corretos, trata adequadamente cenarios negativos (termos inexistentes, caracteres especiais) e que os resultados exibidos sao coerentes com o termo buscado.

## Analise da Aplicacao

O Blog do Agi e um site WordPress com o tema **Astra**. A funcionalidade de pesquisa segue o padrao WordPress:

- **URL original:** `https://blogdoagi.com.br/` (redireciona 301 para `https://blog.agibank.com.br/`)
- **Mecanismo:** Icone de lupa (slide-search) no header que expande um campo de busca via CSS transition
- **Comportamento:** Submissao do formulario redireciona para `?s={termo}` com resultados paginados
- **Tema Astra:** Utiliza a classe `ast-dropdown-active` para controlar a visibilidade do campo de pesquisa

### Seletores Identificados

| Elemento | Seletor CSS | Observacao |
|----------|------------|------------|
| Icone de busca | `.astra-search-icon` | Link clicavel que ativa o dropdown |
| Container | `.ast-search-menu-icon` | Div que recebe a classe `ast-dropdown-active` |
| Campo de busca | `.search-field` | Input hidden por padrao, visivel apos ativacao |
| Artigos de resultado | `article` | Padrao WordPress para cada resultado |
| Titulo do resultado | `.entry-title a` | Link dentro do heading do artigo |
| Sem resultados | `.no-results` | Container exibido quando nao ha resultados |

### Desafio Tecnico: Headless Mode

Em modo headless, o click nativo do Selenium no icone de busca nao ativa a transicao CSS do tema Astra. A solucao adotada foi ativar o dropdown via **JavaScript**, manipulando diretamente as classes CSS do container (`ast-dropdown-active`) e a visibilidade do campo. Esta abordagem e robusta tanto para execucao local quanto em CI.

## Cenarios de Teste

### CT-001: Pesquisa com termo valido retorna resultados
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Pre-condicao** | Pagina inicial carregada |
| **Passos** | 1. Acessar a pagina inicial 2. Abrir o campo de busca 3. Pesquisar por "automacao" |
| **Resultado esperado** | Pelo menos 1 artigo e retornado na pagina de resultados |
| **Por que e relevante** | Valida o fluxo principal (happy path) da pesquisa — se este cenario falha, a funcionalidade esta quebrada |

### CT-002: Pesquisa com termo inexistente exibe mensagem
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Pre-condicao** | Pagina inicial carregada |
| **Passos** | 1. Acessar a pagina inicial 2. Pesquisar por "xyznonexistent999" |
| **Resultado esperado** | Pagina exibe a secao `.no-results` e contagem de resultados e zero |
| **Por que e relevante** | Valida o tratamento de cenario negativo — o usuario precisa receber feedback claro quando nao ha resultados |

### CT-003: Pesquisa com caracteres especiais nao quebra
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Normal |
| **Pre-condicao** | Pagina inicial carregada |
| **Passos** | 1. Acessar a pagina inicial 2. Pesquisar por "@#$%&" |
| **Resultado esperado** | A pagina de resultados carrega sem erros (exibe resultados ou mensagem de nenhum resultado) |
| **Por que e relevante** | Garante que a aplicacao trata inputs inesperados sem quebrar — cenario comum de seguranca e robustez |

### CT-004: Resultado contem titulo e link coerentes com o termo
| Campo | Descricao |
|-------|-----------|
| **Severidade** | Critical |
| **Pre-condicao** | Pagina inicial carregada |
| **Passos** | 1. Acessar a pagina inicial 2. Pesquisar por "credito" 3. Verificar o primeiro resultado |
| **Resultado esperado** | O titulo do primeiro resultado contem o termo buscado (case-insensitive) e o link nao esta vazio |
| **Por que e relevante** | Valida a relevancia dos resultados — nao basta retornar artigos, eles precisam ser pertinentes ao termo |

## Arquitetura e Design Patterns

### Page Object Model (POM)

Cada pagina da aplicacao e representada por uma classe Java que encapsula os seletores e acoes disponiveis naquela pagina. Isso traz:

- **Manutencao simplificada:** se um seletor muda, so precisa ser atualizado em um unico lugar
- **Legibilidade dos testes:** os testes leem como especificacoes de negocio, nao como scripts de automacao
- **Reuso:** acoes comuns (navegar, pesquisar) sao metodos reutilizaveis

```
BasePage (abstrata)
  ├── HomePage        → navegacao e acao de pesquisa
  └── SearchResultPage → verificacoes de resultados
```

### Factory Pattern — DriverFactory

A criacao do WebDriver e encapsulada numa Factory que suporta **Chrome**, **Firefox** e **Edge**. O browser e configurado via `application.yml`, sem alterar codigo de teste. Isso implementa o **Strategy Pattern** implicitamente — trocar de browser e apenas mudar uma configuracao.

### Singleton — DriverManager e ConfigManager

- **DriverManager** usa `ThreadLocal<WebDriver>` para garantir que cada thread de teste tem sua propria instancia do browser — essencial para execucao paralela
- **ConfigManager** carrega `application.yml` uma unica vez e disponibiliza as configuracoes globalmente

### Padrao AAA (Arrange-Act-Assert)

Todos os testes seguem a estrutura:

```java
@Test
void shouldReturnResultsForValidSearch() {
    // Arrange — preparar o estado inicial
    homePage.navigate();

    // Act — executar a acao sendo testada
    SearchResultPage results = homePage.searchFor("automacao");

    // Assert — verificar o resultado
    assertThat(results.getResultCount()).isGreaterThan(0);
}
```

**Por que AAA:** garante que cada teste tem uma unica responsabilidade clara, facilita a leitura e a identificacao de onde uma falha ocorre.

### Explicit Waits

Todas as interacoes usam `WebDriverWait` com `ExpectedConditions` em vez de `Thread.sleep()`. Isso garante:

- Testes mais rapidos (nao espera tempo fixo, so ate o elemento estar pronto)
- Testes mais estaveis (nao falha por timing em ambientes lentos)
- Timeouts configuraveis via `application.yml`

## Stack Tecnologica

| Tecnologia | Versao | Justificativa |
|-----------|--------|---------------|
| Java | 17 | LTS atual, suporte a switch expressions e records |
| Selenium WebDriver | 4.27.0 | Padrao de mercado para automacao web, compativel com W3C WebDriver |
| JUnit 5 | 5.11.4 | Framework de testes moderno com suporte a `@DisplayName`, `@Nested`, testes parametrizados |
| Gradle | 8.12 | Build tool mais conciso e rapido que Maven, DSL Groovy legivel |
| Allure Report | 2.29.1 | Relatorios visuais com severidade, steps, screenshots — padrao enterprise |
| AssertJ | 3.27.3 | Assertions fluentes e legiveis (`assertThat().isGreaterThan()`) |
| WebDriverManager | 5.9.2 | Gerenciamento automatico de drivers (ChromeDriver, GeckoDriver) |

## Estrutura do Projeto

```
qa-test-web-selenium/
├── .github/workflows/test.yml          # Pipeline CI/CD
├── build.gradle                        # Dependencias e configuracao
├── src/
│   ├── main/java/com/qatest/web/
│   │   ├── config/
│   │   │   └── ConfigManager.java      # Singleton — carrega application.yml
│   │   ├── driver/
│   │   │   ├── DriverFactory.java      # Factory — cria WebDriver por browser
│   │   │   └── DriverManager.java      # Singleton + ThreadLocal — gerencia instancias
│   │   ├── pages/
│   │   │   ├── BasePage.java           # Classe base — waits e acoes comuns
│   │   │   ├── HomePage.java           # Page Object — pagina inicial + pesquisa
│   │   │   └── SearchResultPage.java   # Page Object — resultados da pesquisa
│   │   └── utils/
│   │       └── WaitUtils.java          # Explicit waits centralizados
│   ├── main/resources/
│   │   └── application.yml             # Configuracoes (URL, browser, timeouts)
│   └── test/java/com/qatest/web/
│       ├── base/
│       │   └── BaseTest.java           # Setup/teardown — cria e destroi driver
│       └── tests/
│           └── SearchTest.java         # 4 cenarios de teste
└── README.md
```

## Pre-requisitos

- **Java 17+** — [Download](https://adoptium.net/)
- **Google Chrome** — [Download](https://www.google.com/chrome/)
- **Git** — [Download](https://git-scm.com/)

> O ChromeDriver e gerenciado automaticamente pelo WebDriverManager — nao precisa instalar manualmente.

## Configuracao e Execucao

```bash
# 1. Clonar o repositorio
git clone https://github.com/filipeCardorso/qa-test-web-selenium.git
cd qa-test-web-selenium

# 2. Executar testes (modo visual — abre o browser)
./gradlew test

# 3. Executar testes (headless — sem abrir browser)
CI=true ./gradlew test

# 4. Gerar relatorio Allure
./gradlew allureReport

# 5. Abrir relatorio no browser
./gradlew allureServe
```

### Configuracao do Browser

Editar `src/main/resources/application.yml`:

```yaml
web:
  base-url: https://blog.agibank.com.br/
  browser: chrome        # chrome | firefox | edge
  headless: false        # true para rodar sem interface grafica
  timeout:
    implicit: 10         # segundos
    explicit: 15         # segundos
    page-load: 30        # segundos
```

## CI/CD

O projeto possui pipeline **GitHub Actions** que executa automaticamente a cada push ou pull request:

1. **Setup:** Configura JDK 17, Chrome stable e Gradle
2. **Testes:** Executa todos os cenarios em modo headless
3. **Relatorio de Resultados:** Publica graficos com contagem de testes (passed/failed/skipped) diretamente no Summary da pipeline
4. **Allure Report:** Gera e publica o relatorio em GitHub Pages
5. **Artifacts:** Salva o relatorio Allure para download (retencao de 30 dias)

### Relatorio Online

Apos cada execucao, o Allure Report fica disponivel em:
**https://filipecardorso.github.io/qa-test-web-selenium/allure-report**

## Relatorios

| Tipo | Onde Encontrar | O Que Mostra |
|------|---------------|-------------|
| **JUnit (terminal)** | Output do `./gradlew test` | Resultado de cada teste (PASSED/FAILED) |
| **Pipeline Summary** | GitHub Actions > run > Summary | Graficos com total de testes, taxa de sucesso |
| **Allure Report** | GitHub Pages ou `./gradlew allureServe` | Cenarios detalhados, severidade, duracao, steps, historico |

## Decisoes Tecnicas

| Decisao | Alternativa | Justificativa |
|---------|------------|---------------|
| Gradle sobre Maven | Maven | Menos boilerplate, builds mais rapidos, DSL legivel |
| JUnit 5 sobre TestNG | TestNG | Standard atual do ecossistema Java, melhor integracao com IDEs e CI |
| AssertJ sobre Hamcrest | Hamcrest (built-in JUnit) | API fluente, mensagens de erro mais claras, autocomplete no IDE |
| Explicit Waits sobre Thread.sleep | Thread.sleep | Mais rapido, mais estavel, timeouts configuraveis |
| YAML sobre properties | .properties | Suporta hierarquia, mais legivel para configuracoes aninhadas |
| JS click sobre click nativo | Selenium click | Compatibilidade com headless mode no tema Astra slide-search |
| URL base `blog.agibank.com.br` | `blogdoagi.com.br` | Evita redirect 301 que adiciona latencia desnecessaria |
