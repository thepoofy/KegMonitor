package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Adapted from HX711 http://image.dfrobot.com/image/data/SEN0160/hx711_english.pdf
 *
 * @author Will Vanderhoef
 */
public class Hx711Driver {

    private static final boolean HIGH = true;
    private static final boolean LOW = false;
    private static final int WAKE_RETRY_LIMIT = 100;

    private final Gpio dat;
    private final Gpio sck;

    public Hx711Driver(Gpio dat, Gpio sck) {
        this.dat = dat;
        this.sck = sck;
    }

    public static long booleanArrayToLong(boolean[] data) {
        long value = 0;
        for (boolean isTrue : data) {
            value = (value << 1) + (isTrue ? 1 : 0);
        }
        return value;
    }

    public void sleep() throws IOException {
        Timber.v("Attempting to sleep HX711.");
        // Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.
        writeSerialValue(LOW);
        writeSerialValue(HIGH);
    }

    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        // if the controller is asleep, setting the pin to low will wake it.
        writeSerialValue(LOW);
    }

    private boolean isReady() throws IOException {
        return readSerialValue() == LOW;
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
            writeSerialValue(HIGH);
            data[i] = readSerialValue();
            writeSerialValue(LOW);
        }

        // send 25th pulse to end sequence, keeping device in 128 bit mode.
        writeSerialValue(HIGH);
        writeSerialValue(LOW);

        Timber.v("Read value: %s", Arrays.toString(data));

        return booleanArrayToLong(data);
    }

    /**
     * @return True if value is HIGH
     * @throws IOException on error reading value
     */
    private boolean readSerialValue() throws IOException {
        return dat.getValue();
    }

    /**
     * @param isHighValue True if HIGH
     * @throws IOException on error writing value
     */
    private void writeSerialValue(boolean isHighValue) throws IOException {
        sck.setValue(isHighValue);
    }
}
