Sun Ephemeris
=============

![CI/CD](https://github.com/mwierzchowski/sun-ephemeris/workflows/CI/CD/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=alert_status)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=ncloc)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=coverage)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)

Microservice providing sun ephemeris as a REST endpoint, and the stream of events to Redis channel, published at the
time of their occurrence.

### Toolchain
- [Gradle](https://gradle.org)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
- [Spring Boot Dev Tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools)
- [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)
- [Spring MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Lombok](https://projectlombok.org)
- [ShedLock](https://github.com/lukas-krecan/ShedLock)
- [Resilience4J](https://github.com/resilience4j/resilience4j)
- [Redis](https://redislabs.com)
- [Springdoc-OpenAPI](https://springdoc.org)
- [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator)
- [Spock Framework](http://spockframework.org)
- [WireMock](http://wiremock.org)
- [Testcontainers](https://www.testcontainers.org) and [Playtika](https://github.com/Playtika/testcontainers-spring-boot)
- [GitHub Actions](https://github.com/features/actions)
- [SonarCloud](https://sonarcloud.io)
- [Docker](https://www.docker.com)

For the full list see the [build.gradle](build.gradle).

### Architecture
Design, code structure and naming convention were inspired by:
- [Onion](https://www.codeguru.com/csharp/csharp/cs_misc/designtechniques/understanding-onion-architecture.html) /
  [Clean](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) /
  [Hexagonal](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)) Architecture
- [Domain Driven Design (DDD)](https://en.wikipedia.org/wiki/Domain-driven_design)

If one wants to read more about combining all these together,
[here](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)
is an interesting article.

Overview
--------

Service does not calculate sun ephemeris on its own. Instead, it uses API exposed by
[Sunrise-Sunset](https://sunrise-sunset.org) site (kudos for great work!). Client code is generated from
[specification](/etc/sunrise-sunset-spec.yml) with OpenAPI Generator. To optimize network traffic and avoid risk of
abusing API usage rules, received ephemeris is cached in Redis under a date key.

**Please note:** Since, it is assumed that location is static configuration and never changes during service lifetime,
location is not part of the key.

Each day, shortly after midnight, publish scheduler requests new day ephemeris and schedules tasks that will publish
events to Redis channel at the event time. The same data is also available as REST endpoint for interested parties.
Following sun ephemeris events are available:
- dawn
- sunrise
- noon
- sunset
- dusk 

**Please note:** Events on Redis channel and REST endpoint have the same structure. It is documented in endpoint
OpenAPI specification.
  
Since there might be more than one instance of the service (e.g. due to HA requirements), publishers try to acquire a
lock and only winner publishes event. Following diagram presents events publishing sequence:
![Success Sequence](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/mwierzchowski/sun-ephemeris/master/etc/seq-success.puml)

Sunrise-Sunset is an external endpoint that might be down at any time without a notice. In order to mitigate this risk,
service retries calls with an exponentially growing backoff time. Since data is requested shortly after midnight, 
it should give enough time for Sunrise-Sunset site to recover before first ephemeris event (dawn) time. However, if
outage continues, publish scheduler uses previous day ephemeris which should be accurate enough as a replacement.
Following diagram presents fallback sequence:
![Alternate Sequence](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/mwierzchowski/sun-ephemeris/master/etc/seq-alternate.puml)

Usage
-----

Service is distributed as a Docker image. The latest version (including release candidates) is available in
[DockerHub](https://hub.docker.com/repository/docker/mwierzchowski/sun-ephemeris) under tag
`mwierzchowski/sun-ephemeris:latest`. 

In order to start container, at minimum following environment variables have to be provided:
- `location.latitude` and `location.longitude` - coordinates for ephemeris calculation
- `spring.redis.host` - Redis host name (assuming default port is used)

All properties are listed in [application.yml](/src/main/resources/application.yml).

**Please note:** Service depends on Spring Boot autoconfiguration feature that may be configured with additional
properties.  

Developer Guide
---------------
 
### Prerequisites
Project development requires following software being installed on a developer's machine:  

Tool                                                             | Version        | Comment
-----------------------------------------------------------------|----------------|----------------------------------------------------------------------------------------------------------
[Git](https://git-scm.com/)                                      | `latest`       |
JDK                                                              | `15`           | [AdoptOpenJDK](https://adoptopenjdk.net/archive.html?variant=openjdk14&jvmVariant=hotspot) is recommended
IDE                                                              | `latest`       | [IntelliJ IDEA](https://www.jetbrains.com/idea/) is recommended
[Docker Desktop](https://www.docker.com/products/docker-desktop) | `2.4` or newer |

**Please note:** Project does not depend on IntelliJ IDEA specific features. Feel free to use [Eclipse](https://www.eclipse.org)
or Notepad instead :)

Optionally, consider installing IDE plugins that improve development experience. Recommended plugins should have
versions available for most popular IDEs (IntelliJ links below):

Plugin                                                                     | Comment
---------------------------------------------------------------------------|----------------------------------------------------------------------
[Lombok](https://plugins.jetbrains.com/plugin/6317-lombok)                 | Support for Lombok generated code
[MapStruct](https://plugins.jetbrains.com/plugin/10036-mapstruct-support)  | Support for MapStruct generated code
[Docker](https://plugins.jetbrains.com/plugin/7724-docker)                 | Support for docker-compose (handy when starting application locally)
[SonarLint](https://plugins.jetbrains.com/plugin/7973-sonarlint)           | Quality feedback on the fly 
[PlantUML](https://plugins.jetbrains.com/plugin/7017-plantuml-integration) | Helps writing diagrams with PlantUML

**Please note:** Without some of these plugins, IDEs may highlight references to generated code (e.g. Lombok properties
or MapStruct mappers) as errors. It is annoying but do not affect building or running application.

### Environment
Development environment is provided as a code by Docker Compose. It may be controlled with standard docker commands
or using Gradle tasks:
- `composeUp` - starts dev-env as Docker Compose services (waits until services are up and running)
- `composeDown` - stops dev-env (all the data is wiped, including database content)

For example, following command starts dev-env:
```
./gradlew composeUp 
```

Once started, following services are available:

Service                                                               | URL                         | Credentials
----------------------------------------------------------------------|-----------------------------|----------------------------
[Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) | http://localhost:82         | `admin` / `admin`
[Swagger UI](https://swagger.io/tools/swagger-ui/)                    | http://localhost:83/swagger | n/a
[Redis](https://redislabs.com)                                        | http://localhost:6379       | n/a


### Build
Project build is powered by [Gradle wrapper](https://gradle.org) with additional plugins (e.g. `java`, `spring-boot`,
`docker-compose`). Few most useful build tasks:
- `clean` - cleans the build
- `test` - executes unit and integration tests
- `build` - builds the application (and executes tests)

For example, following command runs a clean build:
```
./gradlew clean build 
```

### Run
Service, as a regular Spring Boot application may be started locally by running main application class or using Gradle
task:
- `bootRun` - starts application (compiles and builds code if needed)

Since application to start requires development tools to be up and running, one may combine Gradle tasks to launch
complete development environment with a single command, e.g.:
```
./gradlew composeUp bootRun 
```

Once started, application listens on http://localhost:8080. Status of the running application can be checked using one
of the Actuator endpoints, e.g.:
- http://localhost:8080/actuator/info - general info
- http://localhost:8080/actuator/health - health status

**Please note:** Project includes [spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools)
"*that can make the application development experience a little more pleasant*", e.g. provides code changes detection
and automatic restarts.

License
-------

This software is released under the [MIT](LICENSE) Open Source license.
