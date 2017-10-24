package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.SpiDevice;
import com.thepoofy.kegerator.android.helpers.CloseableHelper;
import com.thepoofy.kegerator.android.helpers.Stopwatch;

import java.io.IOException;

import timber.log.Timber;

/**
 * @author Will Vanderhoef
 */
public class HX711SpiDriver implements HX711 {

    private final Gpio dat;
    private final SpiDevice sck;

    public HX711SpiDriver(Gpio dat, SpiDevice sck) {
        this.dat = dat;
        this.sck = sck;
    }

    @Override
    public boolean readDat() throws IOException {
        return readValue();
    }

    @Override
    public void pulseHighLow() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        writePulseByte();
        stopwatch.clock("pulse sent");
    }

    @Override
    public void sleep() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        writeHighByte();
        stopwatch.clock("high byte sent");
    }

    @Override
    public void wake() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        writeLowByte();
        stopwatch.clock("low byte sent");
    }

    @Override
    public void release() {
        CloseableHelper.release(dat);
        CloseableHelper.release(sck);
    }

    @Override
    public void setupPulseMode() throws IOException {

    }

    private boolean readValue() throws IOException {
        boolean isHigh = dat.getValue();
        Timber.d("\t\t\tget DAT: %s", isHigh);
        return isHigh;
    }

    private void writePulseByte() throws IOException {
        Timber.d("\t\t\tset SCK: %s", 0b0000_1000);
        sck.write(new byte[]{0b0000_1000}, 1);
    }

    private void writeHighByte() throws IOException {
        Timber.d("\t\t\tset SCK: %s", Byte.MAX_VALUE);
        sck.write(new byte[]{Byte.MAX_VALUE}, 1);
    }

    private void writeLowByte() throws IOException {
        Timber.d("\t\t\tset SCK: %s", 0);
        sck.write(new byte[]{0}, 1);
    }
}
