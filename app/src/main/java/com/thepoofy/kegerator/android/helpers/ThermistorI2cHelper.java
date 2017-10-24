package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.Nullable;

import com.google.android.things.pio.PeripheralManagerService;
import com.thepoofy.kegerator.android.drivers.ThermistorDriver;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */

public class ThermistorI2cHelper {
    public static final String I2C_PIN = "I2C3";
    public static final int DEFAULT_I2C_ADDRESS = 0x48;

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

    public ThermistorDriver register(String name, int address) throws IOException {
        return new ThermistorDriver(manager.openI2cDevice(name, address));
    }

    public void release(@Nullable ThermistorDriver thermistor) {
        if (thermistor != null) {
            thermistor.release();
        }
    }
}
