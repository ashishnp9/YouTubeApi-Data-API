/**
 * 
 */
package com.example.youtube.youTubeApi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ashish.Patel
 *
 */

@Component
@PropertySource("classpath:globalConfig.properties")
@ConfigurationProperties
@Getter
@Setter
public class GlobalProperties {
	
	private String apikey;
	private String defaultYouTubeUrl;
	private String dataFileName;
	private String defaultSearchKeyword;
	private String applicationName;
	private String contentType;
	private String specificField;
	private long numberOfVideoPerRequest;
	private long maxSizeofData;
	private String url;
	private String videoTitle;
	private String videoKindContent;

}
