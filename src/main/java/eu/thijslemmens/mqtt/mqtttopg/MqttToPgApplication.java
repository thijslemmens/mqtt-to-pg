package eu.thijslemmens.mqtt.mqtttopg;

import java.sql.Timestamp;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jdbc.JdbcMessageHandler;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
@EnableConfigurationProperties(MqttToPgConfiguration.class)
public class MqttToPgApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqttToPgApplication.class, args);
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter p1MonitorEndpoint(MqttToPgConfiguration configuration) {
        return new MqttPahoMessageDrivenChannelAdapter(configuration.getMqttUrl(),
                configuration.getMqttClientId(), configuration.getMqttTopic());
    }

    private MessageHandler postgresqlMessageHandler(DataSource dataSource) {
        JdbcMessageHandler jdbcMessageHandler = new JdbcMessageHandler(dataSource,
                "INSERT INTO metrics (time, name, value, labels, unit) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING");
        jdbcMessageHandler.setPreparedStatementSetter((ps, m) -> {
            Metric metric = (Metric) m.getPayload();
            ps.setTimestamp(1, Timestamp.valueOf(metric.timestamp()));
            ps.setString(2, metric.name());
            ps.setDouble(3, metric.value());
            ps.setObject(4, metric.labels());
            ps.setString(5, metric.unit());
        });
        return jdbcMessageHandler;
    }

    @Bean
    public IntegrationFlow mqttInbound(DataSource dataSource, MqttPahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter) {
        return IntegrationFlows.from(mqttPahoMessageDrivenChannelAdapter)
                .transform(new DSMSerialMessageToMetricTransformer())
                .filter((Optional<Metric> optionalMetric) -> optionalMetric.isPresent())
                .transform((Optional<Metric> optionalMetric) -> optionalMetric.get())
                .handle(postgresqlMessageHandler(dataSource))
                .get();
    }

}
