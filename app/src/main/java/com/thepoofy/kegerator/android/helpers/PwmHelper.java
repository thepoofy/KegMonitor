package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.Nullable;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */
public class PwmHelper {

    public static void release(@Nullable Pwm pwm) {
        if (pwm != null) {
            try {
                pwm.close();
            } catch (IOException e) {
                Timber.w(e, "Error releasing Pwm");
            }
        }
    }

    public static void logPwmAddresses(PeripheralManagerService manager) {
        List<String> pwmList = manager.getPwmList();
        if (pwmList != null && !pwmList.isEmpty()) {
            Timber.i("PWM addresses: [%s]", pwmList);
        } else {
            Timber.i("No PWM addresses found.");
        }
    }
}
