package com.example.fund_app;

import org.springframework.boot.SpringApplication;

public class TestFundAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(FundAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
