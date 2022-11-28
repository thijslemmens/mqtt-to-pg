package eu.thijslemmens.mqtt.mqtttopg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.dsl.IntegrationFlow;

@SpringBootTest
class MqttToPgApplicationTests {

	@Autowired
	IntegrationFlow integrationFlow;

	@Test
	public void contextLoads() {
		assertThat(integrationFlow).isNotNull();
	}

}
