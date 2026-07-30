package com.openclassrooms.tourguide.user;

import java.util.*;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

public class User {
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private volatile VisitedLocation lastVisitedLocation;
    private List<UserReward> userRewards = new ArrayList<>();
    private final Set<String> rewardedAttractionNames = new HashSet<>();
    private UserPreferences userPreferences = new UserPreferences();
    private List<Provider> tripDeals = new ArrayList<>();
    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    /** the Set does one hash lookup instead of a scan for checking
     *  Duplicates. **/
    public void addUserReward(UserReward userReward){
        if (rewardedAttractionNames.add(userReward.attraction.attractionName)){
            userRewards.add(userReward);
        }
    }

    public List<UserReward> getUserRewards() {
        return Collections.unmodifiableList(userRewards);
    }

    public boolean hasRewardFor(String attractionName){
        return rewardedAttractionNames.contains(attractionName);
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public VisitedLocation getLastVisitedLocation() {
        return lastVisitedLocation;
    }

    public void setLastVisitedLocation(VisitedLocation visitedLocation){
        this.lastVisitedLocation = visitedLocation;
    }

    public boolean hasVisitedLocation() {
        return lastVisitedLocation != null;
    }

    public void setTripDeals(List<Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }

    public List<Provider> getTripDeals() {
        return tripDeals;
    }

}
