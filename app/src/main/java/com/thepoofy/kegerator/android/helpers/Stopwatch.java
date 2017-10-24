package com.thepoofy.kegerator.android.helpers;

import android.os.SystemClock;

import timber.log.Timber;

/**
 * @author wvanderhoef
 */

public class Stopwatch {

    private long startTime;

    public Stopwatch() {
        this.startTime = SystemClock.elapsedRealtimeNanos();
    }

    public long clock() {
        long current = SystemClock.elapsedRealtimeNanos();

        Timber.v("Time Elapsed us:\t%s", (current - startTime) / 1000);
        startTime = current;
        return current;
    }

    public long clock(String label) {
        long current = SystemClock.elapsedRealtimeNanos();

        Timber.v("%s, Time Elapsed us:\t%s", label, (current - startTime) / 1000);
        startTime = current;
        return current;
    }
}
