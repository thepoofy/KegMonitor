package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.Pwm;
import com.thepoofy.kegerator.android.helpers.GpioHelper;
import com.thepoofy.kegerator.android.helpers.PwmHelper;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

import static com.thepoofy.kegerator.android.helpers.BinaryUtils.booleanArrayToLong;

/**
 * Adapted from HX711 http://image.dfrobot.com/image/data/SEN0160/hx711_english.pdf
 *
 * @author Will Vanderhoef
 */
public class Hx711PwmDriver implements HX711{

    private static final double ON_DUTY_CYCLE = 100;
    private static final double PULSE_DUTY_CYCLE = 1;
    private static final double PULSE_HERTZ = 1000;
    private final Gpio dat;
    private final Pwm sck;

    public Hx711PwmDriver(Gpio dat, Pwm sck) {
        this.dat = dat;
        this.sck = sck;
    }

    @Override
    public boolean readDat() throws IOException {
        return dat.getValue();
    }

    @Override
    public void pulseHighLow() throws IOException {
        sck.setPwmDutyCycle(PULSE_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        sck.setEnabled(true);
        sck.setEnabled(false);
    }

    @Override
    public void sleep() throws IOException {
        Timber.v("Attempting to sleep HX711.");
        // Setting the pin to HIGH for > 60 ums will put the controller in sleep mode.
        sck.setPwmDutyCycle(ON_DUTY_CYCLE);
        sck.setPwmFrequencyHz(PULSE_HERTZ);
        sck.setEnabled(true);
        sck.setEnabled(false);
    }

    @Override
    public void wake() throws IOException {
        Timber.v("Attempting to wake HX711.");
        // if the controller is asleep, setting the pin to low will wake it.
        sck.setEnabled(false);
    }

    @Override
    public void release() {
        GpioHelper.release(dat);
        PwmHelper.release(sck);
    }
}
