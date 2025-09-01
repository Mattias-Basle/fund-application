package com.example.fund_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FundAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
