package com.thepoofy.kegerator.android.drivers;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

import static com.thepoofy.kegerator.android.helpers.BinaryUtils.booleanArrayToLong;

/**
 * Adapted from HX711 http://image.dfrobot.com/image/data/SEN0160/hx711_english.pdf
 *
 * @author Will Vanderhoef
 */
public class Scale {
    private static final int ITERATIONS_PER_READ = 10;
    private static final int WAKE_RETRY_LIMIT = 100;
    private static final long GRAM_SCALE = 1992;

    private final HX711 hx711;

    public Scale(HX711 hx711) {
        this.hx711 = hx711;
    }

    public void sleep() throws IOException {
        Timber.v("Attempting to sleep HX711.");
        // Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.

        hx711.sleep();
    }

    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        // if the controller is asleep, setting the pin to low will wake it.
        hx711.wake();
    }

    private boolean isReady() throws IOException {
        return !hx711.readDat();
    }

    void ready() throws IOException {
        int retryCount = 0;

        do {
            wake();
            // read the serial value until it reports a LOW (false)
            retryCount++;
            if (isReady()) {
                return;
            }
        } while (retryCount < WAKE_RETRY_LIMIT);

        throw new IOException("Couldn't wake the device");
    }

    public long getValue() throws IOException {
        ready();

        // read the data from the sensor as 3 bytes (24 bits) through 24 pulses
        boolean[] data = new boolean[24];
        for (int i = 0; i < data.length; i++) {
            hx711.pulseHighLow();
            data[i] = hx711.readDat();
        }

        // send 25th pulse to end sequence, keeping device in 128 bit mode.
        hx711.pulseHighLow();

        Timber.v("Read value: %s", Arrays.toString(data));

        return booleanArrayToLong(data);
    }

    public void release() {
        hx711.release();
    }

    public double getWeightUnscaled() throws IOException {
        return getAverageValue(ITERATIONS_PER_READ);
    }

    public double getWeightInGrams() throws IOException {
        return getAverageValue(ITERATIONS_PER_READ) / GRAM_SCALE;
    }

    public double getAverageValue(int iterationCount) throws IOException {
        long sum = 0;
        for (int i = 0; i < iterationCount; i++) {
            long value = getValue();
            Timber.i("value read from sensor=%s", value);
            sum += value;
        }
        return sum / iterationCount;
    }
}
