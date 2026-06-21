package com.localjobfinder.bakend.configurationPackage;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeometryConfig {

//    * This factory will create points compatible with MySQL/Google Maps.
//            */
    @Bean
    public GeometryFactory geometryFactory() {
        // SRID 4326 = WGS84 (Standard GPS Coordinates)
        return new GeometryFactory(new PrecisionModel(), 4326);
    }

    /**
     * This allows Jackson to convert JTS Geometry (Point) into JSON.
     * Without this, your API will error when returning a Point object.
     */
    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }

}
