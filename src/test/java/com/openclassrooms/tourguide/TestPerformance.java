package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.AttractionCatalog;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     *
     * These are performance metrics that we are trying to hit:
     *
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * stopWatch.getDuration().toSeconds());
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * stopWatch.getDuration().toSeconds());
     */

    @Test
    public void highVolumeTrackLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);
        // Users should be incremented up to 100,000, and test finishes within 15
        // minutes
        InternalTestHelper.setInternalUserNumber(100);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);
        // otherwise the Tracker try to access the same ressource at the same time as the watcher (stopWatch).
        tourGuideService.tracker.stopTracking();

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (User user : allUsers) {
            tourGuideService.trackUserLocation(user);
        }
        stopWatch.stop();
        long elapsedSeconds = stopWatch.getDuration().toSeconds();
        System.out.println("highVolumeTrackLocation: Time Elapsed: " + elapsedSeconds + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= elapsedSeconds);
    }

    @Test
    public void highVolumeGetRewards() {
        GpsUtil gpsUtil = new GpsUtil();
        AttractionCatalog attractionCatalog = new AttractionCatalog(gpsUtil);
        RewardsService rewardsService = new RewardsService(new RewardCentral(), attractionCatalog);

        // Users should be incremented up to 100,000, and test finishes within 20
        // minutes
        InternalTestHelper.setInternalUserNumber(100);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, attractionCatalog);
        // otherwise the Tracker try to access the same ressource at the same time as the watchenr (stopWatch)
        tourGuideService.tracker.stopTracking();
        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        allUsers.forEach(u -> rewardsService.calculateRewards(u));
        stopWatch.stop();
        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }

        long elapsedSeconds = stopWatch.getDuration().toSeconds();
        System.out.println("highVolumeGetRewards: Time Elapsed: " + elapsedSeconds + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= elapsedSeconds);
    }

}
