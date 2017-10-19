package com.thepoofy.kegerator.android.helpers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.thepoofy.kegerator.android.drivers.HX711;
import com.thepoofy.kegerator.android.drivers.Hx711GpioDriver;
import com.thepoofy.kegerator.android.drivers.Hx711PwmDriver;
import com.thepoofy.kegerator.android.drivers.Scale;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class ScaleFactory {

    private static final String GPIO_DAT_ADDRESS = "GPIO2_IO01";
    private static final String GPIO_SCK_ADDRESS = "GPIO2_IO00";
    private static final String PWM_SCK_ADDRESS = "PWM7";

    private final PeripheralManagerService manager;

    public ScaleFactory(PeripheralManagerService manager) {
        this.manager = manager;
    }

    private Gpio configureGpioDat(String address) throws IOException {
        Timber.i("Registering GPIO DAT to address %s", address);
        Gpio dat = manager.openGpio(address);
        dat.setDirection(Gpio.DIRECTION_IN);
        dat.setActiveType(Gpio.ACTIVE_HIGH);
        dat.setEdgeTriggerType(Gpio.EDGE_NONE);

        return dat;
    }

    private Gpio configureGpioSck(String address) throws IOException {
        Timber.i("Registering GPIO SCK to address %s", address);
        Gpio sck = manager.openGpio(address);
        sck.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        sck.setActiveType(Gpio.ACTIVE_HIGH);
        sck.setEdgeTriggerType(Gpio.EDGE_NONE);

        return sck;
    }

    private Pwm configurePwmSck(String address) throws IOException {
        Timber.i("Registering PWM SCK to address %s", address);
        return manager.openPwm(address);
    }

    public Scale createGpioScale() throws IOException {
        List<String> gpioList = manager.getGpioList();
        if (!(gpioList.contains(GPIO_DAT_ADDRESS) && gpioList.contains(GPIO_SCK_ADDRESS))) {
            throw new IOException("Invalid GPIO Constants.");
        }

        Gpio dat = configureGpioDat(GPIO_DAT_ADDRESS);
        Gpio sck = configureGpioSck(GPIO_SCK_ADDRESS);

        return new Scale(new Hx711GpioDriver(dat, sck));
    }

    public Scale createPwmScale() throws IOException {
        List<String> gpioList = manager.getGpioList();
        if (!gpioList.contains(GPIO_DAT_ADDRESS)) {
            throw new IOException("Invalid GPIO Address Constants.");
        }
        List<String> pwmList = manager.getPwmList();
        if (!pwmList.contains(PWM_SCK_ADDRESS)) {
            throw new IOException("Invalid PWM Address Constants.");
        }

        Gpio dat = configureGpioDat(GPIO_DAT_ADDRESS);
        Pwm sck = configurePwmSck(PWM_SCK_ADDRESS);

        return new Scale(new Hx711PwmDriver(dat, sck));
    }
}