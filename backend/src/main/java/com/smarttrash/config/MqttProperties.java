package com.smarttrash.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
public record MqttProperties(
        boolean enabled,
        String brokerUrl,
        String clientId,
        String topic
) {
    public String requiredBrokerUrl() {
        if (brokerUrl == null || brokerUrl.isBlank()) {
            throw new IllegalStateException("mqtt.broker-url must be configured");
        }
        return brokerUrl;
    }

    public String requiredClientId() {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("mqtt.client-id must be configured");
        }
        return clientId;
    }

    public String requiredTopic() {
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("mqtt.topic must be configured");
        }
        return topic;
    }
}
