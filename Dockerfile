########## STAGE 1: build the jar and cache the maven dependencies ##########
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# copy the pom first, so the dependency layer is cached and does not rebuild on a code change
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# now copy the source and package, skipping the tests because the tests run in stage 2
COPY src ./src
RUN mvn -B -q package -DskipTests

########## STAGE 2: the test runner image ##########
# this is the only image published to the registry and the only one the Job runs
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

COPY --from=build /root/.m2 /root/.m2
COPY --from=build /app/target /app/target
COPY pom.xml .
COPY src ./src

# non-root user: kubernetes refuses to run root by default under the restricted SCC
RUN useradd -m -u 10001 tester && chown -R tester:tester /app
USER tester

# default values, every one of them can be overridden by the env vars in compose/k8s
ENV ENV=qa \
    BROWSER=chrome \
    HEADLESS=true \
    INCOGNITO=false \
    HIGHLIGHT=false \
    REMOTE=true \
    CONTAINER=true \
    HUB_URL=http://selenium-hub:4444/wd/hub \
    URL=https://ui.cogmento.com/

# mvn test is the entry point, so the same image runs a smoke or a regression suite
# by overriding the suite file at runtime:
#   docker run --rm crm-tests mvn -B test -Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_smoke.xml
CMD ["mvn", "-B", "test", "-Dsurefire.suiteXmlFiles=src/test/resources/testrunners/testng_regression.xml"]
