/**
 * 
 */
package com.example.youtube.youTubeApi.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	
	 /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${youtube.apikey}")
    private String apiKey;
	
	@Value("${youtube.dataFileName}")
    private String youTubeDataFileName;
	
	@Value("${youtube.defaultUrl}")
	private String defaultyouTubeUrl;
	
	@Value("${youtube.defaultSearchKeyword}")
	private String defaultSearchKeyword;
	

    private static final long NUMBER_OF_VIDEOS_RETURNED = 2;
    private static final String projectPath = System.getProperty("user.dir");

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;
    
    private static List<YouTubeData> lstYouTubeDatas = new ArrayList<>();

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param args command line args.
     */
//    public static void main(String[] args) {
//    	getVideo("");
//    }
    
    public void getVideo(String nextTocken) {
    	  // Read the developer key from the properties file.
//        Properties properties = new Properties();
//        try {
//            InputStream in = YouTubeDataApiService.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
//            properties.load(in);
//
//        } catch (IOException e) {
//            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
//                    + " : " + e.getMessage());
//            System.exit(1);
//        }

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("YouTube-Api-Search").build();

            // Prompt the user to enter a query term.
           // String queryTerm = getInputQuery();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
           // String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(defaultSearchKeyword);
            search.setPageToken(nextTocken);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url),nextPageToken,pageInfo,prevPageToken");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            String str = searchResponse.getNextPageToken();
            System.out.println("Token :"+searchResponse.getNextPageToken());
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null && lstYouTubeDatas.size() < 30) {
            		collectYouTubeData(searchResultList.iterator());
            		//getVideo(searchResponse.getNextPageToken());
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
     * Prompt the user to enter a query term and return the user-specified term.
     */
    private static String getInputQuery() throws IOException {

        String inputQuery = "";

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "telecom";
        }
        return inputQuery;
    }

    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    static int i = 0;
    private void collectYouTubeData(Iterator<SearchResult> iteratorSearchResults) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on .");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }
        
        YouTubeData youTubeData;
        
        while (iteratorSearchResults.hasNext()) {
        	i++;
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();
            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
               // Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                
                System.out.println(" Video nu: " + i);
                System.out.println(" Video Id: " + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println("\n-------------------------------------------------------------\n");
                
                youTubeData = new YouTubeData();
                youTubeData.setYouTubeUrl(defaultyouTubeUrl+rId.getVideoId());
                youTubeData.setVideoTitle(singleVideo.getSnippet().getTitle());
                lstYouTubeDatas.add(youTubeData);
                
            }
        }
        System.out.println(" Video Id: " + i);
    }
    
	private void dumpDatainFile() {
		System.out.println(" lstYouTubeDatas Id: " + lstYouTubeDatas.size());
		System.out.println(" youTubeDataFileName : " + youTubeDataFileName);
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			
			File file = new File(projectPath + File.separator + youTubeDataFileName);
			
			//File file = new ClassPathResource(youTubeDataFileName).getFile();

			System.out.println(" getAbsoluteFile : " + file.getAbsolutePath());
			
			if (file.exists()) {
				file.delete();
			}else {
				file.createNewFile();
			}

			 fileWriter = new FileWriter(file.getAbsoluteFile(),true);
			 bufferedWriter = new BufferedWriter(fileWriter);
			
			if(!lstYouTubeDatas.isEmpty()) {
				
				for (YouTubeData youTubeData : lstYouTubeDatas) {
					bufferedWriter.write(youTubeData.getYouTubeUrl() + "\n");
					bufferedWriter.write(youTubeData.getVideoTitle() + "\n\n");
				}
			}
			
			bufferedWriter.close();
			
		}catch (IOException e) {
			logger.error("Exception occurred: -> {} ", e.getMessage());
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}

			} catch (IOException e) {
				logger.error("Exception occurred: -> {} ", e.getMessage());
			}

		}
	}

}
