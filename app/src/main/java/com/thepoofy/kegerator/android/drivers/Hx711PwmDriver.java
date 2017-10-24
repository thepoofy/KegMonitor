package com.thepoofy.kegerator.android.drivers;

import android.os.SystemClock;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;
import com.thepoofy.kegerator.android.helpers.CloseableHelper;
import com.thepoofy.kegerator.android.helpers.Stopwatch;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Will Vanderhoef
 */
public class Hx711PwmDriver implements HX711 {

    private static final double ON_DUTY_CYCLE = 100;
    private static final double PULSE_DUTY_CYCLE = 0;
    private static final double PULSE_HERTZ = 1;
    private final Gpio dat;
    private final Pwm sck;

    public Hx711PwmDriver(Gpio dat, Pwm sck) {
        this.dat = dat;
        this.sck = sck;
    }

    @Override
    public boolean readDat() throws IOException {
        return readValue();
    }

    @Override
    public void setupPulseMode() throws IOException {
//        Stopwatch stopwatch = new Stopwatch();
        sck.setEnabled(false);
        sck.setPwmDutyCycle(PULSE_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
//        stopwatch.clock("setup pwm");
    }

    @Override
    public void pulseHighLow() throws IOException {
//        Stopwatch stopwatch = new Stopwatch();
        sck.setEnabled(true);
//        stopwatch.clock("enable pwm");
        sck.setEnabled(false);
//        stopwatch.clock("disable pwm");
    }

    @Override
    public void sleep() throws IOException {
        Timber.v("Attempting to sleep HX711.");
        Stopwatch stopwatch = new Stopwatch();
        sck.setEnabled(false);
        stopwatch.clock("disable pwm");
        sck.setPwmDutyCycle(ON_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        stopwatch.clock("setup pwm");
        sck.setEnabled(true);
        stopwatch.clock("enable pwm");
        SystemClock.sleep(10);
    }

    @Override
    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        sck.setEnabled(false);
        sck.setPwmDutyCycle(0.01);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        sck.setEnabled(true);
        SystemClock.sleep(1);
        sck.setEnabled(false);
    }

    @Override
    public void release() {
        CloseableHelper.release(dat);
        CloseableHelper.release(sck);
    }

    private boolean readValue() throws IOException {
        boolean isHigh = dat.getValue();
        Timber.v("\t\t\tget DAT: %s", isHigh);
        return isHigh;
    }
}
