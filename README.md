# Selenium + TestNG Test Framework

[![selenium-tests](https://github.com/kayla-damoah/selenium-testng-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/kayla-damoah/selenium-testng-framework/actions/workflows/ci.yml)

UI test automation in Java 17 with Selenium 4, TestNG and Maven, run in GitHub Actions on every push.

Target: [Sauce Demo](https://www.saucedemo.com), a public practice shop.

New to the project? Start with [docs/HOW_IT_WORKS.md](docs/HOW_IT_WORKS.md), a plain-English walkthrough.

## What it demonstrates

- **Page Object Model**: selectors and page behavior live in `pages/`, tests only describe intent.
- **Explicit waits only**: `BasePage` wraps `WebDriverWait`; there is no `Thread.sleep`.
- **Parallel-safe**: a `ThreadLocal<WebDriver>` gives each test its own browser, and `testng.xml` runs classes in parallel.
- **Data-driven tests**: TestNG `@DataProvider` for invalid-login cases.
- **Screenshot on failure**, uploaded as a CI artifact.
- **Config via system properties**: `-Dbrowser=chrome|firefox`, `-Dheadless=true|false`.

## Run it

Needs JDK 17+, Maven, and Chrome (Selenium Manager downloads the driver itself).

```bash
mvn test                              # headless Chrome, full suite
mvn test -Dheadless=false             # watch it run
mvn test -Dbrowser=firefox
mvn test -Dgroups=smoke               # smoke subset only
```

Reports: `target/surefire-reports/`. Failure screenshots: `screenshots/`.

## Layout

```
src/test/java/com/kayla/qa/
  base/    DriverFactory, BaseTest (setup, teardown, screenshots)
  pages/   BasePage, LoginPage, InventoryPage, CartPage
  tests/   LoginTests, InventoryTests, CheckoutTests
testng.xml
```

## Ideas to extend

- Allure or ExtentReports HTML reporting
- Selenium Grid / Docker for cross-browser runs
- A retry analyzer with flaky-test reporting
- API setup of test state to speed up UI tests
