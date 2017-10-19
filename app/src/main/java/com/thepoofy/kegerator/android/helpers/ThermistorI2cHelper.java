package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */

public class ThermistorI2cHelper {
    public static final int DEFAULT_I2C_ADDRESS = 0x48;
    private static final int REG_BUFFER_BYTE_LIMIT = 2;
    private final PeripheralManagerService manager;

    public ThermistorI2cHelper(PeripheralManagerService manager) {
        this.manager = manager;
    }

    public List<String> logI2cList() {
        List<String> i2cBusList = manager.getI2cBusList();
        if (i2cBusList != null && !i2cBusList.isEmpty()) {
            Timber.i("I2C Bus Addresses: [%s]", i2cBusList);
        } else {
            Timber.i("No I2C Bus addresses found.");
        }
        return i2cBusList;
    }

    public I2cDevice register(String name, int address) {
        try {
            return manager.openI2cDevice(name, address);
        } catch (IOException e) {
            Timber.w("Unable to access I2C", e);
            return null;
        }
    }

    public void release(@Nullable I2cDevice i2cDevice) {
        if (i2cDevice != null) {
            try {
                i2cDevice.close();
            } catch (IOException e) {
                Timber.e(e, "Unable to release I2C");
            }
        }
    }

    public byte readByte(I2cDevice device, @IntRange(from = 0, to = 3) int channel)
            throws IOException {
        return device.readRegByte(channel);
    }

    public byte[] multipleBytes(I2cDevice device, @IntRange(from = 0, to = 3) int channel)
            throws IOException {
        // Read three consecutive register values
        byte[] data = new byte[REG_BUFFER_BYTE_LIMIT];

        device.readRegBuffer(channel, data, data.length);
        return data;
    }
}
