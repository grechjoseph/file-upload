package com.jg.largefileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class LargeFileUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(LargeFileUploadApplication.class, args);
	}

}
