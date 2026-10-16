package com.hotel.util;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Simple fixed-price taxi fares (no maps API needed):
 *   fare = car base price x pickup-zone multiplier.
 * Airport rides are long, station rides short, "other address" in between.
 */
public class TaxiFare {

    public static final Map<String, Integer> CAR_BASE = Map.of(
            "STANDARD", 3000,   // up to 3 passengers
            "PREMIUM", 6000,    // up to 3 passengers, luxury sedan
            "VAN", 8000);       // up to 7 passengers

    public static final Map<String, Integer> CAR_SEATS = Map.of(
            "STANDARD", 3, "PREMIUM", 3, "VAN", 7);

    public static final Map<String, BigDecimal> ZONE_MULTIPLIER = Map.of(
            "AIRPORT", new BigDecimal("3.0"),
            "STATION", new BigDecimal("1.0"),
            "OTHER", new BigDecimal("1.5"));

    public static boolean isValidCar(String car) { return car != null && CAR_BASE.containsKey(car); }

    public static boolean isValidZone(String zone) { return zone != null && ZONE_MULTIPLIER.containsKey(zone); }

    public static BigDecimal estimate(String carType, String pickupType) {
        return BigDecimal.valueOf(CAR_BASE.get(carType)).multiply(ZONE_MULTIPLIER.get(pickupType))
                .setScale(0, java.math.RoundingMode.HALF_UP);
    }
}
