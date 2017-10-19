package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.Nullable;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.thepoofy.kegerator.android.drivers.Hx711Driver;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

public class HX711GpioHelper {

    private static final String GPIO_OUTPUT = "GPIO2_IO00";
    private static final String GPIO_INPUT = "GPIO2_IO01";
    private static final long gramScale = 1992;
    private static final int ITERATIONS_PER_READ = 10;

    private final PeripheralManagerService manager;

    @Nullable private Gpio deviceInput;
    @Nullable private Gpio deviceOutput;
    @Nullable private Hx711Driver sensorDriver;

    public HX711GpioHelper(PeripheralManagerService manager) {
        this.manager = manager;
    }

    public void logGpioList() {
        List<String> gpioList = manager.getGpioList();
        if (gpioList != null && !gpioList.isEmpty()) {
            Timber.i("GPIO Sockets: [%s]", gpioList);
        } else {
            Timber.i("No GPIO Sockets found.");
        }
    }

    private void configureInput(String address) throws IOException {
        Timber.i("Registering Input GPIO to %s", address);
        deviceInput = manager.openGpio(address);
        deviceInput.setDirection(Gpio.DIRECTION_IN);
        deviceInput.setActiveType(Gpio.ACTIVE_HIGH);
        deviceInput.setEdgeTriggerType(Gpio.EDGE_NONE);
    }

    private void configureOutput(String address) throws IOException {
        Timber.i("Registering Output GPIO to %s", address);
        deviceOutput = manager.openGpio(address);
        deviceOutput.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        deviceOutput.setActiveType(Gpio.ACTIVE_HIGH);
        deviceOutput.setEdgeTriggerType(Gpio.EDGE_NONE);
    }

    public void release() {
        release(deviceInput);
        deviceInput = null;

        release(deviceOutput);
        deviceOutput = null;
    }

    private void release(@Nullable Gpio gpio) {
        if (gpio != null) {
            try {
                gpio.close();
            } catch (IOException e) {
                Timber.w(e, "Error releasing gpio");
            }
        }
    }

    public void initScales() throws IOException {
        List<String> gpioList = manager.getGpioList();
        if (!(gpioList.contains(GPIO_INPUT) && gpioList.contains(GPIO_OUTPUT))) {
            throw new IOException("Invalid GPIO Constants.");
        }

        configureInput(GPIO_INPUT);
        configureOutput(GPIO_OUTPUT);

        if (deviceInput != null && deviceOutput != null) {
            sensorDriver = new Hx711Driver(deviceInput, deviceOutput);
            sensorDriver.wake();
        } else {
            throw new RuntimeException("GPIO's not configured.");
        }
    }

    public void readScaleSensor() throws IOException {
        if (sensorDriver != null) {
            Timber.i("HX711=[%s]", sensorDriver.getValue());
        } else {
            Timber.e("SensorDriver is null!");
        }
    }

    public double getWeightUnscaled() throws IOException {
        return getAverageValue(ITERATIONS_PER_READ);
    }

    public double getWeightInGrams() throws IOException {
        return getAverageValue(ITERATIONS_PER_READ) / gramScale;
    }

    public double getAverageValue(int iterationCount) throws IOException {
        if (sensorDriver == null) {
            throw new RuntimeException("SensorDriver not configured");
        }

        long sum = 0;
        for (int i = 0; i < iterationCount; i++) {
            long value = sensorDriver.getValue();
            Timber.i("value read from sensor=%s", value);
            sum += value;
        }
        return sum / iterationCount;
    }
}