/**
 * 
 */
package com.example.youtube.youTubeApi.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.youtube.youTubeApi.config.GlobalProperties;
import com.example.youtube.youTubeApi.constant.YouTubeConstant;
import com.example.youtube.youTubeApi.dto.YouTubeData;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;


/**
 * @author Ashish.Patel
 *
 */

@Component
public class YouTubeDataApiService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	GlobalProperties globalProperties;

    private static final String projectPath = System.getProperty(YouTubeConstant.USER_DIR);

    
    //To make data API request, first define Youtube Object
    private static YouTube youtube;
    
    private static List<YouTubeData> lstYouTubeDatas = new ArrayList<>();

    /**
     * Getvideo method will connect to Youtube Api and fetched the result based on input parameter
     * @param nextTocket - this parameter need to attache on next call for pagination
     */
    public void getVideo(String nextTocken) {
  
        try {
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(globalProperties.getApplicationName()).build();

            YouTube.Search.List youTubeSearchList = youtube.search().list(YouTubeConstant.SEARCH_LIST);
            youTubeSearchList.setKey(globalProperties.getApikey());
            youTubeSearchList.setQ(globalProperties.getDefaultSearchKeyword());
            youTubeSearchList.setPageToken(nextTocken);

            youTubeSearchList.setType(globalProperties.getContentType());

            youTubeSearchList.setFields(globalProperties.getSpecificField());
            youTubeSearchList.setMaxResults(globalProperties.getNumberOfVideoPerRequest());

            // Call the API and write data in file.
            SearchListResponse searchResponse = youTubeSearchList.execute();
            String nextPageToken = searchResponse.getNextPageToken();
            
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null && lstYouTubeDatas.size() < globalProperties.getMaxSizeofData()) {
            		collectYouTubeData(searchResultList.iterator());
            		if(nextPageToken != null &&  nextPageToken.length()>0 ) {
            			getVideo(searchResponse.getNextPageToken());
            		}
            }
            dumpDatainFile();
        } catch (GoogleJsonResponseException e) {
            
            logger.error("There was a service error: -> {} message : -> {}",
            		e.getDetails().getCode(), e.getDetails().getMessage());
            
        }  catch (Exception e) {
        	
        	logger.error("Exception occurred: -> {} ",
            		e.getMessage());
        }
    }

    /*
     * Collect all data in Java Pojo
     *
     * @param iteratorSearchResults Iterator of SearchResults to write in file
     *
     */
    private void collectYouTubeData(Iterator<SearchResult> iteratorSearchResults) {

       
        if (!iteratorSearchResults.hasNext()) {
            logger.info("No data available for specific search");
        }
        
        YouTubeData youTubeData;
        
        while (iteratorSearchResults.hasNext()) {

            SearchResult searchResult = iteratorSearchResults.next();
            ResourceId resourceId = searchResult.getId();
            
            //here first need to confirm that resourceId.getKind is represented a video or not.
            if (resourceId.getKind().equals(globalProperties.getVideoKindContent())) {
                
                youTubeData = new YouTubeData();
                youTubeData.setYouTubeUrl(globalProperties.getDefaultYouTubeUrl()+resourceId.getVideoId());
                youTubeData.setVideoTitle(searchResult.getSnippet().getTitle());
                
                lstYouTubeDatas.add(youTubeData);
                
            }
        }
    }
    
    /*
     * dumpDatainFile method will write YouTube data in specific file.
     *
     */
	private void dumpDatainFile() {
		
		File file = new File(projectPath + File.separator + globalProperties.getDataFileName());
		
		if (file.exists()) {
			file.delete();
		}else {
			try {
				file.createNewFile();
			} catch (IOException io) {
				logger.error("Exception occurred: -> {} ", io.getMessage());
			}
		}
		 try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true))) {
			
			 if(!lstYouTubeDatas.isEmpty()) {
					long index = 1;
					for (YouTubeData youTubeData : lstYouTubeDatas) {
						bufferedWriter.write("Index: "+index++ + "\n");
						bufferedWriter.write(globalProperties.getUrl()+youTubeData.getYouTubeUrl() + "\n");
						bufferedWriter.write(globalProperties.getVideoTitle()+youTubeData.getVideoTitle() + "\n\n");
					}
				}
		 }
			
		catch (IOException e) {
			logger.error("Exception occurred: -> {} ", e.getMessage());
		} 
	}

}
