package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Adapted from HX711 http://image.dfrobot.com/image/data/SEN0160/hx711_english.pdf
 *
 * @author Will Vanderhoef
 */
public class Hx711PwmDriver {

    private static final int WAKE_RETRY_LIMIT = 100;
    private static final double ON_DUTY_CYCLE = 100;
    private static final double PULSE_DUTY_CYCLE = 1;
    private static final double PULSE_HERTZ = 1000;
    private final Gpio dat;
    private final Pwm sck;

    public Hx711PwmDriver(Gpio dat, Pwm sck) {
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

    private boolean isReady() throws IOException {
        return !readSerialValue();
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
            pulseHighLow();
            data[i] = readSerialValue();
        }

        // send 25th pulse to end sequence, keeping device in 128 bit mode.
        pulseHighLow();

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
     * @throws IOException on error writing value
     */
    private void pulseHighLow() throws IOException {

        sck.setPwmDutyCycle(PULSE_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        sck.setEnabled(true);
        sck.setEnabled(false);
    }

    public void sleep() throws IOException {
        Timber.v("Attempting to sleep HX711.");
        // Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.
        sck.setPwmDutyCycle(ON_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        sck.setEnabled(true);
        sck.setEnabled(false);
    }

    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        // if the controller is asleep, setting the pin to low will wake it.
        sck.setEnabled(false);
    }
}
