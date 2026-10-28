# How it works

This document covers the design of the test suite: how a run flows through the code, why it is structured this way, and what it took to make it stable on CI.

## Overview

The suite drives a real browser through a public demo shop ([Sauce Demo](https://www.saucedemo.com)): logging in, browsing and sorting products, filling the cart and checking out. It runs headless on GitHub Actions for every push and publishes an [Allure report](https://kayla-damoah.github.io/selenium-testng-framework/).

| Tool | Role |
|---|---|
| Selenium WebDriver | Controls the browser from Java |
| TestNG | Discovers and runs tests; provides annotations, data providers and groups |
| Maven | Builds the project, manages dependencies, runs the suite (`pom.xml`) |
| Allure | Turns results into a browsable report with steps and failure screenshots |
| GitHub Actions | Runs the suite and publishes the report on every push |

## Layers

```
Tests          what is being checked                 tests/
Page objects   where things are, how to use them     pages/
Base classes   browser lifecycle, waits, retries     base/, BasePage
Selenium       browser control
```

Each layer only talks to the one below it. Tests never contain selectors, and page objects never contain assertions.

## Project layout

```
pom.xml                    Dependencies, surefire config, Allure plugin
testng.xml                 Which tests run, and the thread count
.github/workflows/ci.yml   Run tests, build the report, publish to Pages
src/test/java/com/kayla/qa/
  base/DriverFactory       Builds Chrome or Firefox (headless or visible)
  base/BaseTest            Browser per test; screenshot on failure; teardown
  pages/BasePage           Waits, click and type helpers that verify their result
  pages/LoginPage          Login screen
  pages/InventoryPage      Product list: add/remove, sort, cart badge
  pages/CartPage           Cart and checkout flow
  tests/LoginTests         Valid and invalid login
  tests/InventoryTests     Product count, cart badge, price sorting
  tests/CheckoutTests      Cart contents and end-to-end purchase
```

## Anatomy of a test

Following `CheckoutTests.endToEndCheckoutShowsConfirmation`:

1. TestNG finds the `@Test` method.
2. `BaseTest.setUp()` (`@BeforeMethod`) calls `DriverFactory.create()` and stores a fresh browser for the current thread.
3. `new LoginPage(driver()).open().loginAs(...)` opens the site, logs in, and returns an `InventoryPage`.
4. `addToCart(...)` clicks the product's button and confirms it changed to "Remove".
5. `openCart()` navigates to the cart and returns a `CartPage`.
6. `checkout(...)` fills the form, continues, and finishes.
7. The test asserts the confirmation message.
8. `BaseTest.tearDown()` (`@AfterMethod`) saves and attaches a screenshot if the test failed, then quits the browser.

## Design decisions

- **Page Object Model.** Selectors live in one place per screen, so a UI change is a one-line fix instead of an edit to every test.
- **Explicit waits only.** `BasePage` wraps `WebDriverWait`. There is no `Thread.sleep` and no implicit wait, so each step waits exactly as long as the page needs.
- **Browser per test, held in a `ThreadLocal`.** Tests do not share state. The suite is parallel-capable, but CI runs it with `thread-count="1"` (see below).
- **Actions verify their own result.** `clickUntil` and `type` in `BasePage` check that a click or keystroke took effect and retry if it did not.
- **Data-driven tests.** Invalid-login cases run from a single TestNG `@DataProvider`.
- **Groups.** Tests tagged `smoke` form a fast subset: `mvn test -Dgroups=smoke`.
- **Configuration through system properties.** `-Dbrowser` and `-Dheadless` select the browser and mode.

## Reporting

Allure's TestNG integration records every run. Page-object methods are annotated with `@Step`, so each test in the report shows its actions in order, grouped by feature (Login, Inventory, Checkout). Failed tests carry an attached screenshot. The AspectJ agent configured in `pom.xml` is what makes the `@Step` annotations visible to Allure.

CI builds the report after every run, including failed ones, and publishes it to GitHub Pages from `main`.

## Stabilising the suite on CI

The first CI run passed 9 of 12 tests. All three failures involved an action that the page silently ignored, typically right after the cart updated. Selenium reported no error in these cases. Progress came from reading the CI log and the failure screenshots uploaded with each run:

| Round | Evidence | Change | Result |
|---|---|---|---|
| 1 | Clicks right after "Add to cart" had no effect | Wait for the button to change to "Remove" | Still failing |
| 2 | Screenshots showed a healthy page with nothing covering it; the click was lost | `clickUntil`: click, verify the effect, retry | Still failing |
| 3 | Three browsers ran at once on a small CI runner | Run tests one at a time | 10 of 12 pass |
| 4 | A few clicks were still lost, even on retry | Retries fall back to a JavaScript click | 11 of 12 pass |
| 5 | Screenshot showed "First Name is required" with empty fields; typed text had vanished | `type` verifies the text landed; checkout retries fill and submit together | 11 of 12 pass |
| 6 | Field focused but empty after three rapid retries, which all landed in the same bad moment | Spaced-out attempts, the text must hold for 750 ms, later attempts set the value through JavaScript | 12 of 12 pass |

The pattern is that the demo site sometimes drops input while it is redrawing, and the driver cannot see that. Every action therefore checks its outcome instead of assuming it succeeded.

The root cause on the site's side was not established. Retrying and verifying is a reasonable defence against a third-party UI that cannot be changed; for an application under the team's control, the next step would be to find and fix the underlying behaviour.

## Known limitations

- Firefox is supported in `DriverFactory` but has not been run in CI.
- Tests run serially on CI. Parallel runs caused dropped actions on the shared runner.
- The retry and JavaScript-fallback logic is a workaround for the demo site's behaviour, not a general pattern to copy into every project.
- The Allure report shows only the latest run; there is no history across runs.
