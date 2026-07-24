# Stack

    > Java 17
    > Spring Boot 3.X
    > JUnit 5
    > maven 3.3.4

# How to have gpsUtil, rewardCentral and tripPricer dependencies available ?

> Run :
## gpsUtil
- ./mvnw install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
## RewardCentral
- ./mvnw install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
## TripPricer
- ./mvnw install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

# Run the app


> - Run the app :  `./mvnw spring-boot:run`   (Windows: `mvnw.cmd spring-boot:run`)
> - Run the tests :  `./mvnw test`            (Windows: `mvnw.cmd test`)
> - Build the jar :  `./mvnw package -DskipTests`

For convenience : (not in windows)

> - `make run`    → start the app
> - `make test`   → run unit tests
> - `make verify` → run unit + integration tests
> - `make build`  → build the jar
> - `make clean`  → remove `target/`
