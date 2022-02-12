package eu.thijslemmens.mqtt.mqtttopg;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

public class DSMSerialMessageToMetricTransformer implements GenericTransformer<Message, Optional<Metric>> {

    Logger logger = LoggerFactory.getLogger(DSMSerialMessageToMetricTransformer.class);

    private static final Pattern ELECTRICITY_METER_PATTERN = Pattern.compile(
            "\\{\\\"SerialReceived\\\":\\\"1-0:(?<direction>1|2)\\.8\\.(?<tariff>1|2)\\((?<value>\\d{6}\\.\\d{3})\\*kWh\\)\\\\r\\\"\\}");
    private static final Pattern ELECTRICITY_POWER_PATTERN = Pattern.compile(
            "\\{\\\"SerialReceived\\\":\\\"1-0:(?<direction>1|2)\\.7\\.0\\((?<value>\\d{2}\\.\\d{3})\\*kW\\)\\\\r\\\"\\}");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
            "\\{\\\"SerialReceived\\\":\\\"0-0:1\\.0\\.0\\((?<timestamp>\\d{12})W\\)\\\\r\\\"\\}");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final Pattern GAS_METER_PATTERN = Pattern.compile(
            "\\{\\\"SerialReceived\\\":\\\"0-1:24\\.2\\.3\\((?<timestamp>\\d{12})W\\)\\((?<volume>\\d{5}\\.\\d{3})\\*m3\\)\\\\r\\\"\\}");
    private static final Pattern ELECTRICITY_VOLTAGE_PATTERN = Pattern.compile("\\{\\\"SerialReceived\\\":\\\"1-0:32.7.0\\((?<voltage>\\d{3}\\.\\d)\\*V\\)\\\\r\\\"\\}");
    private static final Pattern ELECTRICITY_CURRENT_PATTERN = Pattern.compile("\\{\\\"SerialReceived\\\":\\\"1-0:31.7.0\\((?<current>\\d{3}\\.\\d{2})\\*A\\)\\\\r\\\"\\}");

    private LocalDateTime timestamp = null;

    @Override
    public Optional<Metric> transform(Message source) {
        String payload = (String) source.getPayload();

        Matcher timestampMatcher = TIMESTAMP_PATTERN.matcher(payload);
        if (timestampMatcher.find()) {
            String match = timestampMatcher.group("timestamp");
            logger.debug("Found timestamp {}", match);
            timestamp = stringToLocalDateTime(match);
            return Optional.empty();
        }

        Matcher gasMeterMatcher = GAS_METER_PATTERN.matcher(payload);
        if (gasMeterMatcher.find()) {
            String gasTimestamp = gasMeterMatcher.group("timestamp");
            String volume = gasMeterMatcher.group("volume");
            logger.debug("Gasmeter at {}: {}m3", gasTimestamp, volume);
            return Optional.of(
                    new Metric("gas.meter", Double.parseDouble(volume), stringToLocalDateTime(gasTimestamp), Map.of(),
                            "m3"));
        }

        if (timestamp == null) {
            return Optional.empty();
        }

        Matcher electricityMeterMatcher = ELECTRICITY_METER_PATTERN.matcher(payload);
        if (electricityMeterMatcher.find()) {
            String direction = electricityMeterMatcher.group("direction");
            String tariff = electricityMeterMatcher.group("tariff");
            String value = electricityMeterMatcher.group("value");
            logger.debug(String.format("Direction: %s, Tariff: %s, Value: %s kWh", direction, tariff, value));
            return Optional.of(new Metric("electricity.meter", Double.parseDouble(value), timestamp,
                    Map.of("direction", mapDirection(direction), "tariff", mapTariff(tariff)), "kWh"));
        }

        Matcher powerMatcher = ELECTRICITY_POWER_PATTERN.matcher(payload);
        if (powerMatcher.find()) {
            String direction = powerMatcher.group("direction");
            String power = powerMatcher.group("value");
            logger.debug(String.format("Direction: %s, Value %s kW", direction, power));
            return Optional.of(new Metric("electricity.power", Double.valueOf(power), timestamp,
                    Map.of("direction", mapDirection(direction)), "kW"));
        }

        Matcher voltageMatcher = ELECTRICITY_VOLTAGE_PATTERN.matcher(payload);
        if (voltageMatcher.find()) {
            String voltage = powerMatcher.group("voltage");
            logger.debug("Voltage: {}", voltage);
            return Optional.of(new Metric("electricity.voltage", Double.parseDouble(voltage), timestamp, Map.of(), "V"));
        }

        Matcher currentMatcher = ELECTRICITY_CURRENT_PATTERN.matcher(payload);
        if (currentMatcher.find()) {
            String current = currentMatcher.group("current");
            logger.debug("Current: {}", current);
            return Optional.of(new Metric("electricity.current", Double.parseDouble(current), timestamp, Map.of(), "A"));
        }

        return Optional.empty();
    }

    private static LocalDateTime stringToLocalDateTime(String timestamp) {
        return LocalDateTime.parse(timestamp, TIMESTAMP_FORMAT);
    }

    private static String mapDirection(String match) {
        switch (match) {
            case "1":
                return "in";
            case "2":
                return "out";
            default:
                return match;
        }
    }

    private static String mapTariff(String match) {
        switch (match) {
            case "1":
                return "high";
            case "2":
                return "low";
            default:
                return match;
        }
    }

}
