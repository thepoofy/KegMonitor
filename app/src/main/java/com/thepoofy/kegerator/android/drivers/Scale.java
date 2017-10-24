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
    private static final int WAKE_RETRY_LIMIT = 10;
    private static final long GRAM_SCALE = 1992;

    private final HX711 hx711;
    private final int iterationsPerRead;
    private final long scale;

    private int offset = 0;

    public Scale(HX711 hx711) {
        this.hx711 = hx711;
        this.iterationsPerRead = ITERATIONS_PER_READ;
        this.scale = GRAM_SCALE;
    }

    public Scale (HX711 hx711, int iterationsPerRead, long scale) {
        this.hx711 = hx711;
        this.iterationsPerRead = iterationsPerRead;
        this.scale = scale;
    }

    /**
     *
     */
    public void release() {
        hx711.release();
    }

    /**
     * @return
     * @throws IOException
     */
    public double getWeightUnscaled() throws IOException {
        return getAverageValue(iterationsPerRead);
    }

    /**
     * @return
     * @throws IOException
     */
    public double getWeightInGrams() throws IOException {
        return getAverageValue(iterationsPerRead) / scale;
    }

    /**
     * @param iterationCount
     * @return
     * @throws IOException
     */
    public double getAverageValue(int iterationCount) throws IOException {
        long sum = 0;
        for (int i = 0; i < iterationCount; i++) {
            long value = getValue();
            Timber.i("value read from sensor=%s", value);
            sum += value;
        }
        return sum / iterationCount;
    }

    public void sleep() throws IOException {
        hx711.sleep();
    }

    long getValue() throws IOException {
        ready();

        hx711.setupPulseMode();

        // read the data from the sensor as 3 bytes (24 bits) through 24 pulses
        boolean[] data = new boolean[24];
        for (int i = 0; i < data.length; i++) {
            hx711.pulseHighLow();
            data[i] = hx711.readDat();
        }

        // send 25th pulse to end sequence, keeping device in 128 bit mode.
        hx711.pulseHighLow();
        hx711.sleep();

        Timber.i("Read value: %s", Arrays.toString(data));
        return booleanArrayToLong(data);
    }

    private void ready() throws IOException {
        int retryCount = 0;

        do {
            hx711.wake();
            // read the serial value until it reports a LOW (false)
            retryCount++;
            if (isReady()) {
                Timber.i("Wake successful on attempt: %s", retryCount);
                return;
            }
        } while (retryCount < WAKE_RETRY_LIMIT);

        throw new IOException("Couldn't wake the device");
    }

    private boolean isReady() throws IOException {
        return !hx711.readDat();
    }
}
