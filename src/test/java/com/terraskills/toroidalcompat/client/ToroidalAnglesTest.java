package com.terraskills.toroidalcompat.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToroidalAnglesTest {
    @Test
    void toroidalAngleStartsAtSeam() {
        assertEquals(0.0, ToroidalAngles.toroidal(-256, -256, 512));
        assertEquals(90.0, ToroidalAngles.toroidal(-128, -256, 512));
        assertEquals(180.0, ToroidalAngles.toroidal(0, -256, 512));
        assertEquals(270.0, ToroidalAngles.toroidal(128, -256, 512));
    }

    @Test
    void poloidalAngleIsSignedAroundEquator() {
        assertEquals(90.0, ToroidalAngles.poloidal(-256, -256, 512), 1e-12);
        assertEquals(45.0, ToroidalAngles.poloidal(-192, -256, 512), 1e-12);
        assertEquals(0.0, ToroidalAngles.poloidal(-128, -256, 512), 1e-12);
        assertEquals(-90.0, ToroidalAngles.poloidal(0, -256, 512), 1e-12);
        assertEquals(0.0, ToroidalAngles.poloidal(128, -256, 512), 1e-12);
        assertEquals(90.0, ToroidalAngles.poloidal(256, -256, 512), 1e-12);
    }

    @Test
    void latitudeBranchDistinguishesTheTwoHalvesOfTheLoop() {
        assertEquals('+', ToroidalAngles.latitudeBranch(-128, -256, 512));
        assertEquals('-', ToroidalAngles.latitudeBranch(128, -256, 512));
    }

}
