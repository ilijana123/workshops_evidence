package com.example.workshops_evidence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.workshops_evidence", "com.example.activities"})
public class WorkshopsEvidenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkshopsEvidenceApplication.class, args);
	}

}
