# skill.md — CRMHybridAutomationFrameWork

> **Basis of this document:** a static read of every file in the uploaded snapshot `CRMHybridAutomationFrameWork-main.zip` (41 files). The ZIP contains **no `.git` directory**, so commit history, branches, tags and PR conventions could **not** be inspected. Anything not evidenced in the files is marked **"Not found in the repository."** Nothing was executed; runtime behaviour statements are derived from reading code and are labelled *(unverified at runtime)* where relevant.

---

# 1. Project Identity

| Item | Finding |
|---|---|
| Repository name | `CRMHybridAutomationFrameWork` (GitHub: `Madhusudan-1990`) |
| Maven coordinates | `groupId` / `artifactId`: `SeleniumPOMTestNGFramework`, version `0.0.1-SNAPSHOT` (original project name; differs from repo name) |
| Purpose | UI test automation of the **Cogmento CRM** web app (`https://ui.cogmento.com/`, the FreeCRM UI) |
| Framework type | Selenium WebDriver + TestNG + Maven, Page Object Model, Excel data-driven tests via DataProvider. Described as "hybrid" by the repo name; in code this means POM + data-driven (Excel) + config-driven. **No keyword-driven layer exists.** |
| Application areas automated | Login, Home dashboard, Contacts (create/search/open), Forms (create) |
| Primary goals (evidenced) | Regression and sanity suites run via Maven/Jenkins, multi-environment config, optional remote (grid/Selenoid-style) execution, Extent + Allure reporting, failure screenshots, retry of failed tests, push of per-test status to Elasticsearch |
| Maturity | Early-to-intermediate learning/portfolio framework. Good skeleton (ThreadLocal driver, factory, option manager, listeners, CI file) but with notable gaps: no real logging, `Thread.sleep` waits, plaintext credentials, outdated dependencies, tiny test suite (4 test classes, ~16 test methods). See §26–27. |

---

# 2. Technology Stack

Only items found in `pom.xml` / source / `Jenkinsfile`.

| Area | Technology | Version / evidence |
|---|---|---|
| Language | Java | `maven.compiler.source/target = 17` (pom). `debug.log` shows JDK 17.0.7 on the author's machine |
| Build | Maven | Maven version **not pinned** in the repo (no `mvnw`, no wrapper files). `debug.log` shows Apache Maven 3.9.9 on Windows 10. `.mvn/jvm.config` and `.mvn/maven.config` exist but are **empty** |
| Compiler plugin | maven-compiler-plugin | 3.10.1 |
| Test runner plugin | maven-surefire-plugin | 3.0.0-M5; `forkCount=3`, `reuseForks=true`, `suiteXmlFiles=${surefire.suiteXmlFiles}`, `argLine` = AspectJ `-javaagent` |
| Packaging | maven-assembly-plugin 3.3.0 (`jar-with-dependencies`, bound to `package`); maven-deploy-plugin 2.8.2 |
| Browser automation | selenium-java | **4.31.0** |
| Test framework | TestNG | **7.11.0** (scope `compile`, not `test`) |
| WebDriver management | WebDriverManager (bonigarcia) | 5.9.2 |
| Reporting | ExtentReports | 5.0.8 (`extentreports-version` property), `ExtentSparkReporter` |
| Reporting | Allure TestNG | 2.29.1, with `aspectjweaver` 1.9.7 dependency |
| Logging libs | log4j-api / log4j-core | 2.14.1 — **declared but never used; no log4j config file exists** |
| Excel | Apache POI | `poi`, `poi-ooxml`, `poi-ooxml-schemas`, `poi-scratchpad` **3.9**; `ooxml-schemas` 1.1; `openxml4j` 1.0-beta |
| JSON | jackson-databind | 2.9.4 (used by `ResultSender`) |
| HTTP client | unirest-java | 1.4.9 (used only by `ResultSender` → Elasticsearch) |
| Other | guava 17.0; lombok 1.18.36 | Both declared; **lombok has no usages** in source. `com.google.errorprone.annotations.Keep` is imported (unused) in `LoginPageTest` |
| Transitive (not declared) | commons-io | `org.apache.commons.io.FileUtils` is used in `DriverFactory.getScreenshot`; resolved transitively, not declared in `pom.xml` |
| Remote/grid | `RemoteWebDriver` + `selenoid:options` capabilities | Code in `DriverFactory` / `OptionsManager`; grid URL only in `qa.config.properties` (`http://localhost:4444/wd/hub`). No Grid/Selenoid/Docker files in repo |
| CI/CD | Jenkins (declarative pipeline) | `Jenkinsfile` (real) and `Jenkinsfile_sample` (echo-only placeholders). Uses `bat` → Windows agent |
| Result sink | Elasticsearch HTTP endpoint | `http://localhost:9200/app_v2/_doc` hard-coded in `ResultSender` |
| Not present | YAML libs, DB/JDBC libs, REST-assured, Docker, Docker Compose, GitHub Actions, Cucumber/BDD, Log4j2/SLF4J config, Maven wrapper, README | **Not found in the repository.** |

Other pom facts: `distributionManagement` points to a local Nexus (`http://…@localhost:8081/repository/maven-snapshots/`) with credentials embedded in the URL (see §10).

---

# 3. Repository Structure

```
CRMHybridAutomationFrameWork-main/
├── .gitignore
├── .mvn/ (jvm.config, maven.config — both empty)
├── Jenkinsfile                 # real pipeline
├── Jenkinsfile_sample          # placeholder pipeline (echo only)
├── debug.log                   # committed `mvn -X` output (a compile failure from an earlier build)
├── pom.xml
└── src/
    ├── main/java/com/qa/crm/
    │   ├── constants/AppConstants.java
    │   ├── errors/AppError.java
    │   ├── exceptions/{BrowserException, FrameworkException, ElementException}.java
    │   ├── factory/{DriverFactory, OptionsManager}.java
    │   ├── listeners/{AnnotationTransformer, ExtentReportListener, Retry,
    │   │              TestAllureListener, ResultSender, TestStatus}.java
    │   ├── pages/{LoginPage, HomePage, ContactsPage, PersonDetailsPage, FormPage}.java
    │   └── utils/{ElementUtil, JavaScriptUtil, ExcelUtil}.java
    └── test/
        ├── java/com/qa/crm/
        │   ├── base/BaseTest.java
        │   └── tests/{LoginPageTest, HomePageTest, ContactsPageTest, FormPageTest}.java
        └── resources/
            ├── config/{config, dev.config, qa.config, stage.config, uat.config}.properties
            ├── testdata/crmtestdata.xlsx          # sheets: login (empty), contacts, forms
            └── testrunners/{testng_regression.xml, testng_sanity.xml}
```

Note: **no `src/main/resources`** exists; configs/data live under `src/test/resources` and are loaded by **relative file path from the working directory**, not from the classpath.

| Package / dir | Responsibility | Important classes | Depends on |
|---|---|---|---|
| `constants` | Timeouts, expected titles/headers, sheet names, sleep value | `AppConstants` | `java.util.List` only |
| `errors` | Error message strings | `AppError` (`INVALID_BROWSER_MSG`) | — |
| `exceptions` | Custom runtime exceptions | `BrowserException`, `FrameworkException` (both `extends RuntimeException`, message-only ctor). `ElementException` is an **empty class** (does not extend anything, unused) | — |
| `factory` | Driver creation, config loading, screenshot | `DriverFactory`, `OptionsManager` | WebDriverManager, Selenium, `exceptions`, `errors` |
| `listeners` | TestNG listeners, retry, reporting, ES push | see §12 | `DriverFactory`, Extent, Allure, Jackson, Unirest |
| `pages` | Page Objects | 5 page classes | `ElementUtil`, `AppConstants` |
| `utils` | Element/wait helpers, JS helpers, Excel reader | `ElementUtil`, `JavaScriptUtil`, `ExcelUtil` | Selenium, POI, `DriverFactory` (for highlight flag) |
| `test/base` | Shared TestNG setup/teardown | `BaseTest` | `DriverFactory`, all pages |
| `test/tests` | Test classes | 4 | `BaseTest`, `AppConstants`, `ExcelUtil`, listeners |
| `test/resources/config` | Per-environment properties | 5 files | read by `DriverFactory.initProp()` |
| `test/resources/testdata` | Excel data | `crmtestdata.xlsx` | read by `ExcelUtil` |
| `test/resources/testrunners` | Suite XMLs | regression, sanity | referenced by Jenkins via `-Dsurefire.suiteXmlFiles` |

Important structural quirk: framework code (pages, utils, listeners, factory) lives in **`src/main`**, tests in `src/test`. Because TestNG is declared at `compile` scope, `src/main` can import `org.testng.*`. (`debug.log` records a compile failure "package org.testng does not exist" from an earlier build, evidence that this was once a problem.)

---

# 4. Framework Architecture

Layers actually present:

| Layer | Implementation |
|---|---|
| Driver management | `DriverFactory` (`initDriver`, `getDriver`, `ThreadLocal<WebDriver> tlDriver`) + `OptionsManager` |
| Test base | `BaseTest` (`@BeforeTest setup`, `@AfterTest tearDown`) |
| Page Objects | `com.qa.crm.pages.*` — plain POM, `By` fields, **no PageFactory** |
| Utility | `ElementUtil` (actions + waits), `JavaScriptUtil`, `ExcelUtil` |
| Test | `com.qa.crm.tests.*` |
| Configuration | `.properties` files selected by `-Denv`, loaded in `DriverFactory.initProp()` |
| Reporting | `ExtentReportListener` (Extent Spark HTML), `TestAllureListener` + `@Step`/`@Attachment` (Allure) |
| Listener | `ExtentReportListener`, `TestAllureListener`, `AnnotationTransformer` (+`Retry`) |
| Data | `ExcelUtil.getTestData(sheet)` feeding `@DataProvider`s; plus one inline `@DataProvider` |
| External result sink | `ResultSender` → Elasticsearch |
| API / DB / Docker layers | **Not found in the repository.** |

Interaction summary: tests extend `BaseTest`; `BaseTest` builds `DriverFactory`, loads properties, initialises the driver and creates `LoginPage`. Page objects are constructed with a `WebDriver` and create their own `ElementUtil`. Pages return the next page object from navigation methods. Listeners are wired via `testng.xml` (and `@Listeners` on the classes) and independently reach the driver through the static `DriverFactory.getDriver()`.

---

# 5. Execution Flow (real)

```
mvn clean test -Dsurefire.suiteXmlFiles=<suite.xml> [-Denv=<env>]
 → surefire (forkCount=3, AspectJ javaagent) → TestNG suite XML
 → suite listeners: ExtentReportListener (static init creates ./reports/ + ExtentReports),
                    TestAllureListener, AnnotationTransformer (sets Retry on every @Test)
 → <test> block: parameters browser / browserversion / testname
 → BaseTest.setup()  [@BeforeTest]
      → new DriverFactory(); prop = df.initProp()          // env → properties file (default qa)
      → if browser param != null: prop.browser/browserversion/testname overwritten from XML
      → df.initDriver(prop)
           → isHighlight = prop.highlight (static)
           → OptionsManager(prop)
           → switch(browser): WebDriverManager.<x>driver().setup(); tlDriver.set(new <X>Driver(options))
                              or init_remoteDriver(...) → RemoteWebDriver(huburl, options)
           → maximize, deleteAllCookies, get(prop.url)
      → loginPage = new LoginPage(driver); softAssert = new SoftAssert()
 → Test class @BeforeClass (HomePageTest/ContactsPageTest/FormPageTest): loginPage.doLogin(username, password) → HomePage (→ navigate)
 → @Test methods → Page Object methods → ElementUtil → WebDriver
 → Listener callbacks per test:
      ExtentReportListener.onTestStart: create ExtentTest (categories = suite name + class name), store in ThreadLocal
      onTestSuccess/Failure/Skipped: ResultSender.send(status) → Elasticsearch POST; extent pass/fail/skip;
                    on failure DriverFactory.getScreenshot() → ./screenshot/<method>_<millis>.png attached to Extent
      TestAllureListener.onTestFailure: Allure @Attachment screenshot (bytes) + text log
      Retry (via AnnotationTransformer): up to 3 retries on failure
 → BaseTest.tearDown() [@AfterTest] → driver.close()
 → ExtentReportListener.onFinish → extent.flush() → ./reports/TestExecutionReport.html
 → Allure raw results in ./allure-results (Allure default location; no allure.properties in repo)
```

---

# 6. Driver Management

- **Creation:** `DriverFactory.initDriver(Properties)` — `switch` on lower-cased `browser` property: `chrome`, `firefox`, `safari`, `ie`, `edge`; anything else throws `BrowserException(AppError.INVALID_BROWSER_MSG + browserName)`.
- **Binary management:** `WebDriverManager.<browser>driver().setup()` is called on **every** `initDriver` call (local runs only).
- **Options:** `OptionsManager` builds `ChromeOptions`/`FirefoxOptions`/`EdgeOptions` from properties `headless` (`--headless --disable-gpu --window-size=1920,1080`), `incognito` (`--incognito`; `--inPrivate` for Edge) and `remote` (adds `browserName` + `selenoid:options` map: `screenResolution=1280x1024x24`, `enableVNC=true`, `name=<testname>`; Edge remote only sets `browserName`). `safari` and `ie` get **no options**.
- **Storage:** `public static ThreadLocal<WebDriver> tlDriver` in `DriverFactory`; read via static `DriverFactory.getDriver()`. `BaseTest` also keeps an instance field `driver`.
- **Local vs remote:** `remote=true` (only set in `qa.config.properties`) routes `chrome`/`firefox` to `init_remoteDriver`, which creates `RemoteWebDriver(new URL(prop.huburl), options)`. `edge` with `remote=true` is routed to `init_remoteDriver("edge")`, whose switch has **no `edge` case** → throws `BrowserException`. `safari`/`ie` ignore `remote`.
- **Post-init:** `maximize()`, `deleteAllCookies()`, `get(prop.url)`.
- **Closing:** `BaseTest.tearDown()` calls `driver.close()` (not `quit()`). `tlDriver.remove()` is **never** called.
- **Singleton:** none. **Parallel:** `parallel="tests"`, `thread-count="4"` in both suite XMLs (see §16).
- **Default environment = qa**, and the qa config has `remote=true` → a run with no `-Denv` tries to reach `http://localhost:4444/wd/hub`.
- Parameter `browserversion` is stored into `prop` but **never used** to select a browser version.

---

# 7. Page Object Model

**Pattern in use (all 5 pages):**
```java
public class XxxPage {
    private WebDriver driver;
    private ElementUtil eleUtil;
    private By someLocator = By.xpath("...");           // private By fields at top
    public XxxPage(WebDriver driver) { this.driver = driver; eleUtil = new ElementUtil(driver); }
    public NextPage doSomething(...) { eleUtil.doClick(...); return new NextPage(driver); }
}
```
- Locators: private `By` instance fields; mix of `By.xpath` (dominant), `By.name`, `By.cssSelector`, `By.linkText`. Dynamic locators by string concatenation (`ContactsPage.beforeXpath/afterXpath`, `By.linkText(contactName)`).
- **PageFactory / `@FindBy`: not used.** Page base class: **not found** (each page repeats `driver` + `eleUtil`).
- Navigation: methods return the next page (`doLogin → HomePage`, `HomePage.navigateToContactPage → ContactsPage`, `ContactsPage.selectContact → PersonDetailsPage`, `HomePage.navigateToFormPage → FormPage`, `HomePage.doSearch → ContactsPage`). The comment in `LoginPage` states the project convention: *POM methods should return something (not `void`)*; this is not uniformly followed (e.g. `ContactsPage.selectContactsByName` is `void`).
- Allure `@Step` is used on `LoginPage` methods only.
- Reusable components: none (no component classes).

**Convention for new Page Objects**
1. Package `com.qa.crm.pages`, class name `<Name>Page`.
2. Private `WebDriver driver`, private `ElementUtil eleUtil`, private `By` locators, public constructor taking `WebDriver`.
3. Use `ElementUtil` for all interactions (do **not** call `driver.findElement` directly — a few existing methods do; don't copy that).
4. Timeouts come from `AppConstants`, never literals.
5. Return the next page object (or a value) instead of `void` where possible; keep assertions out of pages.
6. Do not add `Thread.sleep`; use an `ElementUtil` wait.

---

# 8. Utilities

### `ElementUtil` (`com.qa.crm.utils`) — central element + wait helper
- **Constructor:** `ElementUtil(WebDriver)`; builds `Actions` and `JavaScriptUtil`.
- **Interaction:** `doClick(By)` / `doClick(By,int timeout)`, `doSendKeys(By,String)` (clears first), `doSendKeys(By,String,int)` (waits; does **not** clear), `doSendKeys(By,CharSequence...)`, `doSendKeys(WebElement,String)`, `doGetElementText`, `doElementGetAttribute` (`getDomAttribute`), `isElementDisplayed` (returns false on `NoSuchElementException`), counts/presence helpers (`getEelementsCount` [sic], `isElementPresent`, …), dropdown helpers (`Select`-based and non-Select), `Actions` helpers (`doActionsClick`, `doActionsSendKeys`, `doActionsSendKeysWithPause`, `pressEnterViaKeyboard`), menu helpers `ParentChildMenu` (3 overloads; contain `Thread.sleep`), generic `doSearch(searchField, suggestions, key, match)` (contains `Thread.sleep(3000)`).
- **Waits:** `waitForElementPresence/Visible` (+ interval overload), `waitForElementVisibleWithFluentFeeatures` [sic], `waitForElementAndClick`, `waitForElementsVisible/Presence`, title/URL waits (`waitForTitleIs/Contains[AndReturn]`, `waitForURLContains[AndReturn]` — return `"-1"`/`false` on timeout), alerts, frames, windows, `navigateBack`, `addThreadSleep(long)`.
- **Gotcha:** `getElement(By)` is a **plain `findElement` with no wait**; the no-timeout overloads `doClick(By)` / `doSendKeys(By,String)` therefore do not synchronize. Highlighting (`checkElementHighlight`) is triggered inside `getElement` and the `waitFor*` single-element methods when `DriverFactory.isHighlight` is `"true"`.
- **Use for:** every element interaction from pages. **Don't use:** `addThreadSleep`/`ParentChildMenu` as a substitute for proper waits in new code.
- **Depends on:** Selenium, `JavaScriptUtil`, `FrameworkException`, `DriverFactory` (static `isHighlight`), Allure `@Step`.

### `JavaScriptUtil`
Title/URL/inner text, history back/forward/refresh, zoom (`zoomChromeEdgeSafariFirefox`, `zoomFirefox`), scroll helpers, `scrollIntoView`, `drawBorder`, `flash` (10× colour flip with `Thread.sleep(20)`), `generateJsAlert`. Only `flash` is used in practice (via `ElementUtil`). Note `generateJsAlert` concatenates the message into the script (no escaping).

### `ExcelUtil`
`public static Object[][] getTestData(String sheetName)` — reads `./src/test/resources/testdata/crmtestdata.xlsx` (hard-coded path, relative to working directory); row 0 treated as header; every cell converted with `.toString()`. Uses **static** `workBook`/`sheet` fields; `FileInputStream`/workbook never closed; IO exceptions are printed and the method returns `null` (→ DataProvider failure). Blank cells → NPE.

### Not found in the repository
Dedicated Screenshot utility (screenshot lives in `DriverFactory.getScreenshot`), Configuration utility (lives in `DriverFactory.initProp`), Date utility, File utility, Random-data utility, API utility, DB utility.

---

# 9. TestNG Architecture

- **Annotations used:** `@Test` (with `priority`, `dataProvider`), `@BeforeTest`, `@AfterTest`, `@BeforeClass`, `@DataProvider`, `@Parameters`, `@Optional`, `@Listeners`.
- **Config lifecycle:** `BaseTest.setup()` = `@BeforeTest` + `@Parameters({"browser","browserversion","testname"})`; `tearDown()` = `@AfterTest`. Subclasses log in with their own `@BeforeClass`.
  - `@Optional` is applied only to the first parameter; `browserversion` and `testname` are mandatory → running a test class **without** a suite XML (e.g. directly from an IDE) will fail on missing parameters.
  - `@BeforeTest` runs once per `<test>` tag; the framework relies on **one test class per `<test>`** (as in both XMLs). Multi-class `<test>` blocks are not demonstrated in the repo.
- **Test classes:** `LoginPageTest` (loginPageTitleTest, loginPageURLTest, bellIconExist, loginTest [priority `Integer.MAX_VALUE`, runs last]), `HomePageTest` (6 tests, priorities 1–6), `ContactsPageTest` (verifyContactsPageLabelTest, createNewContactTest [Excel], searchTest [inline data]), `FormPageTest` (formSubmitPageTest [Excel]).
- **DataProviders:** defined in the same test classes; `getCRMTestData` (sheet `contacts`) and `getFormData` (sheet `forms`) call `ExcelUtil`; `getSearchData` is hard-coded (`Joe/Joe Simon`, `Bret/Bret Lee`, `Andy/Andy Flower`). Not parallel, not named.
- **Parameters:** `browser`, `browserversion`, `testname` per `<test>` in suite XMLs.
- **Groups:** **Not found** (no `groups` attribute anywhere).
- **Listeners:** declared in both suite XMLs (`ExtentReportListener`, `TestAllureListener`, `AnnotationTransformer`) **and** via `@Listeners({AnnotationTransformer, ExtentReportListener})` on each test class. Whether TestNG de-duplicates these double registrations was not verified. Note: `IAnnotationTransformer` is only honoured when registered at suite level (the XML does this); the class-level `@Listeners` entry for it is not effective.
- **Retry:** `Retry implements IRetryAnalyzer` — `maxTry = 3` (static), `count` per instance; applied to **all** tests by `AnnotationTransformer`.
- **Parallel:** `parallel="tests" thread-count="4"` in both XMLs; `verbose="4"`.
- **Suite files:** `testng_regression.xml` — 4 `<test>`s (Login: chrome 125.0; Home: **firefox** 115.0; Contacts: chrome 125.0; Forms: chrome 125.0). `testng_sanity.xml` — Login only (chrome 115.0). Both suites are named `"CRM App Test Regression Suite"` (copy/paste).

---

# 10. Configuration Management

| Item | Implementation |
|---|---|
| Files | `src/test/resources/config/{config,dev.config,qa.config,stage.config,uat.config}.properties` |
| Selection | `System.getProperty("env")` in `DriverFactory.initProp()`: `null`→**qa**; `qa`, `dev`, `stage`, `uat`; **`prod` → `config.properties`**; anything else → `FrameworkException("INVALID ENV NAME")` |
| Loading | `new FileInputStream("./src/test/resources/config/<file>")` — **working-directory dependent**; stream never closed; `FileNotFoundException`/`IOException` are printed and swallowed (returns a possibly empty `Properties`) |
| Keys | `browser`, `url`, `username`, `password`, `headless`, `incognito`, `highlight`; `remote`, `huburl` (qa only); `testname`, `browserversion` injected at runtime from suite XML |
| Browser | `browser` property, **overridden** by the suite XML `browser` parameter whenever present |
| URL | Same URL in every environment (`https://ui.cogmento.com/`) — environments do not differ in target |
| Timeouts | `AppConstants` (5 / 10 / 40 / 1200 s; sleep 5000 ms), not configurable |
| System properties | `-Denv`, `-Dsurefire.suiteXmlFiles` |
| Environment variables | **Not found.** |

**Security concerns (HIGH):**
1. A real-looking email address and a **plaintext password** are committed in `config`, `dev`, `qa`, `uat` (and as a commented-out line in `stage`).
2. `pom.xml` `distributionManagement` contains credentials inside the Nexus URL.
3. Allure `@Step` templates print arguments: `LoginPage.doLogin` (`"…username : {0} and password : {1}"`), `ElementUtil.doSendKeys` (`"entering value : {1}…"`), `DriverFactory.initDriver` (`"…prop : {0}"` → the whole `Properties`, including password) → **credentials land in Allure reports**.
4. `stage.config.properties` has **no active password**, so `-Denv=stage` yields `null` → `doSendKeys(..., null)` → `IllegalArgumentException` (the Jenkins sanity stage uses `-Denv=stage`).

*AI agents: never copy these values into new files, logs, docs or commits; treat them as compromised and recommend rotation.*

---

# 11. Test Data Management

| Mechanism | Where |
|---|---|
| Excel via POI | `crmtestdata.xlsx`: sheet `login` (empty, unused), `contacts` (FirstName, LastName, Company; 3 rows: Joe Simon/Amazon, Bret Lee/Flipkart, Andy Flower/Google), `forms` (FormNameText, IntroText, CompleteText; 3 rows: TOM, David, Mukta) |
| Hard-coded DataProvider | `ContactsPageTest.getSearchData` |
| Hard-coded constants | `AppConstants` (expected titles, header list/count, suffix `" [Active]"`) |
| Properties | credentials/URL (see §10) |
| JSON / CSV / API-generated / DB data | **Not found.** |

Coupling to be aware of: `searchTest` expects contacts (`Joe Simon`, `Bret Lee`, `Andy Flower`) that `createNewContactTest` (priority 2) creates from Excel; there is **no cleanup** of data created in the CRM, so reruns create duplicates.

---

# 12. Reporting

**Extent (`ExtentReportListener implements ITestListener`)**
- Static init: creates `./reports/`, `ExtentReports` + `ExtentSparkReporter("./reports/TestExecutionReport.html")`, report name `"Open Cart Automation Test Results"`, system info `System=MAC`, `Author=Naveen AutomationLabs`, `Build#=1.1`, `Team=OpenCart QA Team`, `ENV NAME=System.getProperty("env")` — **stale values copied from another project** (and `env` may be `null`).
- `onTestStart`: `extent.createTest(methodName, description)`; categories = suite name + class name; stored in `public static ThreadLocal<ExtentTest> test`.
- `onTestSuccess` → `pass("Test passed")`; `onTestFailure` → `fail("Test failed")` + `fail(throwable, screenshot)` where the screenshot path is from `DriverFactory.getScreenshot(methodName)` (`<user.dir>/screenshot/<method>_<millis>.png`); `onTestSkipped` → `skip`. Pass/skip screenshot lines are commented out.
- Start/end times set from TestNG millis. `onFinish(ITestContext)` → `extent.flush()` and `test.remove()` (flushed once per `<test>` block).
- Output: `./reports/TestExecutionReport.html` — one fixed filename (overwritten each run). Jenkins publishes it with `publishHTML`.

**Allure (`TestAllureListener` + annotations)**
- `@Attachment` screenshot (bytes) and text log on failure; other callbacks only print.
- Requires the AspectJ agent (surefire `argLine`, see §24 for a version mismatch).
- Annotations (`@Epic/@Feature/@Story/@Owner/@Link/@Issue/@Severity/@Description`) are used only in `LoginPageTest`; `@Step` in `LoginPage`, `ElementUtil`, `DriverFactory`, `BaseTest`.
- Raw results: `allure-results/` (Allure's default; no `allure.properties`). Jenkins calls the Allure plugin on `allure-results`.

**Elasticsearch push (`ResultSender` + `TestStatus`)**
- Called from `ExtentReportListener.sendStatus` on pass/fail/skip; POSTs JSON `{testClass, description, status, executionTime}` to `http://localhost:9200/app_v2/_doc` via Unirest; exceptions are printed and swallowed.

---

# 13. Logging

- Log4j 2.14.1 jars are declared, but **no `log4j2.xml`/`log4j2.properties` exists and no class imports `org.apache.logging.log4j`**.
- Actual "logging" = `System.out.println` (55 occurrences in `src`) and `e.printStackTrace()` (11).
- Log levels, appenders, log files, rolling logs, logger creation: **Not found in the repository.**
- `debug.log` in the repo root is a captured `mvn -X` output, not framework logging. `.gitignore` lists `application.log` and `activityLog.log` but nothing writes them.
- Convention to follow today: existing code prints with `System.out.println`. Introducing a real logger is a recommended improvement (§28), not the current convention.

---

# 14. Exception Handling

- **Custom exceptions:** `BrowserException` (invalid browser), `FrameworkException` (invalid env, "No Suggestions FOUND"). `ElementException` is an empty placeholder.
- **Pattern:** `System.out.println(msg)` then `throw new <X>Exception(msg)`.
- **Swallow-and-print:** `initProp` (file errors), `init_remoteDriver` (`MalformedURLException` → driver stays null → later NPE), `ExcelUtil` (returns `null`), `ResultSender`, `FormPage.navigateToFormPage`/`HomePage.navigateToFormPage` (`InterruptedException`), `DriverFactory.getScreenshot` (`IOException`).
- **Selenium exceptions:** `TimeoutException` is caught in title/URL/window waits and converted to `"-1"` / `false`; `NoSuchElementException` caught in `isElementDisplayed`; the fluent-wait helpers ignore `NoSuchElement/StaleElementReference/ElementNotInteractable`. Elsewhere Selenium exceptions propagate to TestNG.
- **Retry:** §9. **Screenshot on failure:** Extent (file) and Allure (bytes) — both call `DriverFactory.getDriver()`; if the driver is `null` (init failure) the Extent path will NPE. **Fail-fast:** not configured (`SoftAssert` used only in `searchTest`).

---

# 15. Synchronization Strategy

- **Implicit wait:** not set anywhere. **Page-load/script timeouts:** not set.
- **Explicit waits:** `WebDriverWait` + `ExpectedConditions` (presence, visibility, all-visible, clickable, title, URL, alert, frame, window count) in `ElementUtil`; timeouts from `AppConstants` (`DEFAULT_SHORT_TIME_OUT=5`, `MEDIUM=10`, `LONG=40`, `ULTRA_LONG=1200`).
- **Fluent wait:** two helpers (`waitForElementVisibleWithFluentFeeatures`, `waitForAlertUsingFluentWaitAndSwitch`) — not used by any page.
- **Thread.sleep (hard waits):** `ElementUtil.addThreadSleep` (5000 ms via `DEFAULT_THREAD_SLEEP_TIME` in `ContactsPage.createNewContact`, `FormPage.createdFormHeader`, `FormPage.navigateToFormPage`), `pressEnterViaKeyboard(5000)` pause in `HomePage.doSearch`, `ParentChildMenu` (1.0–1.5 s), generic `doSearch` (3 s), `JavaScriptUtil.flash` (20 ms).
- **Unsynchronized interactions:** `doClick(By)`, `doSendKeys(By,String)`, `doGetElementText`, `isElementDisplayed` use `findElement` directly. `LoginPage.doLogin` and many form interactions rely on this.
- **Stale risk:** `ContactsPage.contactSelect` caches a `WebElement` field.
- Recommendations come after documentation — see §26/§28.

---

# 16. Parallel Execution & Thread Safety

**Supported by configuration:** TestNG `parallel="tests"`, `thread-count="4"`; surefire `forkCount=3`. `tlDriver` is a `ThreadLocal` and Extent's current test is a `ThreadLocal<ExtentTest>`.

**Why it largely works today:** each `<test>` has exactly one test class, so each thread owns its own `BaseTest` instance (driver, `prop`, page objects, `softAssert`).

**Thread-safety problems found:**
- `DriverFactory.isHighlight` is `public static` and overwritten per `initDriver` call.
- `ExcelUtil` uses `static` `workBook` / `sheet`.
- `ExtentReportListener.testStatus` is an **instance field** on a listener shared by all threads; it is replaced in `onTestStart`, so concurrent tests can send each other's class/description/status to Elasticsearch.
- `tlDriver.remove()` never called; `driver.close()` instead of `quit()` can leave driver processes.
- All suites write to the **same** `./reports/TestExecutionReport.html`, `./screenshot/`, `./allure-results/`; with `forkCount=3` (separate JVMs) the shared report file is a likely clobbering point *(interaction of forkCount with a single suite XML not verified)*.
- Shared remote state: all tests log in with one account and create persistent CRM records (name collisions possible).
- `Retry.count` is per instance, fine.

**Verdict:** safe only under the current "one class per `<test>`, distinct data" assumptions; not safe to extend casually to class-/method-level parallelism. New code must use `DriverFactory.getDriver()`/instance fields — never new statics.

---

# 17. Coding Standards (derived)

- **Packages:** lowercase, `com.qa.crm.<layer>`.
- **Classes:** PascalCase; pages `…Page`, tests `…PageTest`/`…Test`, utils `…Util`, exceptions `…Exception`.
- **Methods:** camelCase; prefixes `do…` (actions: `doClick`, `doSendKeys`, `doLogin`, `doSearch`), `get…`, `is…Exists`/`isElementDisplayed`, `waitFor…`, `verify…`, `navigateTo…`; tests end with `Test`. Exceptions to the rule: `ParentChildMenu` (capital P), typos `getEelementsCount`, `waitForElementVisibleWithFluentFeeatures`, `getcontactCount` — **do not rename without updating all callers; do not replicate typos.**
- **Fields:** camelCase; locators named by role (`loginBtn`, `userName`, `firstName`); page fields `driver`, `eleUtil`.
- **Constants:** `UPPER_SNAKE_CASE` in `AppConstants` (`public static final`), grouped with `//*****` banners.
- **Style:** Allman braces in many classes (opening brace on next line) mixed with K&R in `ElementUtil`; tabs for indentation; Javadoc on a handful of methods; `// TODO Auto-generated catch block` left in several places.
- **Exceptions:** custom `RuntimeException`s with message-only constructors; print-then-throw.
- **Logging:** `System.out.println`.
- **Asserts:** `org.testng.Assert` in tests; `SoftAssert` from `BaseTest` for multi-assert tests (`assertAll()` called explicitly). Pages contain no assertions.
- **Imports:** several unused (`java.awt.desktop.AppHiddenEvent`, `com.google.errorprone.annotations.Keep`, `org.openqa.selenium.By` in test classes).

---

# 18. Design Patterns (only those evidenced)

| Pattern | Where | Notes |
|---|---|---|
| Page Object Model | `pages/*` | Methods return next page objects (fluent-ish navigation) |
| Factory (simple) | `DriverFactory.initDriver` (browser switch), `OptionsManager` | Not an abstract factory |
| ThreadLocal | `DriverFactory.tlDriver`, `ExtentReportListener.test` | Per-thread driver / ExtentTest |
| Facade-style helper | `ElementUtil` wrapping Selenium | Not formally a Facade class |
| Template-method-like inheritance | `BaseTest` → test classes | TestNG lifecycle in base class |
| Observer (TestNG listeners) | `ITestListener` implementations | Framework-provided mechanism |
| Data-driven | `@DataProvider` + `ExcelUtil` | |
| Singleton, Builder, Strategy, Dependency Injection | **Not found** (static members exist but are not Singleton implementations) |

---

# 19. Dependency Map

```
suite.xml ──► Listeners (Extent, Allure, AnnotationTransformer→Retry)
   │
   ▼
XxxPageTest ──extends──► BaseTest ──► DriverFactory ──► OptionsManager
   │                         │             │               │
   │                         │             ├─► config/<env>.properties
   │                         │             └─► WebDriverManager / RemoteWebDriver ► WebDriver (ThreadLocal)
   │                         └─► LoginPage
   ├─► Page Objects (Login/Home/Contacts/PersonDetails/Form) ─► ElementUtil ─► JavaScriptUtil ─► WebDriver
   │                                                         └─► AppConstants
   └─► ExcelUtil ─► crmtestdata.xlsx   (DataProviders)

ExtentReportListener ─► DriverFactory.getScreenshot ─► ./screenshot
                     ─► ResultSender ─► Elasticsearch (localhost:9200)
TestAllureListener   ─► DriverFactory.getDriver ─► Allure attachment
ElementUtil ─► DriverFactory.isHighlight (static)
```

---

# 20. Common Development Tasks

**Add a Page Object:** copy the pattern in §7 into `com.qa.crm.pages`; use `ElementUtil`; add timeouts/expected strings to `AppConstants`; return the next page from navigation methods; add a field in `BaseTest` only if tests of several classes need it (existing convention: `protected` page fields in `BaseTest`).

**Add a test:** create `<Name>PageTest extends BaseTest` in `com.qa.crm.tests`; add `@Listeners({AnnotationTransformer.class, ExtentReportListener.class})` (existing convention) and a `@BeforeClass` login if the page needs a session; **add a new `<test>` block** to the suite XML(s) with `browser`, `browserversion`, `testname` parameters (required by `BaseTest.setup`). Keep one class per `<test>`.

**Add a locator:** add a `private By` field in the page class that owns the screen; prefer `name`/`id`/CSS over absolute XPath; do not duplicate a locator already defined in another page (e.g. `formIcon`/`formTab` exist in both `HomePage` and `FormPage`) — move shared behaviour rather than copy.

**Add a utility:** first search `ElementUtil`/`JavaScriptUtil`. Put new element/wait helpers into `ElementUtil` following `do…`/`waitFor…` naming; new generic helpers go in `com.qa.crm.utils`. Don't add `static` mutable state.

**Add a DataProvider:** in the test class; Excel-backed → add a sheet to `crmtestdata.xlsx`, a constant in `AppConstants` (`…_SHEET_NAME`), and `return ExcelUtil.getTestData(AppConstants.<NAME>)`. Sheet row 0 must be a header; avoid blank cells; remember all values arrive as `String` (numbers like `5` become `"5.0"`).

**Add a browser:** extend the `switch` in `DriverFactory.initDriver`, add `get<X>Options()` to `OptionsManager`, add a case to `init_remoteDriver` if remote is needed, and add `WebDriverManager` setup. Update `AppError`/docs.

**Add an environment:** add `<env>.config.properties` under `src/test/resources/config` and a `case` in `DriverFactory.initProp()`. Use placeholders/secret injection for credentials — do not commit passwords.

**Add a listener:** `com.qa.crm.listeners`, implement TestNG interface, register it in **both** suite XMLs (don't rely only on `@Listeners`); keep state `ThreadLocal`/local, not instance fields.

**Add a report feature:** extend `ExtentReportListener` (e.g. screenshots on pass) or Allure annotations; keep `ThreadLocal<ExtentTest>` usage; remove stale hard-coded system info while there.

**Debug a failed test:** open `reports/TestExecutionReport.html` (stack + screenshot), `screenshot/`, Allure results, console output (framework prints are the only logs); reproduce with the sanity suite; check `-Denv` and whether `remote=true` applies (§24).

---

# 21. How to Run the Framework

Commands **evidenced** by `Jenkinsfile`/code comments:

```bash
# Compile
mvn clean compile            # standard Maven; not scripted in the repo
# Regression suite (Jenkins)
mvn clean test -U -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_regression.xml
# Sanity suite on stage (Jenkins)
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_sanity.xml -Denv=stage
# Package (Jenkins stage 1, different repo checkout)
mvn -Dmaven.test.failure.ignore=true clean package
# Comment in DriverFactory
mvn clean install -Denv="qa"
```

- **Suite file is mandatory:** the pom sets `<suiteXmlFile>${surefire.suiteXmlFiles}</suiteXmlFile>` with **no default** property, so a plain `mvn test` has no suite to run *(behaviour unverified)*.
- **Environment:** `-Denv=qa|dev|stage|uat|prod` (default `qa`, which has `remote=true`; a Selenium Grid/Selenoid must be listening on `localhost:4444` or switch env, e.g. `-Denv=dev` for local headless Chrome).
- **Browser:** chosen by the suite XML `browser` parameter (overrides properties). There is **no `-Dbrowser`** support.
- **Single test / class / groups:** no documented mechanism. `-Dtest=…` would bypass the suite XML and `BaseTest` then lacks its mandatory parameters; groups are not defined. Safest approach: create a temporary suite XML with one `<test>` and pass it via `-Dsurefire.suiteXmlFiles`.
- **Run with TestNG directly (CLI/IDE):** Not found in the repository (must run from a suite XML to supply parameters).
- Allure report generation commands: **Not found in the repository** (Jenkins plugin used).
- Working directory must be the project root (relative file paths).

---

# 22. Git Workflow

Repository history is **not available** in the provided snapshot, so the real branch/commit/PR conventions are **Not found in the repository.** Evidence available: `Jenkinsfile` checks out branch `main` (one stage checks out a different repo, `SampleDevRepoForJenkinsPipelineBuild`); `.gitignore` excludes `target/`, `reports/`, `screenshot/`, `allure-results/`, `test-output/`, IDE folders and `*.jar`.

Recommended (not currently evidenced):
- Branches: `feature/<ticket>-<short-name>`, `bugfix/…`, `chore/…` off `main`.
- Commits: small, imperative subject (≤72 chars), one concern each; no secrets, no generated files (`debug.log` should not be committed).
- PRs into `main` with at least one reviewer; PR checklist = suite passes, no new `Thread.sleep`, no credentials, no unrelated files.
- Merge: squash-merge for features to keep history linear.
- Do not modify the repository from automated analysis; propose changes in PRs.

---

# 23. AI Coding-Agent Rules

1. **Inspect first.** Read the target class and its callers (`grep` for usages) before editing; before deleting/refactoring anything, search for usages across `src/main` and `src/test`.
2. **Reuse before creating.** Look in `ElementUtil`, `JavaScriptUtil`, `AppConstants`, existing pages. Do not duplicate functionality (e.g. don't add another navigation helper if `HomePage` has one).
3. **Preserve architecture:** keep `com.qa.crm.{constants,errors,exceptions,factory,listeners,pages,utils}` in `src/main` and `base`/`tests` in `src/test`. No new design patterns, no PageFactory migration, no BDD layer without explicit justification.
4. **Follow conventions (§17):** naming, `do…`/`waitFor…`, private `By` fields, `AppConstants` for timeouts/strings, exception style (custom `RuntimeException`), reporting style (Extent `ThreadLocal`, Allure `@Step`).
5. **Logging:** the repo currently uses `System.out.println`; match it unless the task is explicitly "introduce logging". If adding a logger, don't leave Log4j 2.14.1 (see §26).
6. **Dependencies:** check `pom.xml` first; add none unless necessary and compatible (see version conflicts in §26).
7. **Secrets:** never hard-code or echo credentials/URLs of environments; never log `prop`; mask arguments in `@Step`. Do not copy values from the existing `.properties` files.
8. **Thread safety:** all new state must be instance-level or `ThreadLocal`; use `DriverFactory.getDriver()`; no new `static` mutables; don't share `Properties`, workbooks or `ExtentTest` across threads.
9. **Synchronization:** never add `Thread.sleep`; use `ElementUtil` waits; prefer the `…(By, int timeout)` overloads.
10. **Layer discipline:** Page Objects = page behaviour only, no assertions; tests = scenarios + assertions; utilities = reusable, no page-specific locators.
11. **Scope:** change only the files required; no unrelated reformatting, renames (typos exist — leave them unless asked), or import cleanups outside the touched lines.
12. **Compatibility:** keep public method signatures; if one must change, update all callers in the same change.
13. **Config contract:** a new property needs entries in **all** env files (or a safe default in code); `qa` uniquely sets `remote`/`huburl`.
14. **Suites:** a new test class must be added to the suite XMLs with the three required `<parameter>`s, one class per `<test>`.
15. **Explain architectural changes** (why, alternatives, impact on Jenkins/suites).
16. **Don't "fix" by retrying:** `Retry` already re-runs failures 3×; investigate root cause.

---

# 24. Debugging Guide (repository-specific)

| Symptom | Likely causes in THIS repo |
|---|---|
| Browser doesn't start (default run) | No `-Denv` → `qa` → `remote=true` → needs grid at `http://localhost:4444/wd/hub`; use `-Denv=dev`/`uat` for local. Also `headless=true` in dev/stage/uat |
| Driver init failure / `NullPointerException` on `getDriver()` | `MalformedURLException` swallowed in `init_remoteDriver`; properties file not found (relative path, wrong working dir) → empty `Properties` → `browser` null NPE; `WebDriverManager` needs internet |
| `BrowserException: Please pass on the right browser name` | Browser value not in `chrome/firefox/safari/ie/edge`; or `edge` with `remote=true` (no remote edge case) |
| Missing `browserversion`/`testname` parameter error | Running a class without the suite XML (`@Parameters` not optional beyond `browser`) |
| `INVALID ENV NAME` | `-Denv` value outside qa/dev/stage/uat/prod |
| Login `IllegalArgumentException` (null keys) | `stage` env: password commented out → `null`; see also the empty-string workaround in `LoginPage.doLogin` |
| `NoSuchElementException` | Unsynchronised overloads (`doClick(By)`, `doSendKeys(By,String)`, `getElement`) hit before the page renders; fragile absolute XPaths/class-text locators (`//span[@class='selectable ']`, `//button[@class='ui linkedin button']`) |
| `TimeoutException` | Waits use 5 s (`SHORT`) by default; title/URL waits swallow it and return `"-1"`, so assertion shows `-1` instead |
| `StaleElementReferenceException` | Cached `ContactsPage.contactSelect`; re-rendered React-style lists after `Thread.sleep`-based flows |
| TestNG test not executing | Not listed in suite XML; no suite passed (`${surefire.suiteXmlFiles}` unresolved); `-Dtest` overriding suite; method priorities/`dataProvider` null |
| DataProvider failure | `ExcelUtil` returned `null` (path relative to cwd, missing sheet name, blank cell NPE); sheet names must match `AppConstants` (`contacts`, `forms`) |
| Listener not firing / duplicated | Check both suite XML `<listeners>` and class-level `@Listeners`; `AnnotationTransformer` works only from XML |
| Report not generated | `extent.flush()` only in `onFinish`; JVM killed/forks (`forkCount=3`) overwriting `./reports/`; run directory differs from project root |
| Screenshot not captured | Driver null at failure time; screenshots only on failure; saved under `<user.dir>/screenshot/`; Allure screenshot needs the AspectJ agent |
| Allure shows no steps/attachments | AspectJ agent not loaded: surefire `argLine` uses `${aspectj.version}` = **1.9.5** while the declared dependency is **1.9.7** → the agent jar path may not exist in `~/.m2` |
| Maven build failure | `org.testng` not found in `src/main` if TestNG scope is changed to `test` (seen in `debug.log`); old `ooxml-schemas`/`openxml4j` conflicts; Guava 17 / Jackson 2.9.4 older than what Selenium 4.31 expects (classpath conflicts) |
| Configuration not loading | Relative `./src/test/resources/config/...` path; running from IDE with a different working directory |
| Elasticsearch errors in console | `localhost:9200` not running — swallowed `printStackTrace` per test (and adds latency) |
| Passwords visible in Allure | `@Step` templates print arguments (§10) |

---

# 25. Current Strengths

- `ThreadLocal<WebDriver>` accessed through a static `getDriver()`, giving listeners and utilities one consistent way to reach the active driver.
- Clear split of browser creation (`DriverFactory`) from option construction (`OptionsManager`), including a ready remote/Selenoid capability path.
- Environment selection via `-Denv` with fail-fast `FrameworkException` for unknown values.
- Pages return the next page object, making flows readable; `ElementUtil` centralises Selenium calls (and optional highlight) so locators and waits are not scattered.
- A fairly complete `ElementUtil` wait toolbox (visibility, presence, title/URL, alert, frame, window, fluent).
- Dual reporting (Extent + Allure) with failure screenshots in both, and Extent `ThreadLocal` for per-thread tests.
- Global retry applied through `IAnnotationTransformer` rather than per-test annotations.
- Excel-driven DataProviders with sheet names kept as constants.
- A working Jenkins pipeline with regression + sanity suites, report publishing and per-environment parameterisation.

---

# 26. Current Technical Debt

| # | Problem | Evidence | Impact | Suggested improvement | Priority |
|---|---|---|---|---|---|
| 1 | Plaintext credentials committed | `config/*.properties` (4 files + commented line in stage) | Account exposure; also a public-repo risk | Remove from history, rotate, inject via env vars/Jenkins credentials | HIGH |
| 2 | Credentials in Nexus URL | `pom.xml` `distributionManagement` | Secret in VCS | Use `settings.xml` server ids | HIGH |
| 3 | Passwords/props leaked into Allure | `@Step` on `doLogin`, `doSendKeys`, `initDriver` | Secrets in reports/Jenkins | Mask arguments; drop `{0}`/`{1}` for sensitive values | HIGH |
| 4 | Vulnerable/ancient dependencies | log4j 2.14.1 (Log4Shell-affected line), POI 3.9, Jackson 2.9.4, Guava 17, Unirest 1.4.9, `openxml4j` 1.0-beta, `ooxml-schemas` 1.1 | Security + classpath conflicts with Selenium 4.31 | Upgrade (log4j ≥ 2.17.x or SLF4J/Logback, POI 5.x single `poi-ooxml`, current Jackson/Guava), remove unused | HIGH |
| 5 | Hard sleeps | `addThreadSleep(5000)` ×3, `pressEnterViaKeyboard(5000)`, `ParentChildMenu`, `doSearch` | Slow, flaky | Replace with explicit waits on a concrete condition | HIGH |
| 6 | Default env (`qa`) forces remote grid | `qa.config.properties` `remote=true`; default branch in `initProp` | New users' runs fail | Default to a local env or document; make `remote` explicit via `-D` | MEDIUM |
| 7 | `driver.close()` instead of `quit()`; `tlDriver.remove()` missing | `BaseTest.tearDown` | Orphan driver processes; ThreadLocal leakage | `quit()` + `remove()` in teardown | MEDIUM |
| 8 | Unsynchronised core helpers | `getElement`, `doClick(By)`, `doSendKeys(By,String)` use bare `findElement` | Flaky tests | Route through waits (clickable/visible) | MEDIUM |
| 9 | Log4j declared but unused; `System.out` everywhere | no config, 55 prints, 11 `printStackTrace` | No log levels/files/diagnostics | Introduce a logger + config, replace prints | MEDIUM |
| 10 | Surefire/AspectJ version mismatch | `aspectj.version=1.9.5` vs dependency `1.9.7` | Allure `@Step`/`@Attachment` may not weave | Single property for both | MEDIUM |
| 11 | No default suite for Maven | `${surefire.suiteXmlFiles}` undefined | `mvn test` fails/ambiguous | Add default property/profile | MEDIUM |
| 12 | Stale copy/paste in report | "Open Cart…", "Naveen AutomationLabs", `System=MAC`, both suites named "…Regression Suite" | Misleading reports | Make configurable/correct | LOW |
| 13 | Thread-safety hazards | static `isHighlight`, static POI fields, shared `testStatus` in listener, shared report path across forks | Data races, wrong status sent, overwritten reports | ThreadLocal/instance state; per-run report names | MEDIUM |
| 14 | `ExcelUtil` robustness | no stream close, `.toString()`, NPE on blanks, swallowed exceptions, hard-coded path | Silent empty data, resource leak | try-with-resources, `DataFormatter`, throw `FrameworkException` | MEDIUM |
| 15 | Swallowed exceptions | `init_remoteDriver`, `initProp`, `ResultSender` | Hidden root causes, later NPEs | Throw `FrameworkException` with cause | MEDIUM |
| 16 | Unused/dead code | `ElementException` (empty), `lombok`, `login` sheet, unused imports, `PersonDetailsPage.contactsIcon`, `browserversion` | Noise | Remove or implement | LOW |
| 17 | Duplicated locators/logic | `formIcon`/`formTab` in `HomePage` & `FormPage`; identical `addBtn`/`saveBtn`; repeated `driver`+`eleUtil` in every page | Maintenance cost | Base page / shared component | LOW |
| 18 | Fragile locators | class-text XPaths with trailing spaces, absolute-ish paths | Breakage on UI change | Stable attributes (`name`, `data-*`, CSS) | LOW |
| 19 | Weak tests | `createNewContactTest` has no assertion; data created without cleanup; inter-test data dependency; `FormPage.addForm()` result unchecked | False confidence, rerun duplicates | Add assertions + cleanup/API setup | MEDIUM |
| 20 | Elasticsearch push in the test path | `ResultSender` hard-coded localhost, synchronous per test | Latency/noise when ES absent | Make optional via config, async | LOW |
| 21 | Generated artefact committed | `debug.log` (mvn -X) | Repo noise | Delete + `.gitignore` | LOW |
| 22 | TestNG at `compile` scope; framework in `src/main` | `pom.xml` | Test deps leak into main artifact; assembly jar includes TestNG | Move framework to `src/test` or a separate module, or keep scope deliberately | LOW |
| 23 | Config/data loaded by relative filesystem path | `FileInputStream("./src/test/resources/…")` | Breaks when cwd differs | Classpath loading | LOW |

---

# 27. Enterprise Readiness Gap Analysis

| Area | Factual observation |
|---|---|
| Parallel execution | Configured (`parallel="tests"`, 4 threads; `forkCount=3`) but only per-`<test>`; shared statics and shared report/screenshot paths exist (§16) |
| Thread safety | Driver and ExtentTest are ThreadLocal; listener state, `isHighlight`, `ExcelUtil` are not |
| Cross-browser | Code supports chrome/firefox/edge/safari/ie; suites use chrome and firefox (Home Page Test runs firefox 115.0); `browserversion` parameter is unused |
| Grid | `RemoteWebDriver` path exists for chrome/firefox with Selenoid-style capabilities; no grid config/compose in repo |
| Cloud execution | Not found (no BrowserStack/LambdaTest/Sauce capabilities) |
| Docker | Not found |
| CI/CD | Jenkinsfile with build, regression, sanity, report publishing; deploy stages are `echo` placeholders; Windows `bat`; no PR/branch triggers defined |
| API integration | Not found as testing capability (Unirest only posts results to Elasticsearch) |
| Database integration | Not found |
| Reporting | Extent + Allure + failure screenshots; single fixed report filename; stale metadata |
| Logging | Not implemented (print statements) |
| Test data | Excel + hard-coded; no data cleanup/API seeding |
| Configuration | Per-env properties; same URL in all envs; relative-path loading |
| Secrets management | None; plaintext in repo |
| Retry strategy | Global 3 retries via transformer; no retry-reporting distinction |
| Failure diagnostics | Screenshots yes; no browser console/network logs, page source, or video (except Selenoid VNC flag); no structured logs |
| Code quality | Typos in public APIs, unused imports, commented code, `TODO` stubs; no static analysis, no unit tests of the framework, no formatter config |
| Maintainability | Small, readable codebase; duplicated locators/boilerplate; hard waits; no README/docs |

---

# 28. Recommended Roadmap

**Phase 1 – Stabilization**
- Remove/rotate credentials, mask Allure steps, remove `debug.log`, fix Nexus URL credentials.
- Upgrade/removal of vulnerable & conflicting dependencies (log4j, POI, Jackson, Guava, Unirest, openxml4j/ooxml-schemas); remove lombok if unused.
- Replace `Thread.sleep` with explicit waits; make `getElement`/`doClick`/`doSendKeys` synchronized.
- `quit()` + `tlDriver.remove()` in teardown; fix `aspectj.version` mismatch; add default suite property.
- Fix `stage` password handling and default env behaviour.

**Phase 2 – Framework Improvements**
- Real logging (single logger style, replace prints), `FrameworkException` with causes instead of swallowing.
- `BasePage` to remove repeated boilerplate; dedupe locators; fix typos via deliberate rename PR.
- `ExcelUtil` hardening (closing streams, `DataFormatter`, explicit errors); configurable paths via classpath.
- Test quality: assertions in `createNewContactTest`, data cleanup, remove inter-test coupling; use TestNG `groups` (smoke/regression).

**Phase 3 – Parallel/Distributed Execution**
- Remove statics (`isHighlight`, POI fields), make listener state thread-confined, unique report/screenshot names per run, decide between surefire `forkCount` and TestNG parallelism, support method/class-level parallel.
- Validate Grid/Selenoid setup with the existing remote branch; add `edge` remote case.

**Phase 4 – CI/CD**
- Parameterised Jenkins job (`env`, `suite`, `browser`), credentials via Jenkins store, real deploy/health stages, branch/PR triggers, publish Allure history, fail the build on test failures where appropriate (remove `maven.test.failure.ignore` for gating).

**Phase 5 – Docker/Cloud**
- Dockerfile + compose (Selenium Grid or Selenoid), containerised Jenkins agents, optional cloud-provider capabilities.

**Phase 6 – Enterprise Enhancements**
- API-based test data setup/teardown, DB validation if needed, secrets manager, richer diagnostics (console/network logs, video), flaky-test analytics (the Elasticsearch feed can be formalised), code-quality gates (static analysis, formatter), README/docs.

---

# 29. Senior SDET Interview Context

Use only what the code supports.

- **Explain your framework:** Java 17 / Maven / Selenium 4.31 / TestNG 7.11 framework for Cogmento CRM; Page Object Model with a shared `ElementUtil`; `DriverFactory` + `OptionsManager`; per-environment properties; Excel DataProviders; Extent and Allure reporting; Jenkins pipeline running regression and sanity suites.
- **Why this architecture?** Separation of concerns: tests → pages → utility → driver; factory isolates browser specifics; listeners keep reporting out of tests; properties keep environment values out of code. (Honest note: it grew from a learning/course-style base — stale "OpenCart" strings remain in the report listener.)
- **How is WebDriver managed?** Created in `DriverFactory.initDriver` by browser switch, WebDriverManager for local binaries, `RemoteWebDriver` for grid; stored in `ThreadLocal`, retrieved via `getDriver()`; closed in `@AfterTest` (limitation: `close()` not `quit()`, no `remove()`).
- **Parallel execution?** TestNG `parallel="tests"` with 4 threads; each `<test>` has one class, so each thread has its own driver/page objects. Limitations: some statics and shared report paths; surefire also sets `forkCount=3`.
- **Failures?** Global retry (up to 3) via `IAnnotationTransformer`; Extent failure entry with stack trace and screenshot, Allure screenshot attachment; failure status also pushed to Elasticsearch.
- **Reports?** `ExtentSparkReporter` to `./reports/TestExecutionReport.html` (created at listener class init, flushed on `onFinish`) and Allure results published through the Jenkins plugin.
- **Test data?** Excel via POI (`contacts`, `forms` sheets) feeding `@DataProvider`s, plus inline provider and constants; no API/DB seeding.
- **How would you scale it?** Remove statics, per-run report paths, Grid/Selenoid or Docker, CI parameters, groups, API-based data setup (see §28).
- **What would you improve?** Credentials/secrets, dependency upgrades, hard waits, logging, `quit()`/ThreadLocal cleanup, `BasePage`, stronger assertions and data cleanup.
- **Current limitations?** No real logging, hard-coded creds, sleeps, outdated libs, limited coverage (4 classes), single-account shared state, no Docker/cloud/API/DB.
- Avoid claiming: BDD/Cucumber, REST testing, DB validation, Dockerised execution, PageFactory, Log4j logging, Grid in production use — none exist in this repo.

---

# 30. AI Quick Reference

## MUST KNOW
- Stack: Java 17, Selenium 4.31.0, TestNG 7.11.0, Maven, WebDriverManager 5.9.2, Extent 5.0.8, Allure 2.29.1.
- Driver: `com.qa.crm.factory.DriverFactory` (`initDriver`, `initProp`, static `getDriver()`, `getScreenshot`) + `OptionsManager`; `ThreadLocal<WebDriver> tlDriver`.
- Base: `src/test/java/com/qa/crm/base/BaseTest` — `@BeforeTest` setup / `@AfterTest` teardown; needs suite XML params `browser`, `browserversion`, `testname`.
- Pages in `com.qa.crm.pages`, interactions through `ElementUtil`; constants in `AppConstants`.
- Env via `-Denv` (default **qa**, which is **remote=true**); `prod` maps to `config.properties`.
- Listeners: `ExtentReportListener`, `TestAllureListener`, `AnnotationTransformer`→`Retry` (3 retries), `ResultSender`→Elasticsearch.
- One test class per `<test>`; `parallel="tests"`, 4 threads.

## MUST FOLLOW
- Private `By` fields + constructor `(WebDriver)` + `ElementUtil` in every page; return next page objects.
- Timeouts/strings from `AppConstants`; Excel sheet names as constants.
- Explicit waits only; reuse `ElementUtil`; no new statics; thread-safe additions.
- New tests: extend `BaseTest`, add `@Listeners`, register in suite XML with required parameters.
- Existing naming (`do…`, `waitFor…`, `…Page`, `…PageTest`), custom `RuntimeException`s, minimal scoped diffs.

## DO NOT
- Do not hard-code/echo/log credentials; do not copy values from existing properties files; do not print `prop` or password in `@Step`.
- Do not add `Thread.sleep`, PageFactory, new patterns, or new dependencies without justification.
- Do not call `driver.findElement` from pages; do not store `WebElement`s in fields.
- Do not rename typo'd public methods or reformat unrelated files.
- Do not assume Docker, API, DB, logging, groups, or git history exist — they don't (in this snapshot).

## COMMON LOCATIONS
| What | Where |
|---|---|
| Tests | `src/test/java/com/qa/crm/tests/` |
| Base test | `src/test/java/com/qa/crm/base/BaseTest.java` |
| Page Objects | `src/main/java/com/qa/crm/pages/` |
| Utilities | `src/main/java/com/qa/crm/utils/` |
| Driver/options | `src/main/java/com/qa/crm/factory/` |
| Listeners/retry/ES push | `src/main/java/com/qa/crm/listeners/` |
| Config | `src/test/resources/config/*.properties` |
| Test data | `src/test/resources/testdata/crmtestdata.xlsx` |
| Suites | `src/test/resources/testrunners/*.xml` |
| Reports | `./reports/TestExecutionReport.html`, `./allure-results/`, `./screenshot/` (all git-ignored) |
| Build / CI | `pom.xml`, `Jenkinsfile` |

## COMMANDS
```bash
mvn clean compile
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_regression.xml -Denv=dev
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_sanity.xml -Denv=<env>
```
(Run from the project root. `-Denv` omitted ⇒ `qa` ⇒ remote grid expected at `http://localhost:4444/wd/hub`.)
