package com.github.mwierzchowski.sun.core;

public enum SunEventType {
    DAWN,
    SUNRISE,
    NOON,
    SUNSET,
    DUSK;

    public String description() {
        return toString().toLowerCase();
    }
}
