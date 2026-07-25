package com.openclassrooms.tourguide.service;
import java.util.List;

import org.springframework.stereotype.Component;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;

@Component
public class AttractionCatalog {

    private final List<Attraction> attractions;

    public AttractionCatalog(GpsUtil gpsUtil){
        this.attractions = List.copyOf(gpsUtil.getAttractions());
    }

    public List<Attraction> getAttractions(){
        return attractions;
    }
}
