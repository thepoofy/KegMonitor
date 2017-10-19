package com.thepoofy.kegerator.android.drivers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author wvanderhoef
 */

public class Hx711DriverTest {

    @Test
    public void booleanArrayToLong_whenEmpty_shouldReturnZero() {
        // given
        boolean[] data = new boolean[]{};

        // when & then
        assertThat(Hx711Driver.booleanArrayToLong(data), equalTo(0L));
    }

    @Test
    public void booleanArrayToLong_whenZeros_shouldReturnZero() {
        // given
        boolean[] data = new boolean[]{false, false, false};

        // when & then
        assertThat(Hx711Driver.booleanArrayToLong(data), equalTo(0L));
    }

    @Test
    public void booleanArrayToLong_whenOne_shouldReturnOne() {
        // given
        boolean[] data = new boolean[]{false, false, true};

        // when & then
        assertThat(Hx711Driver.booleanArrayToLong(data), equalTo(1L));
    }

    @Test
    public void booleanArrayToLong_whenFive_shouldReturnFive() {
        // given
        boolean[] data = new boolean[]{true, false, true};

        // when & then
        assertThat(Hx711Driver.booleanArrayToLong(data), equalTo(5L));
    }
}
