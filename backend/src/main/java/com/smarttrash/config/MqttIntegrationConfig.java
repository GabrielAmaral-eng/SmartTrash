package com.smarttrash.config;

import com.smarttrash.service.MqttSensorReadingService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
class MqttIntegrationConfig {

    @Bean
    MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter(MqttProperties properties, MessageChannel mqttInputChannel) {
        var options = new MqttConnectOptions();
        options.setServerURIs(new String[]{properties.requiredBrokerUrl()});
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        var clientFactory = new DefaultMqttPahoClientFactory();
        clientFactory.setConnectionOptions(options);

        var adapter = new MqttPahoMessageDrivenChannelAdapter(
                properties.requiredClientId(),
                clientFactory,
                properties.requiredTopic()
        );
        adapter.setCompletionTimeout(5_000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel);
        return adapter;
    }

    @Bean
    IntegrationFlow mqttInboundFlow(MessageChannel mqttInputChannel, MqttSensorReadingService service) {
        return IntegrationFlow.from(mqttInputChannel)
                .handle(String.class, (payload, headers) -> {
                    service.processPayload(payload);
                    return null;
                })
                .get();
    }
}
