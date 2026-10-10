<div align="center">

# 🚀 CRM Hybrid Automation Framework

**A modern, scalable, data-driven UI test automation framework**
built with **Selenium WebDriver · TestNG · Maven · Page Object Model**

[![Java](https://img.shields.io/badge/Java-17-E76F00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Selenium](https://img.shields.io/badge/Selenium-4.31-43B02A?style=for-the-badge&logo=selenium&logoColor=white)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.11-C4302B?style=for-the-badge&logo=testng&logoColor=white)](https://testng.org/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)](https://www.jenkins.io/)
[![Extent Reports](https://img.shields.io/badge/Extent_Reports-5.0.8-1B9AAA?style=for-the-badge)]()
[![Allure](https://img.shields.io/badge/Allure-2.29-B732CC?style=for-the-badge&logo=allure&logoColor=white)](https://allurereport.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

*Automation framework for the **Cogmento / FreeCRM** web application — Login, Home, Contacts & Forms.*

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [How to Run](#-how-to-run)
- [Test Suites](#-test-suites)
- [Configuration](#-configuration)
- [Reporting](#-reporting)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Docker](#-docker)
- [Kubernetes](#%EF%B8%8F-kubernetes)
- [Test Data Management](#-test-data-management)
- [Extending the Framework](#-extending-the-framework)
- [Troubleshooting](#-troubleshooting)
- [Best Practices](#-best-practices)

---

## 🎯 Overview

The **CRM Hybrid Automation Framework** is a *hybrid* test automation solution that combines three automation strategies into one cohesive, maintainable design:

| Strategy | How it's applied here |
|---|---|
| **Page Object Model** | Each screen of the CRM is a dedicated Page class holding locators + business flows |
| **Data-Driven** | `@DataProvider` + Apache POI reads test data from an Excel workbook |
| **Configuration-Driven** | Environment, browser, credentials & runtime flags come from `.properties` files selected via `-Denv` |

> **Application under test:** `https://ui.cogmento.com/` (FreeCRM / Cogmento UI)

The framework is designed for **parallel execution**, **multi-browser coverage**, **multi-environment runs**, and **rich reporting** — ready to plug into a Jenkins CI/CD pipeline.

---

## ✨ Key Features

- 🏗️ **Clean Page Object Model** — locators, actions and navigation flows isolated per page; pages return the *next* page object for readable chained flows
- 🧵 **Thread-Safe Driver Management** — `ThreadLocal<WebDriver>` inside `DriverFactory`, safe for parallel TestNG execution
- 🌐 **Multi-Browser Support** — Chrome, Firefox, Edge, Safari & IE via a single factory switch + **WebDriverManager** (no manual driver downloads)
- ☁️ **Local & Remote Execution** — flip `remote=true` to run against a Selenium Grid / Selenoid hub with VNC + screen-resolution capabilities
- ⚙️ **Multi-Environment Configuration** — `dev`, `qa`, `stage`, `uat`, `prod` profiles selected with `-Denv`
- 📊 **Dual Reporting** — **ExtentSpark** HTML report *and* **Allure** report, both with automatic failure screenshots
- 🔁 **Automatic Retry** — failed tests re-run up to 3× via a global `IAnnotationTransformer`
- 📈 **Excel Data-Driven Tests** — POI-backed `DataProvider`s feed contacts & forms test data
- 🔍 **Element Highlighting** — toggle `highlight=true` to visually flash every interaction during debug
- 🧰 **Rich Utility Layer** — `ElementUtil` (waits, actions, dropdowns, fluent waits), `JavaScriptUtil`, `ExcelUtil`
- 🛡️ **Custom Exceptions** — `BrowserException` / `FrameworkException` for fail-fast, descriptive errors
- 🔔 **TestNG Listeners** — reporting, screenshots, Allure attachments and status push to **Elasticsearch**
- 🚦 **Jenkins Declarative Pipeline** — build → regression → report publish → sanity → stage gates
- ⚡ **Parallel Execution** — `parallel="tests"` with 4 threads + Surefire `forkCount=3`

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Build Tool | Apache Maven | 3.x |
| Browser Automation | Selenium WebDriver | 4.31.0 |
| Driver Management | WebDriverManager | 5.9.2 |
| Test Framework | TestNG | 7.11.0 |
| Design Pattern | Page Object Model | — |
| Reporting | ExtentReports | 5.0.8 |
| Reporting | Allure TestNG | 2.29.1 |
| Test Data | Apache POI (Excel) | 3.9 |
| JSON Handling | Jackson Databind | 2.9.4 |
| HTTP / Results Push | Unirest | 1.4.9 |
| Logging (declared) | Log4j 2 | 2.14.1 |
| CI/CD | Jenkins (Declarative) | — |
| Results Sink | Elasticsearch (optional) | — |

---

## 🏛️ Architecture

```
                        ┌──────────────────────────┐
                        │   TestNG Suite XML       │
                        │ (regression / sanity)    │
                        │  + listeners & params    │
                        └────────────┬─────────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              ▼                      ▼                      ▼
   ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
   │  BaseTest        │   │  ExtentReport    │   │  TestAllure      │
   │ @BeforeTest      │   │  Listener        │   │  Listener        │
   │ @AfterTest       │   │  + Retry (x3)    │   │  + @Step/Attach  │
   └────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
            │                      │                      │
            ▼                      │                      │
 ┌─────────────────────┐           │                      │
 │   DriverFactory     │           │                      │
 │  • initProp()  ◄─── config/*.properties (-Denv)        │
 │  • initDriver() ──► │ OptionsManager (headless/incognito/remote)
 │  • ThreadLocal<WD>  │ WebDriverManager | RemoteWebDriver
 │  • getScreenshot() ◄┼─────────────── failure screenshots
 └────────┬────────────┘
          │
          ▼
 ┌─────────────────────┐      ┌─────────────────────┐
 │    Page Objects     │─────►│     Utilities       │
 │ LoginPage           │      │ ElementUtil (waits) │
 │ HomePage            │      │ JavaScriptUtil      │
 │ ContactsPage        │      │ ExcelUtil (POI)     │
 │ PersonDetailsPage   │      │ AppConstants        │
 │ FormPage            │      └──────────┬──────────┘
 └────────┬────────────┘                 │
          ▼                              ▼
 ┌─────────────────────┐      ┌─────────────────────┐
 │   Test Classes      │      │  crmtestdata.xlsx   │
 │ LoginPageTest       │◄─────│  (@DataProvider)    │
 │ HomePageTest        │      └─────────────────────┘
 │ ContactsPageTest    │
 │ FormPageTest        │──────► Reports: Extent + Allure + ES
 └─────────────────────┘
```

### Execution Flow

1. Maven Surefire picks the suite XML → TestNG boots and registers listeners
2. `BaseTest.@BeforeTest` loads the environment config, resolves browser/params, and creates the `WebDriver`
3. Page objects are instantiated on top of the shared driver
4. `@Test` methods execute against page flows, with data supplied by Excel-backed `@DataProvider`s
5. Listeners capture pass/fail/skip → screenshot on failure → Extent + Allure attachments → optional Elasticsearch push
6. `BaseTest.@AfterTest` closes the browser; Extent flushes `TestExecutionReport.html`

---

## 📂 Project Structure

```
CRMHybridAutomationFrameWork/
├── pom.xml                          # Maven build & dependencies
├── Jenkinsfile                      # Declarative CI/CD pipeline
├── Jenkinsfile_sample               # Placeholder pipeline
│
└── src/
    ├── main/java/com/qa/crm/
    │   ├── constants/               # AppConstants – timeouts, titles, sheet names
    │   ├── errors/                  # AppError – message catalog
    │   ├── exceptions/              # BrowserException, FrameworkException
    │   ├── factory/                 # DriverFactory + OptionsManager
    │   ├── listeners/               # Extent / Allure / Retry / Elasticsearch
    │   ├── pages/                   # Page Objects (Login, Home, Contacts,
    │   │                            #   PersonDetails, Form)
    │   └── utils/                   # ElementUtil, JavaScriptUtil, ExcelUtil
    │
    └── test/
        ├── java/com/qa/crm/
        │   ├── base/BaseTest.java   # @BeforeTest / @AfterTest lifecycle
        │   └── tests/               # LoginPageTest, HomePageTest,
        │                            #   ContactsPageTest, FormPageTest
        └── resources/
            ├── config/              # config, dev, qa, stage, uat .properties
            ├── testdata/            # crmtestdata.xlsx
            └── testrunners/         # testng_regression.xml, testng_sanity.xml
```

---

## ✅ Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17+ |
| Maven | 3.8+ |
| Git | any |
| Chrome / Firefox | latest (or let WebDriverManager download) |
| (Optional) Jenkins | 2.x with Maven + Allure + HTML Publisher plugins |
| (Optional) Selenium Grid / Selenoid | for `remote=true` execution |
| (Optional) Elasticsearch | `localhost:9200` for result push |

---

## 🚀 Getting Started

```bash
# 1. Clone the repository
git clone https://github.com/Madhusudan-1990/CRMHybridAutomationFrameWork.git
cd CRMHybridAutomationFrameWork

# 2. Configure your credentials (see Configuration section)
#    edit src/test/resources/config/<env>.config.properties

# 3. Compile the project
mvn clean compile

# 4. Run the full regression suite
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_regression.xml
```

> ⚠️ **Always run from the project root** — configuration and Excel data are loaded via working-directory-relative paths.

---

## ▶️ How to Run

### Full Regression Suite (default)

```bash
mvn clean test -U -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_regression.xml
```

### Sanity Suite on a specific environment

```bash
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_sanity.xml -Denv=stage
```

### Environment selection

```bash
mvn clean test -Denv=dev     # local, headless Chrome, incognito + highlight
mvn clean test -Denv=qa      # default environment
mvn clean test -Denv=stage   # headless CI run
mvn clean test -Denv=uat     # headless UAT
mvn clean test -Denv=prod    # production profile (config.properties)
```

| `-Denv` | Config file | Headless | Incognito | Highlight | Remote |
|---|---|---|---|---|---|
| *(none)* | `qa.config.properties` | ❌ | ❌ | ❌ | ❌ |
| `dev` | `dev.config.properties` | ✅ | ✅ | ✅ | ❌ |
| `qa` | `qa.config.properties` | ❌ | ❌ | ❌ | ❌ |
| `stage` | `stage.config.properties` | ✅ | ✅ | ✅ | ❌ |
| `uat` | `uat.config.properties` | ✅ | ✅ | ✅ | ❌ |
| `prod` | `config.properties` | ❌ | ❌ | ❌ | ❌ |

### Run a single suite in the IDE

Right-click any `testng_*.xml` → **Run as → TestNG Suite**.
*(Running a test class directly will fail — `browserversion` and `testname` parameters are supplied by the suite XML.)*

---

## 📚 Test Suites

| Suite | File | Contents |
|---|---|---|
| **Regression** | `testng_regression.xml` | Login (Chrome) · Home (Firefox) · Contacts (Chrome) · Forms (Chrome) — 4 tests, parallel, 4 threads |
| **Sanity** | `testng_sanity.xml` | Login smoke test (Chrome) — used by the Jenkins stage gate |

### Browser matrix at a glance

```
Login     ──► Chrome 125.0
Home      ──► Firefox 115.0      ← cross-browser coverage
Contacts  ──► Chrome 125.0
Forms     ──► Chrome 125.0
```

Each `<test>` block injects `browser`, `browserversion` and `testname` as TestNG parameters, which override the properties file at runtime.

---

## ⚙️ Configuration

All runtime configuration lives in `src/test/resources/config/`:

```properties
browser   = chrome                          # chrome | firefox | edge | safari | ie
url       = https://ui.cogmento.com/        # application URL
username  = ${CRM_USERNAME}                 # ← use env vars / CI secrets
password  = ${CRM_PASSWORD}                 # ← never commit real credentials

headless  = true                            # headless execution
incognito = true                            # incognito / inPrivate mode
highlight = true                            # flash elements on interaction

# Remote grid (qa profile)
remote    = false
huburl    = http://localhost:4444/wd/hub
```

> 🔐 **Security note:** credentials are currently stored in plain text in the properties files. For any real-world use, inject them through **environment variables**, **Jenkins Credentials**, or a secrets manager — and rotate the exposed values. Never log the full `Properties` object or pass credentials into Allure `@Step` templates.

### Runtime flags

| System Property | Purpose | Example |
|---|---|---|
| `-Denv` | Select environment profile | `-Denv=dev` |
| `-Dsurefire.suiteXmlFiles` | Select TestNG suite | `-Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_sanity.xml` |

---

## 📊 Reporting

Every run produces **three** complementary outputs:

| Report | Location | Highlights |
|---|---|---|
| **Extent Spark HTML** | `./reports/TestExecutionReport.html` | Pass/fail/skip with **failure screenshots**, suite + class categories, start/end time |
| **Allure Report** | `./allure-results/` (raw) | Step-by-step timeline, attachments, screenshots, `@Epic/@Feature/@Story` metadata |
| **Elasticsearch index** | `http://localhost:9200/app_v2/_doc` | Per-test `{class, description, status, executionTime}` for dashboards (optional) |

### Viewing reports

```bash
# Extent — simply open the file
open ./reports/TestExecutionReport.html

# Allure — generate & serve the HTML report
allure serve allure-results
```

Screenshots are captured automatically on failure and stored in `./screenshot/`:
```
screenshot/
└── createNewContactTest_1712345678901.png
```

---

## 🔁 CI/CD Pipeline

The repository ships with a **declarative Jenkins pipeline** (`Jenkinsfile`):

```
┌─────────────┐   ┌────────────────┐   ┌──────────────────────────┐
│   Build     │──►│  Deploy to QA  │──►│ Regression UI Automation │
│ (mvn pkg)   │   │                │   │  testng_regression.xml   │
└─────────────┘   └────────────────┘   └────────────┬─────────────┘
                                                     │
       ┌─────────────────────────────────────────────┤
       ▼                                             ▼
┌─────────────────────┐                  ┌──────────────────────┐
│ Publish Allure      │                  │ Publish Extent HTML  │
│ Report              │                  │ Report               │
└─────────────────────┘                  └──────────────────────┘
       │
       ▼
┌──────────────┐  ┌─────────────────────────┐  ┌────────────────────┐
│Deploy to Stage│─►│ Sanity Automation Test  │─►│ Publish Sanity     │
│              │  │ testng_sanity.xml       │  │ Extent Report      │
└──────────────┘  └─────────────────────────┘  └─────────┬──────────┘
                                                          ▼
                                               ┌──────────────────┐
                                               │  Deploy to PROD  │
                                               └──────────────────┘
```

**Stage details**

| Stage | Command | Publishing |
|---|---|---|
| Build | `mvn -Dmaven.test.failure.ignore=true clean package` | JUnit results + archived JAR |
| Regression | `mvn clean test -U -Dsurefire.suiteXmlFiles=…/testng_regression.xml` | Allure + Extent HTML |
| Sanity | `mvn clean test -Dsurefire.suiteXmlFiles=…/testng_sanity.xml -Denv=stage` | Extent HTML |

> The regression and sanity stages use `catchError` so a failing test suite marks the *stage* as failed while still allowing reports to publish.

---

## 🐳 Docker

The whole suite runs in containers. No local JDK, no local Chrome, no local
Selenium — which is exactly why it works the same on a laptop, on Jenkins and
in the cluster.

| File | What it does |
|---|---|
| `Dockerfile` | Two stages. Stage 1 caches the Maven dependencies and packages the jar. Stage 2 is the test runner image that gets pushed to the registry |
| `.dockerignore` | Keeps `.git`, `target/`, `reports/` and secrets out of the build context |
| `docker-compose.yml` | Wires up three services: `hub`, `chrome` and `tests` on one network |
| `Jenkinsfile_docker` | The same pipeline, but every stage runs in a container |

### Run the whole grid with one command

```bash
export CRM_USERNAME=you@example.com
export CRM_PASSWORD=secret
docker compose up --build --abort-on-container-exit
```

`docker compose up` starts the hub, waits for its healthcheck, starts the
Chrome node, and only then runs the `tests` container. The framework inside
resolves the browser at `http://selenium-hub:4444/wd/hub`, which is the
service name on the compose network.

Open <http://localhost:4444/grid/console> to see the grid, or
<http://localhost:7901> to watch the browser live over VNC.

Reports land on your host, owned by you:

```
./reports/TestExecutionReport.html    Extent
./allure-results/                     Allure raw results
./surefire-reports/                   JUnit XML for Jenkins
./screenshot/                         Failure screenshots
```

### One suite, not the whole regression

```bash
docker compose run --rm -e SUITE_XML=src/test/resources/testrunners/testng_smoke.xml tests
```

### How the config gets into the container

`DriverFactory.initProp()` loads the `.properties` file as before, then
**environment variables override it**:

| Env var | Property it sets |
|---|---|
| `ENV` | selects `qa/dev/stage/uat/prod.config.properties` |
| `BROWSER` | `browser` |
| `REMOTE` | `remote` |
| `HUB_URL` | `huburl` |
| `HEADLESS` / `INCOGNITO` / `HIGHLIGHT` | same names, lowercased |
| `CONTAINER` | adds `--no-sandbox --disable-dev-shm-usage` to Chrome |
| `USERNAME` / `PASSWORD` | `username` / `password` |

So the same image runs the regression on QA and the smoke suite on Stage by
changing two env vars, never by editing a file and rebuilding.

---

## ☸️ Kubernetes

Same containers, one step up. Everything lives in the `qa-automation`
namespace. See [`k8s/README.md`](k8s/README.md) for the full walkthrough.

| File | Object | What it does |
|---|---|---|
| `k8s/00-namespace.yaml` | Namespace | Isolates the grid from the rest of the cluster |
| `k8s/01-configmap.yaml` | ConfigMap | Injects `ENV`, `BROWSER`, `REMOTE`, `HUB_URL`, `URL` as env vars — the same ones `initProp()` reads |
| `k8s/02-secret.example.yaml` | Secret (template) | Credentials. The real `02-secret.yaml` is **gitignored** |
| `k8s/03-grid-hub.yaml` | Service + Deployment | The Grid 4 hub. The Service name `selenium-hub` is the hostname the tests use |
| `k8s/04-grid-node.yaml` | Deployment | Chrome nodes. `replicas` = parallel capacity |
| `k8s/05-test-job.yaml` | Job | Runs `mvn test` and exits 0 or 1. This is what CI triggers |
| `k8s/06-pvc.yaml` | PVC | Shared volume so reports outlive the pod |

### Build, push, run

```bash
docker build -t ghcr.io/madhusudan-1990/crm-tests:1.0 .
docker push ghcr.io/madhusudan-1990/crm-tests:1.0

kubectl apply -f k8s/00-namespace.yaml -f k8s/01-configmap.yaml -f k8s/06-pvc.yaml
kubectl apply -f k8s/03-grid-hub.yaml -f k8s/04-grid-node.yaml
kubectl apply -f k8s/05-test-job.yaml

kubectl -n qa-automation logs job/crm-regression -f
```

### Three things that catch people out

1. **`/dev/shm` is 64 MB by default** and Chrome dies with `Tab crashed`.
   `04-grid-node.yaml` mounts an `emptyDir` with `medium: Memory` and
   `sizeLimit: 2Gi` at `/dev/shm`.
2. **`fsGroup: 10001`** on the Job is what lets the non-root image user write
   into the PVC. Without it you get `Permission denied` on `/app/reports`.
3. **A Job is immutable.** `kubectl apply` on a modified Job is a silent no-op.
   Delete it first: `kubectl -n qa-automation delete job crm-regression`.

---

## 📊 Test Data Management

Test data is stored in **`src/test/resources/testdata/crmtestdata.xlsx`**:

| Sheet | Columns | Used by |
|---|---|---|
| `contacts` | FirstName, LastName, Company | `ContactsPageTest.createNewContactTest` |
| `forms` | FormNameText, IntroText, CompleteText | `FormPageTest.formSubmitPageTest` |
| `login` | *(reserved)* | — |

```java
@DataProvider(name = "getCRMTestData")
public Object[][] getCRMTestData() {
    return ExcelUtil.getTestData(AppConstants.CONTACTS_SHEET_NAME);
}
```

*Row 0 is treated as the header row; every value is read as a `String`.*

Adding data is as simple as appending rows to the sheet — **no code change required**.

---

## 🧩 Extending the Framework

### ➕ Add a new Page Object

```java
package com.qa.crm.pages;

public class DealsPage {
    private WebDriver driver;
    private ElementUtil eleUtil;

    private By createDealBtn = By.xpath("//button[@data-name='create-deal']");

    public DealsPage(WebDriver driver) {
        this.driver = driver;
        eleUtil = new ElementUtil(driver);
    }

    public DealDetailsPage clickCreateDeal() {
        eleUtil.doClick(createDealBtn, AppConstants.DEFAULT_MEDIUM_TIME_OUT);
        return new DealDetailsPage(driver);   // ← return the next page
    }
}
```

### ➕ Add a new test

```java
package com.qa.crm.tests;

@Listeners({AnnotationTransformer.class, ExtentReportListener.class})
public class DealsPageTest extends BaseTest {

    @BeforeClass
    public void login() {
        homePage = loginPage.doLogin(prop.getProperty("username"), prop.getProperty("password"));
    }

    @Test(priority = 1)
    public void createDealTest() { /* ... */ }
}
```

Then register it in the suite XML with the three required parameters:

```xml
<test name="Deals Page Test">
  <parameter name="browser"       value="chrome"/>
  <parameter name="browserversion" value="125.0"/>
  <parameter name="testname"      value="Deals Page Test"/>
  <classes>
    <class name="com.qa.crm.tests.DealsPageTest"/>
  </classes>
</test>
```

### ➕ Add a new environment

1. Create `src/test/resources/config/<name>.config.properties`
2. Add a `case "<name>":` branch in `DriverFactory.initProp()`
3. Run with `mvn clean test -Denv=<name>`

### ➕ Add a new browser

1. Add a `case` in `DriverFactory.initDriver()` with `WebDriverManager.<browser>driver().setup()`
2. Add a matching `get<Browser>Options()` in `OptionsManager`
3. (Optional) handle it in `init_remoteDriver()` for grid execution

---

## 🩹 Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Browser doesn't start | Wrong env / driver not downloaded | Use `-Denv=dev` for local headless runs; ensure internet access for WebDriverManager |
| `BrowserException: Please pass on the right browser name` | Unsupported browser value | Use `chrome`, `firefox`, `edge`, `safari` or `ie` |
| `INVALID ENV NAME` | `-Denv` typo | Use `qa`, `dev`, `stage`, `uat` or `prod` |
| Missing `browserversion` / `testname` parameter | Test class run without a suite XML | Run via `testng_*.xml` or pass `-Dsurefire.suiteXmlFiles=…` |
| `DataProvider` returns `null` | Excel sheet name mismatch or blank cells | Match sheet names to `AppConstants`; avoid empty cells |
| No report generated | Suite not flushed / wrong working dir | Run from project root; check `./reports/` |
| No Allure steps or attachments | AspectJ agent not loaded | Verify the `aspectjweaver` `-javaagent` argLine in `pom.xml` matches the dependency version |
| Test fails on timing | Page not ready yet | Prefer `ElementUtil` explicit waits over `Thread.sleep` |
| Grid connection error | `remote=true` without a hub | Start a Selenium Grid on `huburl`, or set `remote=false` |

---

## ✅ Best Practices Used

✔ **Separation of concerns** — pages contain locators & flows, tests contain assertions
✔ **No hard-coded values** — timeouts, titles and sheet names centralised in `AppConstants`
✔ **Thread-safe driver access** — `ThreadLocal` + static `DriverFactory.getDriver()`
✔ **Fluent navigation** — page methods return the next page object for chainable flows
✔ **Fail-fast errors** — descriptive custom exceptions for invalid browser/env inputs
✔ **Global retry** — flaky failures retried up to 3× through `IAnnotationTransformer`
✔ **Evidence on failure** — screenshots automatically attached to Extent *and* Allure
✔ **Environment isolation** — one properties file per environment, selected by a single flag
✔ **CI-ready** — everything runnable from a single Maven command in Jenkins

---

## 🗺️ Roadmap

- [ ] Replace `Thread.sleep` with fully explicit/fluent waits
- [ ] Migrate credentials to environment variables / secrets manager
- [ ] Introduce a real logging framework (SLF4J + Logback) instead of `System.out`
- [ ] Add API-layer validation (REST-assured) for end-to-end verification
- [ ] Add BDD (Cucumber) feature files as an optional reporting layer
- [ ] Dockerise Selenium Grid + Jenkins for containerised execution
- [ ] Add GitHub Actions workflow as a Jenkins alternative
- [ ] Upgrade Apache POI & Jackson to current, secure versions
- [ ] Introduce Maven profiles for `-Dbrowser` overrides on the command line

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch → `git checkout -b feature/my-feature`
3. Follow the existing package structure & naming conventions
4. Keep assertions in tests, keep pages assertion-free
5. Add your test class to the relevant suite XML
6. Open a Pull Request with a clear description

---

## 📜 License

This project is licensed under the MIT License — feel free to use it as a learning resource or a starting point for your own automation framework.

---

<div align="center">

**Built with ❤️ by [Madhusudan-1990](https://github.com/Madhusudan-1990)**

⭐ *If this framework helped you, consider giving it a star!*

</div>
