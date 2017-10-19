package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;
import com.thepoofy.kegerator.android.helpers.GpioHelper;

import java.io.IOException;

import timber.log.Timber;

/**
 * Adapted from HX711 http://image.dfrobot.com/image/data/SEN0160/hx711_english.pdf
 *
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
        Timber.v("Attempting to sleep HX711.");
        // Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.
        sck.setValue(LOW);
        sck.setValue(HIGH);
    }

    @Override
    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        // if the controller is asleep, setting the pin to low will wake it.
        sck.setValue(LOW);
    }

    @Override
    public boolean readDat() throws IOException {
        return dat.getValue();
    }

    @Override
    public void pulseHighLow() throws IOException {
        sck.setValue(HIGH);
        sck.setValue(LOW);
    }

    @Override
    public void release() {
        GpioHelper.release(dat);
        GpioHelper.release(sck);
    }
}
