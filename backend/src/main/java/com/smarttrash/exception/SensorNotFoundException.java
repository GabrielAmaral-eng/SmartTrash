package com.smarttrash.exception;

public class SensorNotFoundException extends RuntimeException {

    public SensorNotFoundException(String id) {
        super("Sensor not found: " + id);
    }
}
