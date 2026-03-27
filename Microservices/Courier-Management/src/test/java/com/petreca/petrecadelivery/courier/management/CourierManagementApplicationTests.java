package com.petreca.petrecadelivery.courier.management;

import com.petreca.petrecadelivery.courier.management.infrastructure.PostgreSQLTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgreSQLTestContainerConfig.class)
class CourierManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
