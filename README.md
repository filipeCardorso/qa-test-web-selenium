# QA Test - Web Automation (Selenium + Java)

Automação de testes para a pesquisa do Blog do Agi (https://blogdoagi.com.br/).

## Stack

| Tecnologia | Versão |
|-----------|--------|
| Java | 17 |
| Selenium WebDriver | 4.27.0 |
| JUnit 5 | 5.11.4 |
| Gradle | 8.x |
| Allure Report | 2.29.1 |
| AssertJ | 3.27.3 |

## Arquitetura

O projeto utiliza os seguintes Design Patterns:

- **Page Object Model** — encapsula elementos e ações por página
- **Factory Pattern** — criação de WebDriver por tipo de browser
- **Singleton** — gerenciamento thread-safe de driver e configuração
- **Strategy** — troca de browser sem alterar testes

Padrão **AAA (Arrange-Act-Assert)** em todos os testes.

## Cenários de Teste

| # | Cenário | Severidade |
|---|---------|-----------|
| 1 | Pesquisa com termo válido retorna resultados | Critical |
| 2 | Pesquisa com termo inexistente exibe mensagem | Critical |
| 3 | Pesquisa com caracteres especiais não quebra | Normal |
| 4 | Resultado contém título e link coerentes | Critical |

## Pré-requisitos

- Java 17+
- Google Chrome instalado
- Git

## Executar os Testes

```bash
# Clonar o repositório
git clone https://github.com/SEU_USUARIO/qa-test-web-selenium.git
cd qa-test-web-selenium

# Executar testes (modo visual)
./gradlew test

# Executar testes (headless)
CI=true ./gradlew test

# Gerar relatório Allure
./gradlew allureReport

# Abrir relatório
./gradlew allureServe
```

## Estrutura do Projeto

```
src/
├── main/java/com/qatest/web/
│   ├── config/        # Configuração (Singleton)
│   ├── driver/        # WebDriver Factory e Manager
│   ├── pages/         # Page Objects
│   └── utils/         # Utilitários (Waits)
└── test/java/com/qatest/web/
    ├── base/          # BaseTest
    └── tests/         # Cenários de teste
```

## CI/CD

O projeto possui pipeline GitHub Actions que:
1. Configura JDK 17 e Chrome
2. Executa os testes em modo headless
3. Gera e salva o relatório Allure como artifact
