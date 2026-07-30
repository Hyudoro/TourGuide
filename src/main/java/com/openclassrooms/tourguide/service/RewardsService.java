package com.openclassrooms.tourguide.service;

import java.util.List;

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

        for (ProximityCheck check : checks){
            Attraction attraction = check.attraction();
            // hasRewardFor first: it short-circuits before any trig runs.
            if (!user.hasRewardFor(attraction.attractionName)
                && check.covers(sinLat,cosLat, lonRadians, proximityCosThreshold)){
                user.addUserReward (new UserReward(visitedLocation, attraction,
                    getRewardPoints(attraction,user)));
            }
        }
    }


    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return DistanceCalculator.getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

}
