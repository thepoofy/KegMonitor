package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.Nullable;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */
public class GpioHelper {

    public static void release(@Nullable Gpio gpio) {
        if (gpio != null) {
            try {
                gpio.close();
            } catch (IOException e) {
                Timber.w(e, "Error releasing gpio");
            }
        }
    }

    public static void logGpioAddresses(PeripheralManagerService manager) {
        List<String> gpioList = manager.getGpioList();
        if (gpioList != null && !gpioList.isEmpty()) {
            Timber.i("GPIO addresses: [%s]", gpioList);
        } else {
            Timber.i("No GPIO addresses found.");
        }
    }
}
