# Test Plan

## 1. Scope

| Area | Application under test | Tooling | Features in scope |
|---|---|---|---|
| Web | SauceDemo (https://www.saucedemo.com) | Selenium 4 + TestNG | Login, logout and access control; product sorting; cart; checkout |
| API | Restful Booker (https://restful-booker.herokuapp.com) | Postman / Newman | Health check, authentication, booking CRUD, negative cases |
| Mobile | Sauce Labs My Demo App, Android 2.3.0 | Appium 2 + TestNG | Login, cart quantity and removal, guest checkout, address validation |
| Network | Traffic of the API run | Wireshark | DNS, TCP, TLS and HTTP behaviour; credential exposure |

## 2. Out of scope

- Performance and load testing. Both targets are shared public demo systems; load testing them
  would be inappropriate.
- Security testing beyond observation (no scanning, fuzzing or authentication bypass attempts).
- Visual/pixel comparison, accessibility audits and localisation.
- iOS. Only the Android build of My Demo App is tested.
- Browsers other than Chrome and Edge.
- SauceDemo's deliberately broken users (`problem_user`, `error_user`) in automated regression.
  They were explored manually and their defects are recorded in [bug-reports.md](bug-reports.md).

## 3. Test approach

### Web (Selenium)

- **Page Object Model.** Each page is a class in `src/main/java/pages` that owns its locators and
  exposes user actions (`loginAs`, `addToCart`, `sortBy`). Tests contain no locators, so a UI change
  is fixed in one place.
- **Locators** prefer SauceDemo's `data-test` attributes, which exist for automation and do not
  change with styling.
- **Synchronisation** uses explicit waits only (`BasePage` wraps every action in `WebDriverWait`).
  There is no `Thread.sleep`.
- **Isolation.** Every test method starts a new browser, so no state carries over. This makes
  parallel execution safe (`parallel="methods"`, 4 threads, one `ThreadLocal` driver per thread).
- **Data-driven tests** use TestNG `@DataProvider` for the invalid-login matrix, sort options and
  required checkout fields.
- **Assertions check values, not just presence**: sorted order is compared with a sorted copy;
  checkout totals are recalculated from item prices (subtotal + 8% tax).

| Suite | Test methods | Executions (with data providers) |
|---|---|---|
| `LoginTest` | 4 | 8 |
| `SortingTest` | 3 | 5 |
| `CartTest` | 6 | 6 |
| `CheckoutTest` | 3 (+1 known-defect) | 5 |
| **Total** | 16 | 24 |

### API (Postman / Newman)

- Requests are grouped into folders that run in order: `00 Health`, `01 Auth`, `02 Bookings - CRUD`,
  `03 Bookings - Negative`, `04 Teardown`.
- The CRUD folder chains requests: the id returned by *Create* is stored in a collection variable
  and used by *Get*, *Update*, *Patch* and *Delete*. The final *Get* confirms the deletion with a 404.
- Both auth mechanisms are covered: token cookie (PUT) and Basic auth (PATCH).
- Negative tests cover missing or invalid auth, unknown and non-numeric ids, missing fields,
  malformed JSON, wrong data types and invalid date ranges.
- Every request asserts status code, response time (< 5 s), and body schema or values where relevant.
- Test data is created by the collection and removed in the teardown folder.
- Assertions record the API's **actual** behaviour where it differs from good practice (for
  example, DELETE returning 201). Each such deviation is logged as a defect rather than hidden.

Size: 26 requests, 66 assertions.

### Mobile (Appium)

- Screen objects in `src/main/java/pages/mobile` mirror the web Page Object Model.
- Locators are mostly accessibility ids (12 of 15), verified against the running app's UI hierarchy.
- Runs sequentially: one emulator cannot safely host two sessions of the same app.
- Each test starts a new Appium session, which clears the app's data, so every test begins logged
  out with an empty cart.

Size: 8 tests (+2 known-defect).

### Network (Wireshark)

The Newman run is captured and analysed layer by layer (DNS → TCP → TLS → HTTP), with TLS decrypted
using a key log file. Details and filters are in [network-analysis.md](network-analysis.md).

### Known defects

A test that reproduces an open defect asserts the *correct* behaviour and is tagged
`known-defect`. Normal runs exclude this group so the build stays meaningful, and the group can be
run on demand to check whether a bug has been fixed.

## 4. Test environment

| Item | Value |
|---|---|
| OS | Windows 11 (local), Ubuntu 24.04 (CI) |
| Java | 17 language level; run on JDK 21 |
| Build | Maven 3.9 via wrapper, Surefire 3.5.6 |
| Browsers | Chrome (default), Edge; drivers resolved by Selenium Manager |
| API runner | Newman 6.2.3 on Node.js 22 |
| Mobile | Android 14 (API 34) emulator `shopsmart_api34`, Appium 2 + UiAutomator2 driver |
| CI | GitHub Actions: web and API on every push to `main` and on pull requests |

Configuration lives in `config.properties` and can be overridden by environment variables or `-D`
properties. Credentials are supplied only through environment variables or CI secrets.

## 5. Entry and exit criteria

**Entry**
- The application under test is reachable (the API suite's health check returns 201).
- The project compiles (`mvnw test-compile`).
- Required credentials are set as environment variables.
- Mobile only: emulator booted, Appium server running, APK present in `apps/`.

**Exit**
- All tests outside `known-defect` pass, locally and in CI.
- Every failure has been investigated: either the test is fixed (test bug) or a defect is logged
  and the test is moved to `known-defect` (product bug).
- No test is skipped or disabled without a recorded reason.

## 6. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Public demo sites change or go down | False failures | Health check first; locators on stable `data-test` attributes; re-verify locators against the live site |
| Restful Booker is shared and resets periodically | Data from other users; ids disappear | Each run creates and deletes its own data; nothing depends on pre-existing records |
| Timing flakiness | Intermittent failures | Explicit waits only; fresh browser per test |
| Parallel tests interfering | Corrupted sessions | `ThreadLocal` driver; no shared static state |
| Emulator unavailable in CI | No mobile coverage in CI | Mobile suite run locally before merging; documented in README |
| Secrets leaking into the repo | Credential exposure | Environment variables only; Postman environment ships with empty values |
| Known product bugs failing every build | Real regressions get ignored | `known-defect` group excluded from regression runs |

## 7. Deliverables

- Automated suites: `src/test/java/web`, `src/test/java/mobile`, `postman/`
- CI workflow: `.github/workflows/tests.yml`
- Reports: Surefire HTML/XML, failure screenshots, Newman JUnit XML (uploaded as CI artifacts)
- [Defect reports](bug-reports.md)
- [Network analysis](network-analysis.md)
- [Interview questions](interview-questions.md)
