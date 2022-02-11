package eu.thijslemmens.mqtt.mqtttopg;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

@SpringBootApplication
public class MqttToPgApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttToPgApplication.class, args);
	}

	@Bean
	public MqttPahoMessageDrivenChannelAdapter p1MonitorEndpoint() {
		return new MqttPahoMessageDrivenChannelAdapter("tcp://192.168.0.23:1883",
				"testClient", "tele/p1monitor/RESULT" );
	}

	@Bean
	public IntegrationFlow mqttInbound() {
		return IntegrationFlows.from(p1MonitorEndpoint())
                .transform(new DSMSerialMessageToMetricTransformer())
				.filter((Optional<Metric> optionalMetric) -> optionalMetric.isPresent())
				.transform((Optional<Metric> optionalMetric) -> optionalMetric.get())
				.handle(m -> {
					System.out.println(m.getPayload());
				})
				.get();
	}

}
