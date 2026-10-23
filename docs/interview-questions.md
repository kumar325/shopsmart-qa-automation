# Interview Questions

Questions an SDET interviewer is likely to ask, answered with examples from this project. Each
answer points to the file where you can show the code.

---

## 1. Framework design

**Q: Explain the Page Object Model and why you used it.**
Each page of the application is a class that owns that page's locators and exposes what a user can
do there. For example, `LoginPage` has `loginAs(user, password)` and `getErrorMessage()`. Tests only
call these methods and never touch locators. If SauceDemo renames the login button, I change one
line in `LoginPage`, not every test that logs in.
*Show:* `src/main/java/pages/LoginPage.java`, `src/test/java/web/LoginTest.java`

**Q: Why do some page methods return another page object?**
`loginAs` returns an `InventoryPage` because a successful login takes you there. This models
navigation in the type system and lets tests chain calls:
`loginAsStandardUser().sortBy(PRICE_LOW_TO_HIGH).getProductPrices()`. `attemptLogin` returns
`LoginPage` because a failed login keeps you on the same page.

**Q: What goes in `BasePage`?**
Shared behaviour: wait-wrapped helpers (`click`, `type`, `visible`) and the header that appears on
every logged-in page (title, cart badge, logout). Every page inherits these instead of repeating them.
*Show:* `src/main/java/pages/BasePage.java`

**Q: How is configuration handled?**
`ConfigReader` looks up a key in this order: `-D` system property, then environment variable, then
`config.properties`. Defaults live in the file. CI or a developer can override anything from the
command line (`-Dbrowser=edge`) without editing files, and secrets only ever come from environment
variables. `require()` fails immediately with a clear message if a setting is missing.
*Show:* `src/main/java/config/ConfigReader.java`

## 2. Selenium

**Q: Implicit vs explicit waits? Which did you use?**
Explicit waits only. An implicit wait applies to every `findElement` and can't wait for conditions
like "clickable" or "text present". Mixing both can produce unpredictable timeouts. `BasePage` wraps
every action in `WebDriverWait` with an `ExpectedConditions` check, and there is no `Thread.sleep`
anywhere.

**Q: Give an example of a wait you had to think about.**
In `LoginPage.loginAs` the wait is `ExpectedConditions.or(urlContains("/inventory.html"),
visibilityOfElementLocated(ERROR))`. Waiting only for the inventory page would mean a rejected
login hangs for the full timeout and then reports a generic `TimeoutException`. Waiting for either
outcome fails immediately with SauceDemo's own error text.

**Q: How do you choose locators?**
In order of preference: attributes meant for testing (`data-test`), then id, then CSS, and XPath
only as a last resort. SauceDemo provides `data-test` attributes, so almost every web locator uses
them. They don't change when the styling changes.

**Q: How do the tests run in parallel safely?**
`testng-web.xml` sets `parallel="methods"` with 4 threads. `DriverManager` stores the WebDriver in a
`ThreadLocal`, so each thread has its own browser. Every test also starts a fresh browser in
`@BeforeMethod`, so no cookies or cart state is shared. `quit()` calls `ThreadLocal.remove()` so a
reused thread never picks up a dead driver.
*Show:* `src/main/java/driver/DriverManager.java`, `src/test/java/web/BaseTest.java`

**Q: How do you get evidence when a test fails?**
`ScreenshotListener` implements TestNG's `ITestListener`. In `onTestFailure` it saves a PNG to
`target/screenshots/` before `@AfterMethod` closes the browser. CI uploads that folder as an artifact.
It's registered with `@Listeners` on the base test class, not only in the suite XML, because running
a single class with `-Dtest=` bypasses the suite file.

**Q: Describe a non-obvious problem you hit with Selenium.**
After logging in, Chrome's password manager showed a "password found in a data breach" dialog,
because SauceDemo's demo password is public. The dialog is browser UI, not page DOM, so Selenium
couldn't see it, yet it blocked clicks. The fix was to disable the password manager through Chrome
preferences in `DriverFactory`, not to add retries.
*Show:* `src/main/java/driver/DriverFactory.java`

## 3. TestNG

**Q: How do you write data-driven tests?**
With `@DataProvider`. `LoginTest.invalidLogins` returns rows of username, password and expected
error, and one test method runs once per row. That's why 16 web test methods produce 24 executions.

**Q: What are groups used for here?**
Tests that reproduce known bugs are in the `known-defect` group and excluded in the suite XML. The
regression run stays green unless something new breaks, and the defect tests can still be run on
demand to check whether a bug was fixed.

**Q: Something that surprised you about TestNG and Maven?**
Two things:
1. Surefire 3.6.0 runs TestNG through the JUnit Platform, which ignores TestNG suite XML files, so
   parallel settings and group excludes silently stopped applying. I pinned Surefire 3.5.6 and
   documented why in `pom.xml`.
2. `-Dgroups=known-defect` on its own runs 0 tests, because the suite XML's exclude still applies
   and Maven can only narrow the selection. Adding `-Dtest=web.*Test` bypasses the suite file.
   A run that reports "0 tests, BUILD SUCCESS" is a warning sign, not a pass.

## 4. Assertions and test quality

**Q: How do you test sorting without hardcoding the expected order?**
Read the products from the screen, sort a copy with the expected comparator, and assert that the
two lists are equal. The test still works if products are added or re-priced. The name-sort test
first sorts in the *opposite* order, so it proves the sort actually changed something.
*Show:* `src/test/java/web/SortingTest.java`

**Q: Why `BigDecimal` for prices?**
`double` can't represent most decimal fractions exactly: 0.1 + 0.2 is 0.30000000000000004. Price
assertions using `double` would be flaky. `Money.parse` converts UI text to `BigDecimal`.
SauceDemo itself has this bug: its checkout shows `Item total: $59.980000000000004` (BUG-WEB-01),
which a `known-defect` test catches.

**Q: What does a good checkout test assert?**
Not just "the confirmation page appeared". `completePurchaseWithTwoItems` recalculates the item
total from the cart, checks tax is 8% (rounded half-up), checks total = item total + tax, and then
checks the confirmation.

**Q: What's the difference between severity and priority?**
Severity is the impact; priority is how soon to fix it. BUG-WEB-01 is minor severity (a display
issue) but high priority, because every customer sees it at checkout.
*Show:* `docs/bug-reports.md`

## 5. API testing

**Q: What does your Postman collection cover?**
26 requests in ordered folders: health check, auth (positive and negative), the full CRUD lifecycle,
negative booking cases and teardown. Create stores the new booking id in a variable, and the
following requests use it. The final GET expects 404 to prove the delete worked.

**Q: What negative tests did you write?**
Missing or invalid auth on PUT, PATCH and DELETE; unknown and non-numeric ids; missing required
fields; malformed JSON; a string where a number is expected; checkout before checkin; deleting twice.
Several of these exposed real defects, such as a 500 instead of a 400 for missing fields (BUG-API-02).

**Q: What if the API does something wrong but consistently, like DELETE returning 201?**
The test asserts the actual behaviour, so the suite reflects reality, and the deviation is logged
as a defect (BUG-API-05). A test that silently accepts wrong behaviour hides it; a test that always
fails gets ignored. Documenting it is the useful middle ground.

**Q: How do you avoid leaving test data behind?**
The collection creates every booking it uses and deletes them in `04 Teardown`. It never depends on
pre-existing records, which matters on a shared public API that other people also write to.

**Q: Token vs Basic auth?**
Restful Booker accepts both. PUT uses a token from `/auth` sent as a cookie; PATCH uses Basic auth.
Basic auth is only Base64-encoded, not encrypted, which matters because of the next question.

## 6. Mobile (Appium)

**Q: How is the mobile framework structured?**
The same way as web: screen objects in `pages/mobile`, a `ThreadLocal` driver in
`MobileDriverManager`, and a base test that starts a session per test. A new session clears app
data, so every test starts logged out with an empty cart.

**Q: Which locator strategy for mobile?**
Mostly accessibility ids (12 of 15 locators). They're stable, fast, and the same attribute screen
readers use. XPath on mobile is slow because the whole UI hierarchy has to be serialised.

**Q: Why aren't mobile tests in CI?**
Hosted GitHub runners don't have hardware acceleration for an Android emulator, which makes boot
slow and tests flaky. The mobile suite runs locally. Options for CI would be a device cloud or a
self-hosted runner.

**Q: Tell me about an environment problem you debugged.**
The emulator looked like it wasn't working: no window appeared. It was actually running; on a
high-DPI 4K display it put its window outside the visible screen. Separately, Appium failed every
session with "Neither ANDROID_HOME nor ANDROID_SDK_ROOT environment variable was exported". Lesson:
`SessionNotCreatedException` is almost always the environment, not the test code, so read the
server's message first. `scripts/start-emulator.ps1` now places the window on screen.

## 7. Network (Wireshark)

**Q: Capture filter vs display filter?**
A capture filter (BPF syntax) decides what is recorded, and anything it drops is gone for good. A
display filter only hides packets in the view and can be changed at any time.

**Q: How would you debug an API test that fails only in CI?**
Work up the layers. DNS: did the name resolve? TCP: did the handshake complete, or were SYNs
retransmitted? TLS: was there an alert or an unexpected certificate (proxy)? HTTP: decrypt and
compare the exact request with what the test intended. Then compare `tcp.analysis.ack_rtt` (network
time) with `http.time` (server time) to see where the delay is.
*Show:* `docs/network-analysis.md`, section 4

**Q: What can you still see in HTTPS traffic?**
IP addresses, ports, timing, sizes, DNS queries and the SNI hostname. The URL path, headers and body
are encrypted.

**Q: What did the capture reveal?**
Restful Booker is also served over plain HTTP, and credentials sent that way are visible in
cleartext (BUG-API-07).

## 8. CI/CD

**Q: Walk me through your GitHub Actions workflow.**
It runs on pushes to `main`, pull requests and manual dispatch. There are two parallel jobs: web
tests in headless Chrome on Java 21, and the Postman collection through Newman. Both upload reports
even when tests fail (`if: always()`). Credentials come from repository secrets. Permissions are
read-only, and a newer push cancels the previous run on the same branch.
*Show:* `.github/workflows/tests.yml`

**Q: How do you keep secrets out of the repository?**
Nothing sensitive is in source or config files. Tests read passwords from environment variables
(`SAUCEDEMO_PASSWORD`, `MOBILE_PASSWORD`), the Postman environment ships with empty values that
Newman fills with `--env-var`, and CI injects GitHub secrets.

**Q: What would you add next?**
- An HTML report such as Allure, with screenshots attached to failures.
- Cross-browser runs with a matrix of Chrome and Edge in CI.
- Retries for genuinely flaky infrastructure, never for real assertions.
- A device cloud for mobile tests in CI.
- Contract tests for the API using JSON Schema on every response.

## 9. General

**Q: A test is flaky. What do you do?**
Reproduce it first by running it repeatedly or in parallel. Find the cause: usually a missing wait,
shared state between tests, or test-data collisions. Fix the cause. Don't add `Thread.sleep` or
blind retries: they hide the problem and slow the suite down.

**Q: When should a test not be automated?**
When it runs rarely, needs human judgement (visual design, usability), or the feature is still
changing so fast that the test would be rewritten every week. Exploratory testing found most of the
`problem_user` and `error_user` defects in this project.
