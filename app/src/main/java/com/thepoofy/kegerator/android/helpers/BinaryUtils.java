package com.thepoofy.kegerator.android.helpers;

import timber.log.Timber;

import static java.lang.Byte.toUnsignedInt;

/**
 * @author wvanderhoef
 */
public class BinaryUtils {

    public static long booleanArrayToLong(boolean[] data) {
        long value = 0;
        for (boolean isTrue : data) {
            value = (value << 1) + (isTrue ? 1 : 0);
        }
        return value;
    }

}
