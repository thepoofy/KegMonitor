package com.thepoofy.kegerator.android.drivers;

import android.support.annotation.IntRange;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;

import timber.log.Timber;

import static java.lang.Byte.toUnsignedInt;

/**
 * @author wvanderhoef
 */
public class ThermistorDriver {

    private final I2cDevice input;

    public ThermistorDriver(I2cDevice input) {
        this.input = input;
    }

    public byte[] analogRead(@IntRange(from=0, to=3) int address) throws IOException {
        byte[] data = new byte[2];
        input.read(data, address);
        Timber.i("Thermistor raw data=[%s] Register2=[%s]", data[0], data[1]);

        return data;
    }

    public int analogReadAsInt(@IntRange(from=0, to=3) int address) throws IOException {
        byte[] data = analogRead(address);

        int val1 = toUnsignedInt(data[0]);
        int val2 = toUnsignedInt(data[1]) >> 4;

        Timber.i("Thermistor Register1=[%f] Register2=[%f]", val1, val2);

        return val1 + val2;
    }
}



