package com.localjobfinder.bakend.components;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
        return createPoint(BigDecimal.valueOf(lat), BigDecimal.valueOf(lng));
    }

    public Point createPoint(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) {
            return null;
        }
        Point point = geometryFactory.createPoint(new Coordinate(lng.doubleValue(), lat.doubleValue()));
        point.setSRID(4326);
        return point;
    }
}
