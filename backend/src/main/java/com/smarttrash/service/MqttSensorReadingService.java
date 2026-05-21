package com.smarttrash.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrash.dto.SmartTrashMqttMessageDTO;
import com.smarttrash.model.SensorReadingCommand;
import com.smarttrash.repository.SensorReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class MqttSensorReadingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSensorReadingService.class);

    private final ObjectMapper objectMapper;
    private final SensorReadingRepository repository;
    private final BinStatusClassifier classifier;

    public MqttSensorReadingService(ObjectMapper objectMapper, SensorReadingRepository repository, BinStatusClassifier classifier) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.classifier = classifier;
    }

    public ProcessingResult processPayload(String payload) {
        SmartTrashMqttMessageDTO message;
        try {
            message = objectMapper.readValue(payload, SmartTrashMqttMessageDTO.class);
        } catch (JsonProcessingException exception) {
            LOGGER.warn("Invalid MQTT payload received: {}", exception.getOriginalMessage());
            return ProcessingResult.INVALID_JSON;
        }

        var errors = validate(message);
        if (!errors.isEmpty()) {
            LOGGER.warn("Rejected MQTT payload for sensor {}: {}", message.sensorId(), String.join("; ", errors));
            return ProcessingResult.INVALID_MESSAGE;
        }

        if (!repository.sensorExists(message.sensorId())) {
            LOGGER.warn("Rejected MQTT payload because sensor {} is not registered", message.sensorId());
            return ProcessingResult.SENSOR_NOT_FOUND;
        }

        var status = classifier.classify(message.fillLevelPercent());
        var command = new SensorReadingCommand(
                message.sensorId().trim(),
                message.timestamp(),
                message.distanceCm(),
                message.fillLevelPercent(),
                status,
                Optional.ofNullable(message.binHeightCm()),
                Optional.ofNullable(message.latitude()),
                Optional.ofNullable(message.longitude()),
                Optional.ofNullable(message.batteryPercent())
        );

        repository.recordReading(command);
        LOGGER.info(
                "Processed MQTT reading for sensor {} at {}: distanceCm={}, fillLevelPercent={}, status={}",
                command.sensorId(),
                command.timestamp(),
                command.distanceCm(),
                command.fillLevelPercent(),
                command.status()
        );
        return ProcessingResult.PROCESSED;
    }

    private static ArrayList<String> validate(SmartTrashMqttMessageDTO message) {
        var errors = new ArrayList<String>();

        if (message.sensorId() == null || message.sensorId().isBlank()) {
            errors.add("sensorId is required");
        }
        if (message.timestamp() == null) {
            errors.add("timestamp is required");
        }
        if (message.distanceCm() == null) {
            errors.add("distanceCm is required");
        } else if (message.distanceCm() < 0) {
            errors.add("distanceCm must be greater than or equal to 0");
        }
        if (message.fillLevelPercent() == null) {
            errors.add("fillLevelPercent is required");
        } else if (message.fillLevelPercent() < 0 || message.fillLevelPercent() > 100) {
            errors.add("fillLevelPercent must be between 0 and 100");
        }
        if (message.binHeightCm() != null && message.binHeightCm() <= 0) {
            errors.add("binHeightCm must be greater than 0");
        }
        if (message.batteryPercent() != null && (message.batteryPercent() < 0 || message.batteryPercent() > 100)) {
            errors.add("batteryPercent must be between 0 and 100");
        }
        if (message.latitude() != null && (message.latitude() < -90 || message.latitude() > 90)) {
            errors.add("latitude must be between -90 and 90");
        }
        if (message.longitude() != null && (message.longitude() < -180 || message.longitude() > 180)) {
            errors.add("longitude must be between -180 and 180");
        }

        return errors;
    }

    public enum ProcessingResult {
        PROCESSED,
        INVALID_JSON,
        INVALID_MESSAGE,
        SENSOR_NOT_FOUND
    }
}
