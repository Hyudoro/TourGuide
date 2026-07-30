package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.AttractionCatalog;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

public class TestRewardsService {

    // Here the intent
    //  (making sure a user standing at an attraction earn its reward) is preserved while being optimized avoiding GpsUtils calls.
    @Test
    public void userGetRewards() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtil.getAttractions().get(0);
        //Dropping the trackUserLocation call saves a gpsUtil permit.
        tourGuideService.recordLocation(user, new VisitedLocation(user.getUserId(),
            attraction, new Date()));
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void nearAllAttractions() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);
        tourGuideService.tracker.stopTracking();
        User user = tourGuideService.getAllUsers().get(0);
        // because ProximityBuffer is MAX_VALUE whatever location is last, will
        // considered as nil.
        rewardsService.calculateRewards(user, user.getLastVisitedLocation());
        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    }

}
