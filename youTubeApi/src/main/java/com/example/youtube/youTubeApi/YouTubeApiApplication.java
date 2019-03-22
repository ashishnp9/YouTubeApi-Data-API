package com.example.youtube.youTubeApi;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.youtube.youTubeApi.service.YouTubeDataApiService;

@SpringBootApplication
public class YouTubeApiApplication {
	
	@Autowired
	YouTubeDataApiService apiService;

	public static void main(String[] args) {
		SpringApplication.run(YouTubeApiApplication.class, args);
	}
	
	@PostConstruct
	public void initIt() {
		System.out.println("===================================>>>>>>>>>>>>>>");
		apiService.getVideo("");
	}

}
