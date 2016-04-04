package com.ibm.cloudoe.samples;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

import com.evdb.javaapi.EVDBAPIException;
import com.evdb.javaapi.EVDBRuntimeException;
import com.evdb.javaapi.data.Event;
import com.evdb.javaapi.data.SearchResult;
import com.evdb.javaapi.data.request.SearchRequest;
import com.evdb.javaapi.data.request.EventSearchRequest;
import com.evdb.javaapi.operations.EventOperations;
import com.evdb.javaapi.APIConfiguration;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;


@Path("/events")

public class SearchEvent {
	@GET
	public String getCount() {
		String json ="";
////		APIConfiguration api=new APIConfiguration();
//		APIConfiguration.setApiKey("xV3bf27TpCrScP4n");
//		 EventOperations eo = new EventOperations();
//		 
//	        EventSearchRequest esr = new EventSearchRequest();
//	     
//	        esr.setLocation("San Diego");
//	        esr.setDateRange("2012050200-2013052100");
//	        esr.setPageSize(20);
//	        esr.setPageNumber(1);
//	        // These 2 lines will set the timeout to 60 seconds.Normally not needed
//	        // Unless you are using Google App Engine
//	        esr.setConnectionTimeout(60000);  // Used with Google App Engine only
//	        esr.setReadTimeout(60000);        // Used with Google App Engine only
//	        SearchResult sr = null;
//	        try {
//	                sr = eo.search(esr);
//	                if (sr.getTotalItems() > 1) {
//	                	for(Event e: sr.getEvents())
//	                	{
//	                		json += "{\"name\": \"" + e.getTitle()+ "\"";
//	                	}
//	                	
//	                System.out.println("Total items: " + sr.getTotalItems());
//	                }
//	        }catch(EVDBRuntimeException var){
//	                System.out.println("Opps got runtime an error...");
//	        } catch( EVDBAPIException var){
//	                System.out.println("Opps got runtime an error...");
//	        }
		HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://api.eventful.com/json/events/search?location=San+Diego&app_key=xV3bf27TpCrScP4n");

        try {
            HttpResponse response = httpclient.execute(httpget);
            
            json = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            System.out.println("Concert"+ e.getMessage());
        } catch (IOException e) {
        	System.out.println("Concert"+ e.getMessage());
        }        
        
        return json;
    }
	       

}
