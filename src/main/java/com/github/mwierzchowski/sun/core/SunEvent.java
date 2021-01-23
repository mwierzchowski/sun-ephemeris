package com.github.mwierzchowski.sun.core;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class SunEvent implements Serializable {
    private SunEventType type;
    private Instant timestamp;
}
