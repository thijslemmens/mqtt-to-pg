package eu.thijslemmens.mqtt.mqtttopg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "mqtt-to-pg")
public class MqttToPgConfiguration {

    private String mqttUrl;
    private String mqttTopic;
    private String mqttClientId;

    public String getMqttUrl() {
        return mqttUrl;
    }

    public void setMqttUrl(String mqttUrl) {
        this.mqttUrl = mqttUrl;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public void setMqttClientId(String mqttClientId) {
        this.mqttClientId = mqttClientId;
    }
}
