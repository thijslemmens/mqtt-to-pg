package eu.thijslemmens.mqtt.mqtttopg;

import java.time.LocalDateTime;
import java.util.Map;

public record Metric(String name, double value, LocalDateTime timestamp, Map<String, String> labels, String unit){};