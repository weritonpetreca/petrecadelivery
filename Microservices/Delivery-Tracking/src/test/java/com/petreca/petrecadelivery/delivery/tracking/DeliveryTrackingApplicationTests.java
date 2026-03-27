package com.petreca.petrecadelivery.delivery.tracking;

import com.petreca.petrecadelivery.delivery.tracking.infrastructure.PostgreSQLTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgreSQLTestContainerConfig.class)
class DeliveryTrackingApplicationTests {

	@Test
	void contextLoads() {
	}

}
