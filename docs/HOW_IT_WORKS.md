# How this project works

A plain-English walkthrough. Read it top to bottom once, then open the code with it beside you.

## 1. The big idea

A person testing a shop website would open a browser, log in, add something to the cart, check out, and look at what happened. This project does the same steps with code, so they can be repeated in seconds, on every change, with no one clicking.

```
 You / GitHub  ->  Maven  ->  TestNG  ->  Test  ->  Page object  ->  Selenium  ->  Chrome  ->  Website
 (run tests)     (build)   (runs tests)  (what)    (where/how)     (remote control)
```

| Piece | What it is | Everyday comparison |
|---|---|---|
| **Selenium** | A library that controls a real browser from code | A remote control for Chrome |
| **TestNG** | A framework that finds, runs and reports on tests | A referee that runs each check and writes down pass/fail |
| **Maven** | A build tool; downloads libraries and runs everything | The project's manager: `pom.xml` is its shopping list |
| **Page Object Model** | One class per screen that holds its selectors and actions | A map of each screen |
| **GitHub Actions** | Runs the tests on GitHub's computers on every push | A robot that re-runs your checks for you |

## 2. A tour of the files

```
pom.xml                  Shopping list: Java version, Selenium, TestNG, how to run tests
testng.xml               Which tests to run, and to run 3 classes at once (parallel)
.github/workflows/ci.yml The robot's instructions for GitHub
src/test/java/com/kayla/qa/
  base/DriverFactory     Opens Chrome or Firefox (headless or visible)
  base/BaseTest          Before each test: open a browser. After: screenshot if failed, close it
  pages/BasePage         Shared helpers: wait for an element, click, type
  pages/LoginPage        The login screen
  pages/InventoryPage    The product list
  pages/CartPage         The cart and checkout
  tests/LoginTests       Login checks
  tests/InventoryTests   Product list checks
  tests/CheckoutTests    Purchase checks
```

## 3. Following one test from start to finish

Take `endToEndCheckoutShowsConfirmation` in `CheckoutTests`.

1. TestNG sees `@Test` and decides to run it.
2. Before it runs, `BaseTest.setUp()` calls `DriverFactory.create()`, which opens a fresh Chrome.
3. The test calls `new LoginPage(driver()).open().loginAs(...)`. That visits the site, types the username and password, clicks Login, and hands back an `InventoryPage`.
4. `inventory.addToCart("sauce-labs-bolt-t-shirt")` finds the button by its `data-test` attribute and clicks it.
5. `inventory.openCart()` clicks the cart icon and returns a `CartPage`.
6. `cart.checkout(...)` fills in the form, clicks Continue and Finish.
7. `assertTrue(...)` checks the page says "Thank you". If not, the test **fails**.
8. After the test, `BaseTest.tearDown()` saves a screenshot if it failed, then closes Chrome.

The test reads like a story. It never mentions a CSS selector, because those are hidden in the page objects.

## 4. Why it's built this way

**Page objects.** If the site changes the login button, you fix one line in `LoginPage` instead of every test that logs in.

**Explicit waits.** Pages load at different speeds. Instead of sleeping for a fixed time (slow and unreliable), `BasePage` waits until the element is actually there, up to 10 seconds. This is the single biggest reason UI tests become "flaky" when done wrong.

**ThreadLocal driver.** `testng.xml` runs three test classes at the same time. Each needs its own browser, or they'd click over each other. `ThreadLocal` gives every thread its own private copy.

**DataProvider.** `invalidLoginShowsError` runs 4 times with 4 sets of inputs from one method, instead of 4 copy-pasted tests.

**Groups.** Tests tagged `smoke` are the fast, most important ones: `mvn test -Dgroups=smoke`.

**Headless.** Runs Chrome without a window, which is what CI needs. Use `-Dheadless=false` to watch.

## 5. Java ideas you'll see (and can explain)

- **Inheritance**: `LoginTests extends BaseTest` gets the browser setup for free. `LoginPage extends BasePage` gets waits and click helpers.
- **Encapsulation**: selectors are `private static final`; only the page's methods are public.
- **Method chaining**: `open()` returns `this`, so you can write `new LoginPage(d).open().loginAs(...)`.
- **Annotations**: `@Test`, `@BeforeMethod`, `@AfterMethod`, `@DataProvider` tell TestNG what each method is for.
- **Assertions**: `assertEquals(actual, expected)` is the actual check. No assertion means no test.
- **Lambdas/streams**: `.stream().map(...).toList()` turns a list of page elements into a list of numbers or text.

## 6. Run it and break it (the best way to learn)

1. `mvn test -Dheadless=false` and watch the browser.
2. Change `"secret_sauce"` in one test to a wrong password and confirm the test fails and a screenshot appears in `screenshots/`.
3. Change a selector in `LoginPage` and see the error message you get.
4. Add a new test: log in, add three items, assert the badge shows 3.
5. Add a new page object, for example for the checkout overview page.

## 7. Questions you may get in an interview

- *Why page objects?* Maintenance: UI changes touch one class.
- *Why not `Thread.sleep`?* Slow and unreliable; explicit waits are as fast as the page allows.
- *How do you handle flaky tests?* Find the cause (usually timing or shared state), fix it with proper waits and isolated browsers; don't hide it with retries.
- *How do tests run in CI?* GitHub Actions starts Ubuntu, installs Java 17, runs `mvn test` headless, uploads reports and screenshots.
- *What would you add next?* Reporting (Allure), cross-browser runs on Selenium Grid, and API calls to set up test data faster.

Describe this as a personal project you built to learn Selenium and TestNG, which is accurate.

## 8. A real debugging story: actions the page silently drops

The first CI run passed 9 of 12 tests. Getting to green took several rounds, each driven by evidence from CI (the log plus the failure screenshots uploaded as an artifact).

| Round | What the evidence showed | What we changed | Result |
|---|---|---|---|
| 1 | Clicks right after "Add to cart" did nothing; Selenium raised no error | Wait for the button to flip to "Remove" | Still failing |
| 2 | Screenshots: healthy page, nothing covering it; the click was just lost | `clickUntil`: click, verify the effect, retry | Still failing |
| 3 | Three browsers at once on a small CI machine | Run tests one at a time | 10 of 12 pass |
| 4 | A few clicks still lost, even on retry | Retries use a JavaScript click as a fallback | 11 of 12 pass |
| 5 | Screenshot: "First Name is required" with empty fields; typed text had vanished | `type` verifies the text landed; checkout retries fill + Continue together | (pending) |

**The pattern:** the site sometimes drops actions made while the page is redrawing, and Selenium cannot see that. So every step must **verify its result**, not just perform the action.

**Honest caveat:** I never confirmed *why* the site behaves this way. Retries and verification are a sound defence for a third-party UI you don't control. For your own application, you would find and fix the root cause instead of working around it.

**Interview answer:** "A third-party demo site dropped clicks and keystrokes intermittently. I used the CI logs and failure screenshots to narrow it down, ran the suite serially, and made each page action verify its own result instead of assuming it worked."
