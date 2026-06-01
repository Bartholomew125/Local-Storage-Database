package com.homedb;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public record GeoLocation(
        float latitude, 
        float longitude,
        float altitude,
        float latitudeSpan,
        float longitudeSpan
) {
    @Override
    public final String toString() {
        return Stream.of(
                this.latitude,
                this.longitude,
                this.altitude,
                this.latitudeSpan,
                this.longitudeSpan
            )
            .map(String::valueOf)
            .map(c -> "(" + c + ")")
            .collect(Collectors.joining());
    }
}
