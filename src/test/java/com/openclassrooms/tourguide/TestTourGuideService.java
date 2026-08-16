package com.openclassrooms.tourguide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.dto.NearbyAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.AttractionCatalog;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.util.DistanceCalculator;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

public class TestTourGuideService {

    @Test
    public void getUserLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions_shouldReturnFiveAttractions_eachOneFullyDescribed() {
        // GIVEN a user whose position has just been tracked
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        // WHEN the nearby attractions are asked for
        List<NearbyAttractionDto> nearbyAttractions = tourGuideService.getNearbyAttractions(user);

        tourGuideService.tracker.stopTracking();

        // THEN five come back, closest first, every field filled in
        assertThat(nearbyAttractions).hasSize(5);
        assertThat(nearbyAttractions)
            .extracting(NearbyAttractionDto::distanceInMiles)
            .isSorted();
        assertThat(nearbyAttractions).allSatisfy(nearby -> {
            assertThat(nearby.attractionName()).isNotBlank();
            assertThat(nearby.userLatitude()).isEqualTo(visitedLocation.location.latitude);
            assertThat(nearby.userLongitude()).isEqualTo(visitedLocation.location.longitude);
            assertThat(nearby.distanceInMiles()).isNotNegative();
            assertThat(nearby.rewardPoints()).isPositive();
        });
    }

    @Test
    public void getNearbyAttractions_shouldReturnTheClosestFive_howeverFarAwayTheyAre() {
        // GIVEN a user whose position has just been tracked
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        // AND the same five worked out the slow, obvious way
        List<String> closestFiveNames = attractionCatalog.getAttractions().stream()
            .sorted(Comparator.comparingDouble((Attraction attraction) ->
                DistanceCalculator.getDistance(attraction, visitedLocation.location)))
            .limit(5)
            .map(attraction -> attraction.attractionName)
            .toList();

        // WHEN the nearby attractions are asked for
        List<NearbyAttractionDto> nearbyAttractions = tourGuideService.getNearbyAttractions(user);

        tourGuideService.tracker.stopTracking();

        // THEN the selection picked the same attractions, in the same order
        assertThat(nearbyAttractions)
            .extracting(NearbyAttractionDto::attractionName)
            .containsExactlyElementsOf(closestFiveNames);
    }

    public void getTripDeals() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(10, providers.size());
    }

}
