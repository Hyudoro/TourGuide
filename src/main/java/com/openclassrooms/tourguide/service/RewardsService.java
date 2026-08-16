package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import com.openclassrooms.tourguide.util.DistanceCalculator;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

@Service
public class RewardsService {

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private double proximityCosThreshold  =
        DistanceCalculator.cosineForMiles(defaultProximityBuffer);
    private int attractionProximityRange = 200;
    private final AttractionCatalog attractionCatalog;
    private final RewardCentral rewardsCentral;
    public RewardsService(RewardCentral rewardCentral, AttractionCatalog attractionCatalog) {
        this.rewardsCentral = rewardCentral;
        this.attractionCatalog = attractionCatalog;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
        this.proximityCosThreshold = DistanceCalculator.cosineForMiles(proximityBuffer);
    }

    public void setDefaultProximityBuffer() {
        setProximityBuffer(defaultProximityBuffer);
    }

    public void calculateRewards(User user, VisitedLocation visitedLocation) {
        List<ProximityCheck> checks = attractionCatalog.getProximityChecks();


        // The point's own trig, once per location instead of once per attraction
        double latitude = Math.toRadians(visitedLocation.location.latitude);
        double sinLat = Math.sin(latitude);
        double cosLat = Math.cos(latitude);
        double lonRadians = Math.toRadians(visitedLocation.location.longitude);
        List<Attraction> earned = null;
        for (ProximityCheck check : checks){
            Attraction attraction = check.attraction();
            // hasRewardFor first: it short-circuits before any trig runs.
            if (!user.hasRewardFor(attraction.attractionName)
                && check.covers(sinLat,cosLat, lonRadians, proximityCosThreshold)){
                if (earned == null) {
                    earned = new ArrayList<>(4);
                }
                earned.add(attraction);
            }
        }
        if (earned == null) return;

        for (Attraction attraction : earned) {
            user.addUserReward(new UserReward(visitedLocation, attraction,
                getRewardPoints(attraction, user)));
        }
    }

    public void calculateRewards(List<User> users){
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor())
        {
            for (User user : users){
                VisitedLocation location = user.getLastVisitedLocation();
                if (location != null) {
                    executor.execute(() -> calculateRewards(user, location));
                }
            }
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return DistanceCalculator.getDistance(attraction, location) >
        attractionProximityRange ? false : true;
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * The reward points for several attractions, in the order they were given.
     * Each lookup sleeps ~500 ms inside RewardCentral, so they all wait at the
     * same time rather than one after another.
     */
    public int[] getRewardPoints(List<Attraction> attractions, User user) {
        List<Callable<Integer>> lookups = new ArrayList<>(attractions.size());
        for (Attraction attraction : attractions) {
            lookups.add(() -> getRewardPoints(attraction, user));
        }
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // invokeAll returns once every lookup is done, in the submitted order
            List<Future<Integer>> results = executor.invokeAll(lookups);
            int[] points = new int[results.size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = results.get(i).get();
            }
            return points;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading reward points", e);
        } catch (ExecutionException e) {
            // get() would otherwise bury the real cause in a Future, the 2.1 trap
            throw new IllegalStateException("Reward point lookup failed", e.getCause());
        }
    }

}
