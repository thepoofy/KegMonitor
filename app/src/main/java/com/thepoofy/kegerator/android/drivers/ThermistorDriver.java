package com.thepoofy.kegerator.android.drivers;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

import static java.lang.Byte.toUnsignedInt;

/**
 * @author wvanderhoef
 */
public class ThermistorDriver {

    private final int ADDRESS = 0x48;
    private final I2cDevice device;

    public ThermistorDriver(I2cDevice device) {
        this.device = device;
    }

    public static int byteToInt(byte[] data) {
        Timber.i("Thermistor raw data [%s]", Arrays.toString(data));
        int val1 = toUnsignedInt(data[0]);
        int val2 = toUnsignedInt(data[1]) >> 4;

        Timber.i("Thermistor Register1=[%s] Register2=[%s]", val1, val2);

        return val1 + val2;
    }

    public int read() throws IOException {
        tryEverything();
        return 0;
    }

    public void tryEverything() {
        try {
            Timber.i("read1 %s", String.valueOf(byteToInt(read1())));
        } catch (Throwable t) {
            Timber.w(t, "read1 failed");
        }

        try {
            Timber.i("read2 %s", String.valueOf(byteToInt(read2())));
        } catch (Throwable t) {
            Timber.w(t, "read2 failed");
        }

        try {
            Timber.i("read3 %s", String.valueOf(read3()));
        } catch (Throwable t) {
            Timber.w(t, "read3 failed");
        }
    }

    public byte[] read1() throws IOException {
       byte[] data = new byte[2];
        device.readRegBuffer(ADDRESS, data, data.length);
        Timber.i("Thermistor read1 [%s]", Arrays.toString(data));
        return data;
    }

    private byte[] read2() throws IOException {
        byte[] data = new byte[2];
        device.read(data, ADDRESS);
        Timber.i("Thermistor read2 [%s]", Arrays.toString(data));
        return data;
    }

    private short read3() throws IOException {
        short value = device.readRegWord(ADDRESS);
        Timber.i("Thermistor read3 [%s]", value);
        return value;
    }

    public void release() {
        try {
            device.close();
        } catch (IOException e) {
            Timber.e(e, "Failed to close.");
        }
    }
}



