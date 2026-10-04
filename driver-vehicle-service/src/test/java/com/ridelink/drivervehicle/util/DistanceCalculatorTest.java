package com.ridelink.drivervehicle.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceCalculatorTest {

    @Test
    void samePoint_hasZeroDistance() {
        assertEquals(0.0, DistanceCalculator.calculateKm(6.9271, 79.8612, 6.9271, 79.8612), 0.0001);
    }

    @Test
    void colomboToKandy_isRoughly94Km() {
        double km = DistanceCalculator.calculateKm(6.9271, 79.8612, 7.2906, 80.6337);
        assertTrue(km > 92 && km < 97, "expected about 94 km but was " + km);
    }
}
