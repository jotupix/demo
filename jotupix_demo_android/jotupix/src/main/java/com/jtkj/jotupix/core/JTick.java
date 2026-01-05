package com.jtkj.jotupix.core;

public class JTick {

    public static boolean timeAfter(long a, long b) {
        return ((b) - (a) < 0);
    }

    public static boolean timeBefore(long a, long b) {
        return timeAfter(b, a);
    }

    public static boolean timeAfterEq(long a, long b) {
        return ((a) - (b) >= 0);
    }

    public static boolean timeBeforeEq(long a, long b) {
        return timeAfterEq(b, a);
    }

    public static long getNextTick(long start, long interval) {
        return (start) + (interval);
    }
}
