package com.thepoofy.kegerator.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.things.pio.PeripheralManagerService;
import com.thepoofy.kegerator.android.drivers.Scale;
import com.thepoofy.kegerator.android.drivers.ThermistorDriver;
import com.thepoofy.kegerator.android.helpers.LoggingHelper;
import com.thepoofy.kegerator.android.helpers.ScaleFactory;
import com.thepoofy.kegerator.android.helpers.ThermistorI2cHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.thepoofy.kegerator.android.helpers.ScaleFactory.ScaleType.PWM;
import static com.thepoofy.kegerator.android.helpers.ThermistorI2cHelper.DEFAULT_I2C_ADDRESS;
import static com.thepoofy.kegerator.android.helpers.ThermistorI2cHelper.I2C_PIN;
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

    private static boolean ENABLE_SCALE = true;
    private static boolean ENABLE_THERMISTOR = false;

    private final PeripheralManagerService peripherals = new PeripheralManagerService();
    private final ScaleFactory scaleFactory = new ScaleFactory(peripherals);
    private final ThermistorI2cHelper thermistorI2cHelper = new ThermistorI2cHelper(peripherals);

    private Disposable scaleDisposable;
    private Disposable thermistorDisposable;
    @Nullable
    private ThermistorDriver thermistor;
    @Nullable
    private Scale scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Fabric.with(this, new Crashlytics());
        Timber.plant(new Timber.DebugTree());

        scaleDisposable = new CompositeDisposable();
        thermistorDisposable = new CompositeDisposable();

        LoggingHelper.logGpioAddresses(peripherals);
        LoggingHelper.logPwmAddresses(peripherals);
        LoggingHelper.logSpiAddresses(peripherals);

        startScale();

        startThermistor();
    }

    private void startScale() {
        if (ENABLE_SCALE) {
            try {
                scale = scaleFactory.createScale(PWM);
                scale.sleep();

                subscribeScaleData(scale);
            } catch (IOException e) {
                Timber.w(e, "Error from Scale");
            }
        }
    }

    private void startThermistor() {
        if (ENABLE_THERMISTOR) {
            try {
                thermistor = thermistorI2cHelper.register(I2C_PIN, DEFAULT_I2C_ADDRESS);
                subscribeThermistorData(thermistor);
            } catch (IOException e) {
                Timber.w(e, "Error from Thermistor");
            }
        }
    }

    void subscribeScaleData(final Scale scale) {
        scaleDisposable = Observable.interval(TIME_DELAY, TIME_DELAY_UNIT, Schedulers.io())
                .subscribe(timeElapsed -> {
                    if (!scaleDisposable.isDisposed()) {
                        double averageValue = scale.getWeightInGrams();
                        Timber.i("Scale weightInGrams=[%s]", averageValue);
                    } else {
                        Timber.w("Scale is disposed.");
                    }
                }, throwable -> {
                    Timber.e(throwable, "onError while reading scale data.");
                    scaleDisposable.dispose();
                }, () -> {
                    Timber.i("reading scale data has completed.");
                    scaleDisposable.dispose();
                });
    }

    void subscribeThermistorData(final ThermistorDriver thermistor) {
        thermistorDisposable = Observable.interval(TIME_DELAY, TIME_DELAY_UNIT, Schedulers.io())
                .subscribe(timeElapsed -> {
                    if (!thermistorDisposable.isDisposed()) {
                        Timber.i("Starting thermistor read #%s", timeElapsed);
                        thermistor.read();
                    } else {
                        Timber.w("thermistor is disposed.");
                    }
                }, t -> {
                    Timber.e(t, "onError while reading thermistor data.");
                    thermistorDisposable.dispose();
                }, () -> {
                    Timber.i("reading thermistor data has completed.");
                    thermistorDisposable.dispose();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Timber.w("onDestroy called.");

        if (scale != null) {
            scale.release();
            scale = null;
        }

        thermistorI2cHelper.release(thermistor);
        thermistor = null;

        scaleDisposable.dispose();
        thermistorDisposable.dispose();
    }
}
