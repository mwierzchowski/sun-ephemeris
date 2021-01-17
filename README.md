Sun Ephemeris
=============

![CI/CD](https://github.com/mwierzchowski/sun-ephemeris/workflows/CI/CD/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=alert_status)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=ncloc)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mwierzchowski_sun-ephemeris&metric=coverage)](https://sonarcloud.io/dashboard?id=mwierzchowski_sun-ephemeris)

Microservice that provides sun ephemeris as a stream of events or REST API.

### Bill of materials
Project is composed of the following tools:
- [Gradle](https://gradle.org)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
- [Spring Boot Dev Tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools)  
- [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)
- [Spring MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Java Validation Framework (JSR 380)](https://beanvalidation.org/2.0-jsr380/)
- [Lombok](https://projectlombok.org)
- [MapStruct](https://mapstruct.org)
- [ShedLock](https://github.com/lukas-krecan/ShedLock)
- [Resilience4J](https://github.com/resilience4j/resilience4j)
- [PostgreSQL](https://www.postgresql.org)
- [Redis](https://redislabs.com)
- [Springdoc-OpenAPI](https://springdoc.org)
- [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator)
- [Liquibase](https://www.liquibase.org)
- [Spock Framework](http://spockframework.org)
- [WireMock](http://wiremock.org)
- [Testcontainers](https://www.testcontainers.org) and [Playtika](https://github.com/Playtika/testcontainers-spring-boot)
- [GitHub Actions](https://github.com/features/actions)
- [SonarCloud](https://sonarcloud.io)
- [Docker](https://www.docker.com) 

For the full list see the [build.gradle](build.gradle).

### Code standards
Project structure, naming convention and to some degree design were inspired by:
- [Onion](https://www.codeguru.com/csharp/csharp/cs_misc/designtechniques/understanding-onion-architecture.html) / 
  [Clean](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) /
  [Hexagonal](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software)) Architecture
- [Domain Driven Design (DDD)](https://en.wikipedia.org/wiki/Domain-driven_design)

If one wants to read more about combining all these together, 
[here](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)
is an interesting article.
 
Prerequisites
-------------

Project requires following components being installed on a developer's machine:  

Tool                                                                              | Version            | Comment
----------------------------------------------------------------------------------|--------------------|---------------------------------------------------------------------------
[Git](https://git-scm.com/)                                                       | `latest`           | 
[JDK](https://adoptopenjdk.net/archive.html?variant=openjdk14&jvmVariant=hotspot) | `15`               | AdoptOpenJDK is recommended 
[Docker Desktop](https://www.docker.com/products/docker-desktop)                  | `2.4` or newer     |
Favourite IDE, e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)              | `latest`           | 

**Please note:** Project does not depend on any IntelliJ IDEA specific feature. Feel free to use instead
[Eclipse](https://www.eclipse.org) or Notepad :)

Optionally, consider installing IDE plugins that improve development experience. Recommended plugins should have
versions available for most popular IDEs (IntelliJ links below):

Plugin                                                                    | Comment
--------------------------------------------------------------------------|----------------------------------------------------------------------
[Lombok](https://plugins.jetbrains.com/plugin/6317-lombok)                | Support for Lombok generated code
[MapStruct](https://plugins.jetbrains.com/plugin/10036-mapstruct-support) | Support for MapStruct generated code
[SonarLint](https://plugins.jetbrains.com/plugin/7973-sonarlint)          | Quality feedback on the fly 
[Docker](https://plugins.jetbrains.com/plugin/7724-docker)                | Support for docker-compose (handy when starting application locally)

**Please note:** Without some of these plugins, IDEs may highlight references to generated code (e.g. Lombok properties
or MapStruct mappers) as errors. It is annoying but do not affect building or running application.

How to run...
-------------

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

### Dev tools

Development tools are provided as a code by Docker Compose. They may be controlled with standard docker commands
or using Gradle tasks:
- `composeUp` - starts development tools as Docker Compose services (waits until services are up and running)
- `composeDown` - stops development tools (all the data is wiped, including database content)

For example, following command starts development tools:
```
./gradlew composeUp 
```

Once started, following services are available:

Service                                                               | URL                         | Credentials
----------------------------------------------------------------------|-----------------------------|----------------------------
[PostgreSQL](https://www.postgresql.org)                              | http://localhost:5432       | `dev` / `dev`
[Redis](https://redislabs.com)                                        | http://localhost:6379       | NA
[pgAdmin](https://www.pgadmin.org)                                    | http://localhost:81         | `admin@localhost` / `admin` 
[Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) | http://localhost:82         | `admin` / `admin`
[Swagger UI](https://swagger.io/tools/swagger-ui/)                    | http://localhost:83/swagger | NA 

### Application

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

Project includes [spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-devtools)
"*that can make the application development experience a little more pleasant*", e.g. provides code changes detection
and automatic restarts.

License
-------
This software is released under the [MIT](LICENSE) Open Source license.
