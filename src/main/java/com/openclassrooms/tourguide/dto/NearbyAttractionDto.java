package com.openclassrooms.tourguide.dto;

/**
 * One of the five closest attractions, as /getNearbyAttractions returns it:
 * the attraction, where the user was standing, how far apart the two are,
 * and what visiting is worth.
 */
public record NearbyAttractionDto(
    String attractionName,
    double attractionLatitude,
    double attractionLongitude,
    double userLatitude,
    double userLongitude,
    double distanceInMiles,
    int rewardPoints) {
}
