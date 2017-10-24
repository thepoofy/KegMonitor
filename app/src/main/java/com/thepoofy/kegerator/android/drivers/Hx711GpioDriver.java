package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;
import com.thepoofy.kegerator.android.helpers.CloseableHelper;
import com.thepoofy.kegerator.android.helpers.Stopwatch;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Will Vanderhoef
 */
public class Hx711GpioDriver implements HX711 {

    private static final boolean HIGH = true;
    private static final boolean LOW = false;

    private final Gpio dat;
    private final Gpio sck;

    public Hx711GpioDriver(Gpio dat, Gpio sck) {
        this.dat = dat;
        this.sck = sck;
    }

    @Override
    public void sleep() throws IOException {
        Timber.d("\t\tAttempting to sleep HX711.");
        Stopwatch stopwatch = new Stopwatch();
        writeValue(LOW);
        stopwatch.clock();
        writeValue(HIGH);
        stopwatch.clock();
    }

    @Override
    public void wake() throws IOException {
        Timber.d("\t\tAttempting to wake HX711.");
        writeValue(LOW);
    }

    @Override
    public boolean readDat() throws IOException {
        return readValue();
    }

    @Override
    public void setupPulseMode() throws IOException {

    }

    @Override
    public void pulseHighLow() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        writeValue(HIGH);
        stopwatch.clock();
        writeValue(LOW);
        stopwatch.clock();
    }

    @Override
    public void release() {
        CloseableHelper.release(dat);
        CloseableHelper.release(sck);
    }

    private boolean readValue() throws IOException {
        boolean isHigh = dat.getValue();
        Timber.d("\t\t\tget DAT: %s", isHigh);
        return isHigh;
    }

    private void writeValue(boolean isHigh) throws IOException {
        Timber.d("\t\t\tset SCK: %s", isHigh);
        sck.setValue(isHigh);
    }
}
