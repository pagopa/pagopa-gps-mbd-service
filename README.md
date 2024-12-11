# pagoPA MBD GPS Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-gps-mbd-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-gps-mbd-service)
[![Integration Tests](https://github.com/pagopa/pagopa-gps-mbd-service/actions/workflows/ci_integration_test.yml/badge.svg?branch=main)](https://github.com/pagopa/pagopa-gps-mbd-service/actions/workflows/ci_integration_test.yml)

Expose an API that will be used by GPS Payments to generate MBD payment options

- [pagoPA MBD GPS Service](#pagopa-gps-mbd-service)
    * [Api Documentation 📖](#api-documentation-)
    * [Technology Stack](#technology-stack)
    * [Start Project Locally 🚀](#start-project-locally-)
        + [Prerequisites](#prerequisites)
        + [Run docker container](#run-docker-container)
    * [Develop Locally 💻](#develop-locally-)
        + [Prerequisites](#prerequisites-1)
        + [Run the project](#run-the-project)
        + [Spring Profiles](#spring-profiles)
        + [Testing 🧪](#testing-)
            - [Unit testing](#unit-testing)
            - [Integration testing](#integration-testing)
            - [Performance testing](#performance-testing)
    * [Contributors 👥](#contributors-)
        + [Maintainers](#maintainers)

---

## Api Documentation 📖

See the [OpenApi 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-gps-mbd-service/main/openapi/openapi.json)

---

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Web

---

## Start Project Locally 🚀

### Prerequisites

- docker

### Run docker container

from `./docker` directory

`sh ./run_docker.sh local`

---

## Develop Locally 💻

### Prerequisites

- git
- maven
- jdk-17

### Run the project

Start the springboot application with this command:

`mvn spring-boot:run -Dspring.profiles.active=local`

### Spring Profiles

- **local**: to develop locally.
- _default (no profile set)_: The application gets the properties from the environment (for Azure).

### Testing 🧪

#### Unit testing

To run the **Junit** tests:

`mvn clean verify`

#### Integration testing

From `./integration-test/src`

1. `yarn install`
2. `yarn test:<env>`

#### Performance testing

install [k6](https://k6.io/) and then from `./performance-test/src`

1. `k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json main_scenario.js`

---

## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Maintainers

See `CODEOWNERS` file
