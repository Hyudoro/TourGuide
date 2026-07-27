package com.openclassrooms.tourguide.service;

import gpsUtil.location.Attraction;

/** An attraction with its trigonometry worked out once, up front. */
public record ProximityCheck(
    Attraction attraction,
    double attractionSinLat,
    double attractionCosLat,
    double attractionLonRadians
){
    public static ProximityCheck of(Attraction attraction){
        double latitude = Math.toRadians(attraction.latitude);
        return new ProximityCheck(
            attraction,
            Math.sin(latitude),
            Math.cos(latitude),
            Math.toRadians(attraction.longitude));
    }
    /** True when the given point is inside the buffer that cosThreshHold
     *  represents.*/
    public boolean covers(double sinLat, double cosLat, double lonRadians, double cosThreshold){
        double cosAngle = attractionSinLat * sinLat
                          + attractionCosLat * cosLat
                          * Math.cos(attractionLonRadians - lonRadians);
        return cosAngle >=cosThreshold;
    }
}
