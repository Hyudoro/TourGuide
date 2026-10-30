# TourGuide

[![CI](https://github.com/Hyudoro/TourGuide/actions/workflows/ci.yml/badge.svg)](https://github.com/Hyudoro/TourGuide/actions/workflows/ci.yml)

TourGuide turns each user's location into tour recommendations: the five
attractions closest to them, the reward points earned by visiting nearby
attractions, and trip deals priced from those points. A background tracker
re-locates every user every five minutes, and this is a performance brief:
the whole pipeline is tuned to track and reward 100,000 users in minutes,
not hours.

## Stack

Java 21 on virtual threads, Spring Boot 3.5.16, Maven. Locations come from
gpsUtil, reward points from RewardCentral and trip prices from TripPricer:
three jars in libs/ that are not on Maven Central.

## Running it

Install the three local jars, once:

```sh
./mvnw install:install-file -Dfile=libs/gpsUtil.jar \
  -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
./mvnw install:install-file -Dfile=libs/TripPricer.jar \
  -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
./mvnw install:install-file -Dfile=libs/RewardCentral.jar \
  -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
```

Then start the app on http://localhost:8080:

```sh
./mvnw spring-boot:run
```

It starts in test mode with 10 generated users, internalUser0 to
internalUser9. On Windows, use mvnw.cmd instead of ./mvnw.

The Makefile wraps the usual commands: make run, make test, make verify
(unit and integration), make build, make clean.

## Endpoints

Every endpoint takes a userName and answers in JSON.

| Request                              | Action                                            |
|--------------------------------------|---------------------------------------------------|
| GET /getLocation?userName=           | the user's latest location, tracked on demand     |
| GET /getNearbyAttractions?userName=  | the five closest attractions, however far         |
| GET /getRewards?userName=            | the rewards the user has already earned           |
| GET /getTripDeals?userName=          | priced trip providers for the user's preferences  |

A nearby attraction carries its name, both positions, the distance in miles
and its reward points. A reward is earned once per attraction visited within
10 miles.

## How it stays fast

Two targets, asserted by TestPerformance with 100,000 users: track every
user's location in under 15 minutes, and calculate every user's rewards in
under 20 minutes. The code gets there without a line of the pricing or GPS
logic being modifiable, since those live in the provided jars. What it
changes is how the work is scheduled and how little math each check does:

- One virtual thread per user. Tracking and reward passes run each user on
  its own virtual thread, so a hundred thousand blocking calls to the jars
  proceed at once instead of queueing on a small pool.
- Trigonometry computed once, not per pair. Proximity is "within N miles",
  which is a threshold on the central angle: AttractionCatalog precomputes
  each attraction's sine, cosine and longitude in radians at startup, the
  visitor's own trig is computed once per location, and each proximity
  check becomes two multiply-adds and a comparison against a cosine
  threshold. No acos on the hot path.
- Rewards already earned are skipped before any trig runs, so each location
  only pays for the attractions it can still earn.
- Slow lookups wait together. RewardCentral sleeps roughly half a second
  per call, so the five nearest attractions' points are fetched
  concurrently and reassembled in order: one half-second wait instead of
  five.
- The tracker is a single daemon thread that survives its own failures,
  stops on interrupt, and unregisters its shutdown hook so a stopped app
  can be fully collected.

## Tests

Unit tests for the services and rewards logic, a Spring context test, and
the two 100,000-user performance runs. CI builds and tests every push and
pull request to main, and publishes the executable jar and the test reports
as build artifacts.
