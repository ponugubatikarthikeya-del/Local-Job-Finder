package com.localjobfinder.bakend.components;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class GeometryUtils {

    private final GeometryFactory geometryFactory;

    @Autowired
    public GeometryUtils(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public Point createPoint(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return null;
        }
        // IMPORTANT: JTS/MySQL uses (Longitude, Latitude) order
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }
}
