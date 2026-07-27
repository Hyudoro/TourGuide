package com.openclassrooms.tourguide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import com.openclassrooms.tourguide.util.DistanceCalculator;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private static final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final AttractionCatalog attractionCatalog;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    private static final int NUMBER_OF_NEARBY_ATTRACTIONS = 5;
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService,
                            AttractionCatalog attractionCatalog) {
        this.gpsUtil = gpsUtil;
        this.attractionCatalog = attractionCatalog;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
                : trackUserLocation(user);
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }
    /** An arrayList can size itself from the source in one shot
     *  while the stream grows a buffer and resizes as it goes **/
    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getNearbyAttractions(VisitedLocation visitedLocation) {
        List<Attraction> attractions = attractionCatalog.getAttractions();
        int total = attractions.size();
        int wanted = Math.min(NUMBER_OF_NEARBY_ATTRACTIONS, total);

        // one distance per attraction; origin[i] remembers which attraction miles[i] came from
        double[] miles = new double[total];
        int[] origin = new int[total];
        for (int i = 0; i < total; i++) {
            miles[i] = DistanceCalculator.getDistance(attractions.get(i),
                visitedLocation.location);
            origin[i] = i;
        }
        List<Attraction> nearest = new ArrayList<>(wanted);
        int live = total;   // everything from 'live onward is removed
        for (int pick = 0; pick < wanted; pick++) {
            int best = 0;
            double bestMiles = miles[0];
            for (int i = 1; i < live; i++) {                // 26, then 25, 24, 23, 22
                if (miles[i] < bestMiles) {
                    bestMiles = miles[i];
                    best = i;
                }
            }
            nearest.add(attractions.get(origin[best]));     // origin[best], not best

            live--;                                         // drop the last live element into the hole
            miles[best]  = miles[live];
            origin[best] = origin[live];
        }
        return nearest;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 5).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        return ThreadLocalRandom.current().nextDouble(-180,180);
    }

    private double generateRandomLatitude() {
        return ThreadLocalRandom.current().nextDouble(-85.05112878, 85.05112878);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
