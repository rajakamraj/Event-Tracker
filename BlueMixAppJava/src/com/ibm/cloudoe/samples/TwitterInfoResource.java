package com.ibm.cloudoe.samples;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;


//This class define the /twitterinfo RESTful API to fetch the twitter service information

@Path("/twitterinfo")
public class TwitterInfoResource {

	@GET
	public String getInformation() {
		// load all system environments
		//JSONObject sysEnv = new JSONObject(System.getenv());

		// 'VCAP_APPLICATION' is in JSON format, it contains useful information about a deployed application
		// String envApp = System.getenv("VCAP_APPLICATION");

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
        String name = (String) obj.get("name");
        obj = (BasicDBObject) obj.get ("credentials");
        String databaseHost = (String) obj.get ("host");
        Integer port = Integer.parseInt((String) obj.get ("port"));
        String user = (String) obj.get ("username"); 
        String password = (String) obj.get ("password");
        String url = (String) obj.get("url");
        
        
        BasicDBObject TwitterInfoObj = new BasicDBObject();

        TwitterInfoObj.put("name", name);
        TwitterInfoObj.put("host", databaseHost);
        TwitterInfoObj.put("port", port);
//        TwitterInfoObj.put("user", user);
//        TwitterInfoObj.put("pwd", password);
        TwitterInfoObj.put("url", url);
        
        return TwitterInfoObj.toString();
        
        
        
	}
}