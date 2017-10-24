package com.thepoofy.kegerator.android.helpers;

import com.google.android.things.pio.PeripheralManagerService;

import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */

public class LoggingHelper {

    public static void logGpioAddresses(PeripheralManagerService manager) {
        log("GPIO", manager.getGpioList());
    }

    public static void logPwmAddresses(PeripheralManagerService manager) {
        log("PWM", manager.getPwmList());
    }

    public static void logSpiAddresses(PeripheralManagerService manager) {
        log("SPI", manager.getSpiBusList());
    }

    private static void log(String label, List<String> addressList) {
        if (addressList != null && !addressList.isEmpty()) {
            Timber.i("%s addresses: [%s]", label, addressList);
        } else {
            Timber.i("No %s addresses found.", label);
        }
    }
}
