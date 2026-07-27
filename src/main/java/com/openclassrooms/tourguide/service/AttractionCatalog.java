package com.openclassrooms.tourguide.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;

@Component
public class AttractionCatalog {

    private final List<Attraction> attractions;
    private final List<ProximityCheck> proximityChecks;
    public AttractionCatalog(GpsUtil gpsUtil){
        this.attractions = List.copyOf(gpsUtil.getAttractions());
        List<ProximityCheck> checks = new ArrayList<>(attractions.size());
        for (Attraction attraction : attractions){
            checks.add(ProximityCheck.of(attraction));
        }
        this.proximityChecks = List.copyOf(checks);
    }

    public List<Attraction> getAttractions(){
        return attractions;
    }

    public List<ProximityCheck> getProximityChecks(){
        return proximityChecks;
    }
}
