package com.infomaximum.im_event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ImEventApplication {

	public static ConfigurableApplicationContext run;

	public static void main(String[] args) {
		 run = SpringApplication.run(ImEventApplication.class, args);
	}

}
