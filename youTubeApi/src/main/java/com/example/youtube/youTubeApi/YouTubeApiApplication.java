package com.example.youtube.youTubeApi;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.youtube.youTubeApi.service.YouTubeDataApiService;

@SpringBootApplication
public class YouTubeApiApplication {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	YouTubeDataApiService youTubeDataApiService;

	public static void main(String[] args) {
		SpringApplication.run(YouTubeApiApplication.class, args);
	}
	
	@PostConstruct
	public void initIt() {
		logger.info("Call to YouTubeDataApiService ========= >");
		youTubeDataApiService.getVideo("");
	}

}
