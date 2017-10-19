package com.thepoofy.kegerator.android;

import java.math.BigDecimal;

import timber.log.Timber;

/**
 * https://en.wikipedia.org/wiki/Steinhart%E2%80%93Hart_equation
 *
 * @author wvanderhoef
 */
public class SteinhartHartEquation {

    private static final BigDecimal A = new BigDecimal("1.009249522e-03");
    private static final BigDecimal B = new BigDecimal("2.378405444e-04");
    private static final BigDecimal C = new BigDecimal("2.019202697e-07");

    private final float resistor1;

    public SteinhartHartEquation(float resistor1) {
        this.resistor1 = resistor1;
    }

    public static double formatTemperatureCelsius(double temperatureKelvin) {
        return temperatureKelvin - 273.15;
    }

    public static double formatTemperatureFahrenheit(double temperatureKelvin) {
        return (formatTemperatureCelsius(temperatureKelvin) * 9.0) / 5.0 + 32.0;
    }

    public double findTemperatureKelvin(int resistanceInOhms) {
        double resistor2 = resistor1 * (1023.0 / (float) resistanceInOhms - 1.0);
        double logR2 = Math.log(resistor2);

        double temperatureKelvin = (1.0 /
                (A.doubleValue() + B.doubleValue() * logR2 + C.doubleValue() * Math.pow(logR2, 3)));

        Timber.i("Temperature Kelvin=[%f]", temperatureKelvin);

        return temperatureKelvin;
    }
}
