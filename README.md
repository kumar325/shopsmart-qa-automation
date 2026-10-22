# ShopSmart QA Automation

A portfolio SDET project covering web, API, mobile and network testing.

> Work in progress: this is the initial project skeleton. No tests have been implemented or run yet.

## Tech stack

- Java 17, Maven
- Selenium 4 + TestNG (Page Object Model)
- Postman / Newman
- Appium (Java client)
- Wireshark
- GitHub Actions

## Project structure

```
shopsmart-qa-automation/
├── src/
│   ├── main/java/pages/        # Page Objects
│   └── test/java/
│       ├── web/                # Selenium tests (SauceDemo)
│       ├── api/                # API tests
│       └── mobile/             # Appium tests (Sauce Labs sample app)
├── postman/                    # Postman collection + environment (Restful Booker)
├── captures/                   # Wireshark captures (git-ignored by default)
├── docs/                       # Test plan, defect reports, network analysis
├── pom.xml
└── README.md
```

## Applications under test

All targets are public practice applications intended for test automation:

- Web: https://www.saucedemo.com
- API: https://restful-booker.herokuapp.com
- Mobile: Sauce Labs sample app

## Secrets

No credentials are committed. The Postman environment ships with empty `username`, `password`
and `token` values; supply them locally or through CI secrets.
