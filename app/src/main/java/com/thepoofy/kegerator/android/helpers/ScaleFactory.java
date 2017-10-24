package com.thepoofy.kegerator.android.helpers;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.google.android.things.pio.SpiDevice;
import com.thepoofy.kegerator.android.drivers.HX711SpiDriver;
import com.thepoofy.kegerator.android.drivers.Hx711GpioDriver;
import com.thepoofy.kegerator.android.drivers.Hx711PwmDriver;
import com.thepoofy.kegerator.android.drivers.Scale;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class ScaleFactory {

//    private static final String GPIO_DAT_ADDRESS = "GPIO2_IO01";
//    private static final String GPIO_SCK_ADDRESS = "GPIO2_IO00";
//    private static final String PWM_SCK_ADDRESS = "PWM7";
//    private static final String SPI_SCK_ADDRESS = "SPI3";

    private static final String GPIO_DAT_ADDRESS = "GPIO_32";
    private static final String GPIO_SCK_ADDRESS = "GPIO_37";
    private static final String PWM_SCK_ADDRESS = "PWM1";
    private static final String SPI_SCK_ADDRESS = "SPI3.0";
    private final PeripheralManagerService manager;

    public ScaleFactory(PeripheralManagerService manager) {
        this.manager = manager;
    }

    private Gpio configureGpioDat(String address) throws IOException {
        List<String> gpioList = manager.getGpioList();
        if (!gpioList.contains(address)) {
            throw new IOException("Invalid GPIO Address Constants.");
        }

        Timber.i("Registering GPIO DAT to address %s", address);
        Gpio dat = manager.openGpio(address);
        dat.setDirection(Gpio.DIRECTION_IN);
        dat.setActiveType(Gpio.ACTIVE_HIGH);
        dat.setEdgeTriggerType(Gpio.EDGE_NONE);

        return dat;
    }

    private Gpio configureGpioSck(String address) throws IOException {
        List<String> gpioList = manager.getGpioList();
        if (!gpioList.contains(address)) {
            throw new IOException("Invalid GPIO Address Constants.");
        }

        Timber.i("Registering GPIO SCK to address %s", address);
        Gpio sck = manager.openGpio(address);
        sck.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        sck.setActiveType(Gpio.ACTIVE_HIGH);
        sck.setEdgeTriggerType(Gpio.EDGE_NONE);

        return sck;
    }

    private Pwm configurePwmSck(String address) throws IOException {
        List<String> pwmList = manager.getPwmList();
        if (!pwmList.contains(address)) {
            throw new IOException("Invalid PWM Address Constants.");
        }

        Timber.i("Registering PWM SCK to address %s", address);
        return manager.openPwm(address);
    }

    private SpiDevice configureSpiSck(String address) throws IOException {
        List<String> spiList = manager.getSpiBusList();
        if (!spiList.contains(SPI_SCK_ADDRESS)) {
            throw new IOException("Invalid SPI Address Constants.");
        }

        Timber.i("Registering SPI SCK to address %s", address);
        SpiDevice spiDevice = manager.openSpiDevice(address);
        spiDevice.setFrequency(25000);     // 25kHz == 40µs
        spiDevice.setMode(SpiDevice.MODE0);
        spiDevice.setBitJustification(false);   // "endianness" false for most significant bit first
        spiDevice.setBitsPerWord(8);

        return spiDevice;
    }

    private Scale createGpioScale() throws IOException {
        Gpio dat = configureGpioDat(GPIO_DAT_ADDRESS);
        Gpio sck = configureGpioSck(GPIO_SCK_ADDRESS);

        return new Scale(new Hx711GpioDriver(dat, sck));
    }

    private Scale createPwmScale() throws IOException {
        Gpio dat = configureGpioDat(GPIO_DAT_ADDRESS);
        Pwm sck = configurePwmSck(PWM_SCK_ADDRESS);

        return new Scale(new Hx711PwmDriver(dat, sck));
    }

    private Scale createSpiScale() throws IOException {
        Gpio dat = configureGpioDat(GPIO_DAT_ADDRESS);
        SpiDevice sck = configureSpiSck(SPI_SCK_ADDRESS);

        return new Scale(new HX711SpiDriver(dat, sck));
    }

    public Scale createScale(ScaleType type) throws IOException {
        switch (type) {
            case PWM:
                return createPwmScale();
            case GPIO:
                return createGpioScale();
            case SPI:
                return createSpiScale();
        }
        throw new RuntimeException("Scale Type unknown.");
    }

    public enum ScaleType {
        GPIO,
        PWM,
        SPI;
    }
}