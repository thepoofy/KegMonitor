package com.thepoofy.kegerator.android;

import android.app.Activity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.thepoofy.kegerator.android.helpers.HX711GpioHelper;
import com.thepoofy.kegerator.android.helpers.ThermistorI2cHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.thepoofy.kegerator.android.helpers.ThermistorI2cHelper.DEFAULT_I2C_ADDRESS;
import static java.lang.Byte.toUnsignedInt;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

//    I/HX711GpioHelper: GPIO Sockets: [[GPIO1_IO18, GPIO2_IO00, GPIO2_IO01, GPIO2_IO02, GPIO2_IO03, GPIO4_IO19, GPIO4_IO21, GPIO4_IO22, GPIO4_IO23, GPIO5_IO02]]
//    I/ThermistorI2cHelper: I2C Bus Addresses: [[I2C2, I2C3]]

    private static final long TIME_DELAY = 5;
    private static final TimeUnit TIME_DELAY_UNIT = SECONDS;

    private final PeripheralManagerService peripherals = new PeripheralManagerService();
    private final HX711GpioHelper hx711Helper = new HX711GpioHelper(peripherals);
    private final ThermistorI2cHelper thermistorI2cHelper = new ThermistorI2cHelper(peripherals);
    private CompositeDisposable activityDisposables;

    private List<String> i2cAddresses;
    private List<I2cDevice> i2cDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        Timber.plant(new Timber.DebugTree());

        activityDisposables = new CompositeDisposable();

        hx711Helper.logGpioList();
        i2cAddresses = thermistorI2cHelper.logI2cList();

        try {
            hx711Helper.initScales();
            subscribeScaleData();
        } catch (IOException e) {
            Timber.w(e, "Error from HX711");
        }

        initThermistors();
        subscribeThermistorData();
    }

    void subscribeScaleData() {
        final Disposable disposable = Observable.interval(TIME_DELAY,
                                                          TIME_DELAY_UNIT,
                                                          Schedulers.io())
                .subscribe(timeElapsed -> {
                    if (!activityDisposables.isDisposed()) {
                        Timber.i("Starting scale sensor read #%s", timeElapsed);
                        hx711Helper.readScaleSensor();
                    }
                }, throwable -> {
                    Timber.e(throwable, "onError while reading scale data.");
                    activityDisposables.dispose();
                }, () -> {
                    Timber.i("reading scale data has completed.");
                    activityDisposables.dispose();
                });
        activityDisposables.add(disposable);
    }

    void initThermistors() {
        i2cDevices = new ArrayList<>();
        for (String i2cAddress : i2cAddresses) {
            I2cDevice device = thermistorI2cHelper.register(i2cAddress, DEFAULT_I2C_ADDRESS);
            if (device != null) {
                i2cDevices.add(device);
            }
        }
    }

    void subscribeThermistorData() {
        Disposable disposable = Observable.interval(TIME_DELAY, TIME_DELAY_UNIT, Schedulers.io())
                .subscribe(timeElapsed -> {
                    Timber.i("Starting thermistor sensor read #%s", timeElapsed);
                    readThermistors();
                }, t -> {
                    Timber.e(t, "onError while reading thermistor data.");
                }, () -> {
                    Timber.i("reading thermistor data has completed.");
                });

        activityDisposables.add(disposable);
    }

    void readThermistors() throws IOException {
        for (int channel = 0; channel < i2cDevices.size(); channel++) {
            I2cDevice device = i2cDevices.get(channel);

            // CHANNEL (0,1,2,3)
            //byte[] data = thermistorI2cHelper.multipleBytes(device, channel);
            byte data = thermistorI2cHelper.readByte(device, channel);

            Timber.i("Data From i2c device=[%s] channel=[%s] data=[%s]",
                     device.getName(),
                     channel,
                     toUnsignedInt(data));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Timber.w("onDestroy called.");

        hx711Helper.release();

        i2cAddresses.clear();
        for (I2cDevice device : i2cDevices) {
            thermistorI2cHelper.release(device);
        }

        activityDisposables.dispose();
    }
}
