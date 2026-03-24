# QA Test - Web Automation (Selenium + Java)

Automação de testes end-to-end para a funcionalidade de pesquisa do [Blog do Agi](https://blogdoagi.com.br/), desenvolvida como parte de um teste técnico para QA.

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Análise da Aplicação](#análise-da-aplicação)
- [Cenários de Teste](#cenários-de-teste)
- [Arquitetura e Design Patterns](#arquitetura-e-design-patterns)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração e Execução](#configuração-e-execução)
- [CI/CD](#cicd)
- [Relatórios](#relatórios)
- [Decisões Técnicas](#decisões-técnicas)

---

## Sobre o Projeto

Este projeto automatiza os cenários mais relevantes da funcionalidade de pesquisa do Blog do Agi. A escolha da pesquisa como foco se deve ao fato de ser a principal ferramenta de navegação orientada a conteúdo do blog, impactando diretamente a experiência do usuário.

A automação valida que a pesquisa retorna resultados corretos, trata adequadamente cenários negativos (termos inexistentes, caracteres especiais) e que os resultados exibidos são coerentes com o termo buscado.

## Análise da Aplicação

O Blog do Agi é um site WordPress com o tema **Astra**. A funcionalidade de pesquisa segue o padrão WordPress:

- **URL original:** `https://blogdoagi.com.br/` (redireciona 301 para `https://blog.agibank.com.br/`)
- **Mecanismo:** Ícone de lupa (slide-search) no header que expande um campo de busca via CSS transition
- **Comportamento:** Submissão do formulário redireciona para `?s={termo}` com resultados paginados
- **Tema Astra:** Utiliza a classe `ast-dropdown-active` para controlar a visibilidade do campo de pesquisa

### Seletores Identificados

| Elemento | Seletor CSS | Observação |
|----------|------------|------------|
| Ícone de busca | `.astra-search-icon` | Link clicável que ativa o dropdown |
| Container | `.ast-search-menu-icon` | Div que recebe a classe `ast-dropdown-active` |
| Campo de busca | `.search-field` | Input hidden por padrão, visível após ativação |
| Artigos de resultado | `article` | Padrão WordPress para cada resultado |
| Título do resultado | `.entry-title a` | Link dentro do heading do artigo |
| Sem resultados | `.no-results` | Container exibido quando não há resultados |

### Desafio Técnico: Headless Mode

Em modo headless, o click nativo do Selenium no ícone de busca não ativa a transição CSS do tema Astra. A solução adotada foi ativar o dropdown via **JavaScript**, manipulando diretamente as classes CSS do container (`ast-dropdown-active`) e a visibilidade do campo. Esta abordagem é robusta tanto para execução local quanto em CI.

## Cenários de Teste

### CT-001: Pesquisa com termo válido retorna resultados
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Pré-condição** | Página inicial carregada |
| **Passos** | 1. Acessar a página inicial 2. Abrir o campo de busca 3. Pesquisar por "automação" |
| **Resultado esperado** | Pelo menos 1 artigo é retornado na página de resultados |
| **Por que é relevante** | Valida o fluxo principal (happy path) da pesquisa — se este cenário falha, a funcionalidade está quebrada |

### CT-002: Pesquisa com termo inexistente exibe mensagem
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Pré-condição** | Página inicial carregada |
| **Passos** | 1. Acessar a página inicial 2. Pesquisar por "xyznonexistent999" |
| **Resultado esperado** | Página exibe a seção `.no-results` e contagem de resultados é zero |
| **Por que é relevante** | Valida o tratamento de cenário negativo — o usuário precisa receber feedback claro quando não há resultados |

### CT-003: Pesquisa com caracteres especiais não quebra
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Normal |
| **Pré-condição** | Página inicial carregada |
| **Passos** | 1. Acessar a página inicial 2. Pesquisar por "@#$%&" |
| **Resultado esperado** | A página de resultados carrega sem erros (exibe resultados ou mensagem de nenhum resultado) |
| **Por que é relevante** | Garante que a aplicação trata inputs inesperados sem quebrar — cenário comum de segurança e robustez |

### CT-004: Resultado contém título e link coerentes com o termo
| Campo | Descrição |
|-------|-----------|
| **Severidade** | Critical |
| **Pré-condição** | Página inicial carregada |
| **Passos** | 1. Acessar a página inicial 2. Pesquisar por "crédito" 3. Verificar o primeiro resultado |
| **Resultado esperado** | O título do primeiro resultado contém o termo buscado (case-insensitive) e o link não está vazio |
| **Por que é relevante** | Valida a relevância dos resultados — não basta retornar artigos, eles precisam ser pertinentes ao termo |

## Arquitetura e Design Patterns

### Page Object Model (POM)

Cada página da aplicação é representada por uma classe Java que encapsula os seletores e ações disponíveis naquela página. Isso traz:

- **Manutenção simplificada:** se um seletor muda, só precisa ser atualizado em um único lugar
- **Legibilidade dos testes:** os testes leem como especificações de negócio, não como scripts de automação
- **Reuso:** ações comuns (navegar, pesquisar) são métodos reutilizáveis

```
BasePage (abstrata)
  ├── HomePage        → navegação e ação de pesquisa
  └── SearchResultPage → verificações de resultados
```

### Factory Pattern — DriverFactory

A criação do WebDriver é encapsulada numa Factory que suporta **Chrome**, **Firefox** e **Edge**. O browser é configurado via `application.yml`, sem alterar código de teste. Isso implementa o **Strategy Pattern** implicitamente — trocar de browser é apenas mudar uma configuração.

### Singleton — DriverManager e ConfigManager

- **DriverManager** usa `ThreadLocal<WebDriver>` para garantir que cada thread de teste tem sua própria instância do browser — essencial para execução paralela
- **ConfigManager** carrega `application.yml` uma única vez e disponibiliza as configurações globalmente

### Padrão AAA (Arrange-Act-Assert)

Todos os testes seguem a estrutura:

```java
@Test
void shouldReturnResultsForValidSearch() {
    // Arrange — preparar o estado inicial
    homePage.navigate();

    // Act — executar a ação sendo testada
    SearchResultPage results = homePage.searchFor("automação");

    // Assert — verificar o resultado
    assertThat(results.getResultCount()).isGreaterThan(0);
}
```

**Por que AAA:** garante que cada teste tem uma única responsabilidade clara, facilita a leitura e a identificação de onde uma falha ocorre.

### Explicit Waits

Todas as interações usam `WebDriverWait` com `ExpectedConditions` em vez de `Thread.sleep()`. Isso garante:

- Testes mais rápidos (não espera tempo fixo, só até o elemento estar pronto)
- Testes mais estáveis (não falha por timing em ambientes lentos)
- Timeouts configuráveis via `application.yml`

## Stack Tecnológica

| Tecnologia | Versão | Justificativa |
|-----------|--------|---------------|
| Java | 17 | LTS atual, suporte a switch expressions e records |
| Selenium WebDriver | 4.27.0 | Padrão de mercado para automação web, compatível com W3C WebDriver |
| JUnit 5 | 5.11.4 | Framework de testes moderno com suporte a `@DisplayName`, `@Nested`, testes parametrizados |
| Gradle | 8.12 | Build tool mais conciso e rápido que Maven, DSL Groovy legível |
| Allure Report | 2.29.1 | Relatórios visuais com severidade, steps, screenshots — padrão enterprise |
| AssertJ | 3.27.3 | Assertions fluentes e legíveis (`assertThat().isGreaterThan()`) |
| WebDriverManager | 5.9.2 | Gerenciamento automático de drivers (ChromeDriver, GeckoDriver) |

## Estrutura do Projeto

```
qa-test-web-selenium/
├── .github/workflows/test.yml          # Pipeline CI/CD
├── build.gradle                        # Dependências e configuração
├── src/
│   ├── main/java/com/qatest/web/
│   │   ├── config/
│   │   │   └── ConfigManager.java      # Singleton — carrega application.yml
│   │   ├── driver/
│   │   │   ├── DriverFactory.java      # Factory — cria WebDriver por browser
│   │   │   └── DriverManager.java      # Singleton + ThreadLocal — gerencia instâncias
│   │   ├── pages/
│   │   │   ├── BasePage.java           # Classe base — waits e ações comuns
│   │   │   ├── HomePage.java           # Page Object — página inicial + pesquisa
│   │   │   └── SearchResultPage.java   # Page Object — resultados da pesquisa
│   │   └── utils/
│   │       └── WaitUtils.java          # Explicit waits centralizados
│   ├── main/resources/
│   │   └── application.yml             # Configurações (URL, browser, timeouts)
│   └── test/java/com/qatest/web/
│       ├── base/
│       │   └── BaseTest.java           # Setup/teardown — cria e destrói driver
│       └── tests/
│           └── SearchTest.java         # 4 cenários de teste
└── README.md
```

## Pré-requisitos

- **Java 17+** — [Download](https://adoptium.net/)
- **Google Chrome** — [Download](https://www.google.com/chrome/)
- **Git** — [Download](https://git-scm.com/)

> O ChromeDriver é gerenciado automaticamente pelo WebDriverManager — não precisa instalar manualmente.

## Configuração e Execução

```bash
# 1. Clonar o repositório
git clone https://github.com/filipeCardorso/qa-test-web-selenium.git
cd qa-test-web-selenium

# 2. Executar testes (modo visual — abre o browser)
./gradlew test

# 3. Executar testes (headless — sem abrir browser)
CI=true ./gradlew test

# 4. Gerar relatório Allure
./gradlew allureReport

# 5. Abrir relatório no browser
./gradlew allureServe
```

### Configuração do Browser

Editar `src/main/resources/application.yml`:

```yaml
web:
  base-url: https://blog.agibank.com.br/
  browser: chrome        # chrome | firefox | edge
  headless: false        # true para rodar sem interface gráfica
  timeout:
    implicit: 10         # segundos
    explicit: 15         # segundos
    page-load: 30        # segundos
```

## CI/CD

O projeto possui pipeline **GitHub Actions** que executa automaticamente a cada push ou pull request:

1. **Setup:** Configura JDK 17, Chrome stable e Gradle
2. **Testes:** Executa todos os cenários em modo headless
3. **Relatório de Resultados:** Publica gráficos com contagem de testes (passed/failed/skipped) diretamente no Summary da pipeline
4. **Allure Report:** Gera relatório e salva como artifact
5. **Artifacts:** Salva o relatório Allure para download (retenção de 30 dias)

## Relatórios

| Tipo | Onde Encontrar | O Que Mostra |
|------|---------------|-------------|
| **JUnit (terminal)** | Output do `./gradlew test` | Resultado de cada teste (PASSED/FAILED) |
| **Pipeline Summary** | GitHub Actions > run > Summary | Gráficos com total de testes, taxa de sucesso |
| **Allure Report** | Artifact no GitHub Actions ou `./gradlew allureServe` | Cenários detalhados, severidade, duração, steps, histórico |

## Decisões Técnicas

| Decisão | Alternativa | Justificativa |
|---------|------------|---------------|
| Gradle sobre Maven | Maven | Menos boilerplate, builds mais rápidos, DSL legível |
| JUnit 5 sobre TestNG | TestNG | Standard atual do ecossistema Java, melhor integração com IDEs e CI |
| AssertJ sobre Hamcrest | Hamcrest (built-in JUnit) | API fluente, mensagens de erro mais claras, autocomplete no IDE |
| Explicit Waits sobre Thread.sleep | Thread.sleep | Mais rápido, mais estável, timeouts configuráveis |
| YAML sobre properties | .properties | Suporta hierarquia, mais legível para configurações aninhadas |
| JS click sobre click nativo | Selenium click | Compatibilidade com headless mode no tema Astra slide-search |
| URL base `blog.agibank.com.br` | `blogdoagi.com.br` | Evita redirect 301 que adiciona latência desnecessária |
