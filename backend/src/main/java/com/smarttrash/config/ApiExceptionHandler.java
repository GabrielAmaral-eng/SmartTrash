package com.smarttrash.config;

import com.smarttrash.dto.ErrorResponse;
import com.smarttrash.exception.SensorNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SensorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleSensorNotFound(SensorNotFoundException exception) {
        return new ErrorResponse("SENSOR_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleBadRequest(Exception exception) {
        return new ErrorResponse("BAD_REQUEST", exception.getMessage(), Instant.now());
    }
}
