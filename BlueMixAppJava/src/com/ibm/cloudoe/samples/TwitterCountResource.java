package com.ibm.cloudoe.samples;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;


//This class define the /twittercount RESTful API to fetch the number of tweets resulting from a query

@Path("/twittercount")
public class TwitterCountResource {

	@GET
	public String getCount(@QueryParam("q") String query) {
		// ##############################################
		// get the URL of the twitter service
		// ##############################################
		// 'VCAP_SERVICES' contains all the credentials of services bound to this application.
		String envServices = System.getenv("VCAP_SERVICES");

        if (envServices == null) {
        	  return("VCAP_SERVICES not found");
        	  
          }
		
        BasicDBObject obj = (BasicDBObject) JSON.parse (envServices);
        
        String thekey = null;
        Set<String> keys = obj.keySet();
        System.out.println ("Searching through VCAP keys");
  	  // Look for the VCAP key that holds the SQLDB information
        for (String eachkey : keys) {
      	  if (eachkey.contains("twitterinsights")) {
      		  thekey = eachkey;
      	  }
        }
        if (thekey == null) {
      	  return("Cannot find any Twitter service in the VCAP_SERVICES");
        }
        
        BasicDBList list = (BasicDBList) obj.get (thekey);
        obj = (BasicDBObject) list.get ("0");
        obj = (BasicDBObject) obj.get ("credentials");
        String url = "https://" + (String) obj.get("host");
        String credentials = (String) obj.get("username");
        credentials += ":" + (String) obj.get("password");
		String encCreds = javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());

		// ##############################################
		// call the URL
		// ##############################################
		String retval=null;
		try {
	        URL countUrl = new URL(url + "/api/v1/messages/count?q=" + URLEncoder.encode(query));
	        HttpURLConnection urlConnection = (HttpURLConnection) countUrl.openConnection();
			urlConnection.setConnectTimeout(20000);
			urlConnection.setReadTimeout(20000);
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Authorization", "Basic " + encCreds); 
			Reader reader = null;
			if (400 <= urlConnection.getResponseCode()) {
				reader = new InputStreamReader(urlConnection.getErrorStream(), "UTF-8");
				char[] buffer = new char[4096];
				int in;
				StringBuilder sb = new StringBuilder();
				while (0 < (in = reader.read(buffer))) {
					sb.append(buffer, 0, in);
				}
				return "Connection Error " + urlConnection.getResponseCode() + " - "
   	                 + countUrl + " - " + credentials + "(" + encCreds + ") - " + sb.toString();
			}
			reader = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
			char[] buffer = new char[4096];
			int in;
			StringBuilder sb = new StringBuilder();
			while (0 < (in = reader.read(buffer))) {
				sb.append(buffer, 0, in);
			}
			retval=sb.toString();
		} catch (Exception e) {
			return e.toString() + " for " + url + "/api/v1/messages/count?q=" + URLEncoder.encode(query);
		}

        return retval;

	}
}