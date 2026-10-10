# ---------------------------------------------------------------------------
# Test runner image for CRMHybridAutomationFrameWork.
#
# The browser lives IN this image (headless Chrome), so a pod is self contained:
# no Selenium Grid, no separate node container. Run one, get one working browser.
#
#   docker build -t crm-tests:1.0 .
#   docker run --rm -e SHARD_INDEX=0 -e SHARD_COUNT=4 crm-tests:1.0
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="madhusudanj" \
      description="CRM Hybrid Automation Framework - headless Chrome test runner"

WORKDIR /app/tests

# system packages: maven to run the suite, chrome + driver for the browser, curl for health
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      maven git curl wget unzip gnupg ca-certificates \
 && wget -q -O /tmp/google-chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
 && apt-get install -y --no-install-recommends /tmp/google-chrome.deb \
 && rm -f /tmp/google-chrome.deb \
 # chromedriver pinned to the stable channel at build time, so the image is
 # self contained and needs no network when a pod starts
 && DRIVER_VERSION=$(wget -q -O- https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_STABLE) \
 && wget -q -O /tmp/chromedriver.zip \
      "https://storage.googleapis.com/chrome-for-testing-public/${DRIVER_VERSION}/linux64/chromedriver-linux64.zip" \
 && unzip -q /tmp/chromedriver.zip -d /tmp \
 && mv /tmp/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver \
 && chmod +x /usr/local/bin/chromedriver \
 && rm -rf /tmp/chromedriver.zip /tmp/chromedriver-linux64 \
 && rm -rf /var/lib/apt/lists/*

# copy the pom first so the dependency layer is cached and a code change does not re-download
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# then the sources
COPY src ./src

# non root user, matching the uid the kubernetes manifests run as
RUN useradd -m -u 10001 tester && chown -R tester:tester /app/tests
USER tester

# every one of these can be overridden at runtime
ENV ENV=qa \
    BROWSER=chrome \
    HEADLESS=true \
    REMOTE=false \
    CONTAINER=true \
    URL=https://ui.cogmento.com/ \
    CHROME_BINARY=/usr/bin/google-chrome \
    DRIVER_PATH=/usr/local/bin/chromedriver \
    SUITE_XML=src/test/resources/testrunners/testng_shard.xml \
    SHARD_INDEX=0 \
    SHARD_COUNT=1

# a plain local ChromeDriver, not RemoteWebDriver - the browser is already here
ENTRYPOINT ["mvn", "-B", "test"]
CMD ["-Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_shard.xml"]
