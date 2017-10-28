package com.thepoofy.kegerator.android.drivers;

import android.os.SystemClock;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;
import com.thepoofy.kegerator.android.helpers.CloseableHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * @author Will Vanderhoef
 */
public class Hx711HybridDriver implements HX711 {

    private static final double PULSE_DUTY_CYCLE = 0.0;
    private static final double PULSE_HERTZ = 100;

    private final Gpio dat;
    private final Gpio gpioSck;
    private final Pwm pwmSck;

    public Hx711HybridDriver(Gpio dat, Gpio gpioSck, Pwm pwmSck) {
        this.dat = dat;
        this.gpioSck = gpioSck;
        this.pwmSck = pwmSck;
    }

    @Override
    public boolean readDat() throws IOException {
        List<Boolean> values = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            values.add(readValue());
        }

        Timber.i("Scale Values sample %s", Arrays.toString(values.toArray()));

        return !values.contains(Boolean.FALSE);
    }

    @Override
    public void setupPulseMode() throws IOException {
        pwmSck.setPwmDutyCycle(PULSE_DUTY_CYCLE);
        pwmSck.setPwmFrequencyHz(PULSE_HERTZ);
    }

    @Override
    public void pulseHighLow() throws IOException {
        pwmSck.setEnabled(true);
        // TODO consider a sleep
        pwmSck.setEnabled(false);
    }

    @Override
    public void sleep() throws IOException {
        writeValue(true);
        SystemClock.sleep(100);
    }

    @Override
    public void wake() throws IOException {
        writeValue(false);
    }

    @Override
    public void release() {
        CloseableHelper.release(dat);
        CloseableHelper.release(pwmSck);
    }

    private boolean readValue() throws IOException {
        boolean isHigh = dat.getValue();
        Timber.v("\t\t\tget DAT: %s", isHigh);
        return isHigh;
    }

    private void writeValue(boolean isHigh) throws IOException {
        Timber.d("\t\t\tset SCK: %s", isHigh);
        gpioSck.setValue(isHigh);
    }
}
