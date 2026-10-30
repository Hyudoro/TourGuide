# TourGuide

[![CI](https://github.com/Hyudoro/TourGuide/actions/workflows/ci.yml/badge.svg)](https://github.com/Hyudoro/TourGuide/actions/workflows/ci.yml)

Spring Boot web app that tracks users' locations and turns them into tour
recommendations: nearby attractions, reward points and trip deals. Tuned for
throughput, with virtual threads, batched parallel reward fetching and
precomputed trigonometry for distance checks, on top of the gpsUtil,
RewardCentral and TripPricer libraries.

## Stack

- Java 21
- Spring Boot 3.5.16
- JUnit 5
- Maven 3.9.14 (wrapper)

## How to install the gpsUtil, rewardCentral and tripPricer dependencies?

> Run :
### gpsUtil
- ./mvnw install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
### RewardCentral
- ./mvnw install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
### TripPricer
- ./mvnw install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

## Run the app


> - Run the app :  `./mvnw spring-boot:run`   (Windows: `mvnw.cmd spring-boot:run`)
> - Run the tests :  `./mvnw test`            (Windows: `mvnw.cmd test`)
> - Build the jar :  `./mvnw package -DskipTests`

For convenience : (not in windows)

> - `make run`    → start the app
> - `make test`   → run unit tests
> - `make verify` → run unit + integration tests
> - `make build`  → build the jar
> - `make clean`  → remove `target/`
