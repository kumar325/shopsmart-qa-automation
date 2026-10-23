# ShopSmart QA Automation

A portfolio SDET project covering web, API, mobile and network testing against public practice
applications.

| Layer | What is tested | Tooling | Last local run (2026-10-22) |
|---|---|---|---|
| Web | SauceDemo: login, sorting, cart, checkout | Selenium 4 + TestNG, Page Object Model | 24 tests, 0 failures |
| API | Restful Booker: CRUD + negative cases | Postman collection run with Newman | 26 requests, 66 assertions, 0 failures |
| Mobile | Sauce Labs My Demo App (Android) | Appium 2 (UiAutomator2) + TestNG | 8 tests, 0 failures |
| Network | DNS / TCP / TLS / HTTP analysis of the API run | Wireshark | see [docs/network-analysis.md](docs/network-analysis.md) |
| CI | Web + API suites on every push and pull request | GitHub Actions | see [.github/workflows/tests.yml](.github/workflows/tests.yml) |

Tests that reproduce open defects are tagged `known-defect` and excluded from the normal runs, so a
red build always means something new broke. See [Known-defect tests](#known-defect-tests).

## Documentation

- [Test plan](docs/test-plan.md): scope, approach, environments, entry/exit criteria, risks
- [Defect reports](docs/bug-reports.md): 14 defects observed while building this project
- [Network analysis](docs/network-analysis.md): Wireshark filters and a troubleshooting method
- [Interview questions](docs/interview-questions.md): questions and answers based on this code

## Tech stack

- Java 17 (compiled with `--release 17`; also runs on 21), Maven via the included wrapper (`mvnw`)
- Selenium 4, TestNG
- Postman / Newman 6
- Appium 2 with the UiAutomator2 driver, Appium Java client
- Wireshark
- GitHub Actions

## Project structure

```
shopsmart-qa-automation/
├── src/
│   ├── main/java/
│   │   ├── config/             # ConfigReader: -D property, then env variable, then config.properties
│   │   ├── driver/             # DriverFactory, DriverManager (ThreadLocal), MobileDriverManager
│   │   └── pages/              # Web page objects (+ pages/mobile/ screen objects)
│   ├── main/resources/config.properties   # Non-secret defaults
│   └── test/
│       ├── java/web/           # Selenium tests (SauceDemo)
│       ├── java/mobile/        # Appium tests (My Demo App)
│       ├── java/listeners/     # Screenshot on failure
│       └── resources/          # testng-web.xml, testng-mobile.xml
├── postman/                    # Collection + environment (Restful Booker)
├── scripts/start-emulator.ps1  # Starts the Android emulator with its window on screen
├── docs/                       # Test plan, defect reports, network analysis, interview questions
├── .github/workflows/tests.yml # CI
└── pom.xml
```

## Applications under test

All targets are public practice applications intended for test automation:

- Web: https://www.saucedemo.com
- API: https://restful-booker.herokuapp.com
- Mobile: [Sauce Labs My Demo App for Android](https://github.com/saucelabs/my-demo-app-android) 2.3.0

## Secrets

No credentials are committed. Every password is read from an environment variable at run time:

| Variable | Used by | Value for these practice apps |
|---|---|---|
| `SAUCEDEMO_PASSWORD` | Web tests | Shown on the SauceDemo login page |
| `MOBILE_PASSWORD` | Mobile tests | Shown on the My Demo App login screen |
| `username` / `password` (Newman `--env-var`) | API collection | Listed in the Restful Booker API docs |

The Postman environment ships with empty `username` and `password` values. In CI the same values
come from repository secrets (`SAUCEDEMO_PASSWORD`, `RB_USERNAME`, `RB_PASSWORD`).

## Running the web tests

Open PowerShell in the project folder (`C:\Users\Parth\Desktop\KaranCode\shopsmart-qa-automation`) and run:

```powershell
$env:SAUCEDEMO_PASSWORD='secret_sauce'
.\mvnw.cmd test "-Dheadless=false"
```

- The first line sets the password for this terminal session only.
- The web suite (`src/test/resources/testng-web.xml`) is the default, so no suite needs to be named.
- `-Dheadless=false` shows the browser. Leave it out to run headless, as CI does.
- Up to 4 browsers open at once: the suite runs test methods in parallel, each with its own driver.

Other options:

| Goal | Command |
|---|---|
| Headless (as in CI) | `.\mvnw.cmd test` |
| Edge instead of Chrome | `.\mvnw.cmd test "-Dbrowser=edge"` |
| One test class | `.\mvnw.cmd test "-Dtest=LoginTest"` |

On macOS/Linux use `./mvnw` and `export SAUCEDEMO_PASSWORD=...`.

Results: `target/surefire-reports/` (open `index.html`). Failing tests (web and mobile) save a
screenshot to `target/screenshots/`.

## Running the API tests

Requires Node.js and Newman (`npm install -g newman`).

```powershell
newman run postman\ShopSmart-API.postman_collection.json `
  -e postman\ShopSmart-Dev.postman_environment.json `
  --env-var "username=<restful-booker user>" --env-var "password=<restful-booker password>"
```

The collection creates its own bookings and deletes them in the `04 Teardown` folder, so it can be
run repeatedly. It can also be imported into Postman and run with the Collection Runner.

## Running the mobile tests

One-time setup:

1. Install Android Studio, then create an Android 14 (API 34) emulator named `shopsmart_api34`.
2. Set `ANDROID_HOME` to the SDK folder (on Windows usually `%LOCALAPPDATA%\Android\Sdk`).
   Appium refuses to start a session without it.
3. Install Appium and its Android driver: `npm install -g appium` then `appium driver install uiautomator2`.
4. Download the app into `apps/` (the folder is git-ignored):
   ```powershell
   New-Item -ItemType Directory -Force apps
   Invoke-WebRequest https://github.com/saucelabs/my-demo-app-android/releases/download/2.3.0/mda-2.3.0-27.apk -OutFile apps\mda-2.3.0-27.apk
   ```

Each run:

```powershell
.\scripts\start-emulator.ps1     # 1. start the emulator (window is moved onto the screen)
appium                           # 2. in a second terminal; leave it running
$env:MOBILE_PASSWORD='10203040'  # 3. back in the first terminal
.\mvnw.cmd test "-Dsuite=src/test/resources/testng-mobile.xml"
```

The mobile suite runs sequentially (one emulator, one app) and takes about 4 minutes.
`start-emulator.ps1` exists because on high-DPI displays the emulator can open its window outside the
visible screen, so it looks like it is running in the background.

Mobile tests are not run in CI: hosted runners have no hardware acceleration for an emulator, which
makes them slow and flaky.

## Known-defect tests

Tests in the `known-defect` group assert the **correct** behaviour, so they fail until the bug is
fixed. To run them, select classes with `-Dtest` (this bypasses the suite file, whose exclude would
otherwise filter them out) and add the group:

```powershell
.\mvnw.cmd test "-Dtest=web.*Test" "-Dgroups=known-defect"
```

Verified result: 1 test, 1 failure. `itemTotalIsFormattedToTwoDecimals` reports
`expected [Item total: $59.98] but found [Item total: $59.980000000000004]` (BUG-WEB-01).

## Continuous integration

`.github/workflows/tests.yml` runs on every push to `main`, on pull requests, and manually:

- **web-tests**: headless Chrome on Ubuntu, Java 21. Uploads Surefire reports and failure screenshots.
- **api-tests**: Newman 6.2.3. Uploads a JUnit XML report.

Both jobs read credentials from repository secrets. Concurrent runs on the same branch are cancelled
so only the latest commit is tested.
