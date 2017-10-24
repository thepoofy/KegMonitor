package com.thepoofy.kegerator.android.helpers;

import android.support.annotation.Nullable;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */
public class CloseableHelper {

    public static void release(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Timber.w(e, "Error closing device.");
            }
        }
    }
}
