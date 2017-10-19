package com.thepoofy.kegerator.android.drivers;

import java.io.IOException;

/**
 * @author wvanderhoef
 */

public interface HX711 {

    /**
     * @throws IOException on error writing value
     */
    void wake() throws IOException;

    /**
     * @throws IOException on error writing value
     */
    void sleep() throws IOException;

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
