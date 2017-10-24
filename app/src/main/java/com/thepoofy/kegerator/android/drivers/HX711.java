package com.thepoofy.kegerator.android.drivers;

import java.io.IOException;

/**
 * @author wvanderhoef
 */

public interface HX711 {

    /**
     * if the controller is asleep, setting the pin to low will wake it.
     *
     * @throws IOException on error writing value
     */
    void wake() throws IOException;

    /**
     * Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.
     *
     * @throws IOException on error writing value
     */
    void sleep() throws IOException;

    /**
     *
     * @throws IOException on error.
     */
    void setupPulseMode() throws IOException;

    /**
     * @throws IOException on error writing value
     */
    void pulseHighLow() throws IOException;

    /**
     * @return True if value is HIGH
     * @throws IOException on error reading value
     */
    boolean readDat() throws IOException;

    void release();
}
