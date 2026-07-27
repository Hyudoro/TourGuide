package com.openclassrooms.tourguide.util;

import gpsUtil.location.Location;

/**
 * Stateless geographic helper. Computes the great-circle distance between two
 * {@link Location} points, independently of any business feature (rewards,
 * nearby-attractions, ...) that happens to need it.
 */
public final class DistanceCalculator {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private DistanceCalculator() {
        // utility class, not meant to be instantiated
    }

    /**
     * @return the distance between the two locations, in statute miles.
     */
    public static double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double cosAngle = Math.sin(lat1) * Math.sin(lat2)
                          + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2);

        // rounding can push this a hair outside [-1,1], where acos return NaN
        if (cosAngle > 1.0) {
            cosAngle = 1.0;
        } else if (cosAngle < -1.0){
            cosAngle = -1.0;
        }

        double angle = Math.acos(cosAngle);

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}
