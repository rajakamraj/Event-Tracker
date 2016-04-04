package com.ibm.cloudoe.samples;

import java.io.Console;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.db2.jcc.DB2Driver;
import com.ibm.db2.jcc.am.SqlSyntaxErrorException;

@Path("/load")
/**
 * CRUD service to load tweets into a table. It uses REST style.
 *
 */
public class LoadResource {

	private Connection con=null;
	private String searchURL;
	private String credentials;
	private static String status = "idle";
	private static String phase = "Not started...";
	private static int numtweets = 0;
	private static int maxtweets = 0;

	public LoadResource() {
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@FormParam("q") String query, @FormParam("table") String tablename, @FormParam("columns") String columns) {
		Statement stmt;
		System.out.println("here starting");
		String retstr = "";
		int numtbls = 0;
		
		// first check initialization errors
		System.out.println("Before connection");
		con = getConnection();
		System.out.println("connection value is"+con.toString());
		retrieveURL();
		System.out.println(status);
//		if (status != "idle") {			
//			retstr = "{\"status\":\"error\", \"phase\":\"REST API already in used...\"}";			
//			return Response.ok(retstr).build();
//		} else if (searchURL == null || credentials == null) {
//			status = "error";
//			phase = "REST API not configured correctly...";
//			retstr = "{\"status\":\"" + status + "\", \"phase\":\"" + phase + "\"}";
//			return Response.ok(retstr).build();
//		} 
		searchURL = searchURL + "?q=" + URLEncoder.encode(query) + "&from=0&size=100";
		
		// initialize the status variables
		status = "idle";
		phase = "Not started...";
		numtweets = 0;
		maxtweets = 0;

		// create the table as indicated
		String createQuery = "";
		try {
			status = "running";
			phase = "Creating table " + tablename + "...";
			stmt = con.createStatement();
			createQuery = getCreateStatement(tablename, columns);
			System.out.println(createQuery);
			stmt.executeUpdate(createQuery);
		}
		catch (SQLException ex){
			try {
				System.out.println("Table already exists");
				stmt = con.createStatement();
				System.out.println("drop table \""+ tablename+"\"");
				stmt.executeUpdate("drop table \""+ tablename+"\"");
				stmt.executeUpdate("COMMIT");
				createQuery = getCreateStatement(tablename, columns);
				System.out.println(createQuery);
				stmt.executeUpdate(createQuery);
			} catch (SQLException e) {
				System.out.println("Error in drop");
				e.printStackTrace();
				status = "error on drop";
				phase = "Could not drop table " + tablename + "(" + createQuery + "): " + e.toString();
				retstr = "{\"status\":\"" + status + "\", \"phase\":\"" + phase.replace("\"", "\\\"") + "\"}";
				return Response.ok(retstr).build();
			}
		}
		
		

		// load the tweets into the table
		String[] coltypes = getColumnTypes(columns);
		String[] colpaths = getJSONPaths(columns);		
		JSONObject nextTweets = getNextTweets(searchURL);
		maxtweets = ((Long) getObject(nextTweets, "search.results")).intValue();
		phase = "Inserting " + maxtweets + " tweets into table " + tablename + "...";
		while ( nextTweets != null ) {
			JSONArray tweets = getJSONArray(nextTweets, "tweets");
			if (tweets == null || tweets.size() == 0) {
				break;
			}
			if (!insertTweets(tablename, coltypes, colpaths, tweets)) {
				nextTweets = null;
				break;
			}
			if (numtweets >= maxtweets) {
				break;
			}
			// search next bunch of tweets
			String cdeURL = (String) getObject(nextTweets, "related.next.href");
			// the port number returned by the CDE service is wrong: replace first part of the URL with first part of searchURL
			String nextURL = searchURL.substring(0, searchURL.indexOf('?')) + cdeURL.substring(cdeURL.indexOf('?'));
			nextTweets = getNextTweets(nextURL);
		}

		if (nextTweets != null) {
			status = "loaded";
			phase = "Table " + tablename + " created and " + maxtweets + " tweets loaded successfully.";	
			status="idle";
		} else {
			// error already set: delete the table that didn't work
			try {
				stmt = con.createStatement();
				stmt.executeUpdate("drop table "+ tablename);
			} catch (SQLException e) {
				// do nothing
			}
		}
		retstr = "{\"status\":\"" + status + "\", \"phase\":\"" + phase.replace("\"", "\\\"") + "\"}";
		
		return Response.ok(retstr).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		String retstr = "";
		retstr = "{\"status\":\"" + status + "\", \"phase\":\"" + phase.replace("\"", "\\\"") + "\", \"actual\":" + numtweets + ", \"expected\":" + maxtweets + "}";
		if (status == "error" || status == "loaded") {
			// last status to be returned: reinitialize for next load.
			status="idle";
			phase="Not started...";
			numtweets = 0;
			maxtweets = 0;
		}
		return Response.ok(retstr).build();
	}
	
	
	// There are two ways of obtaining the connection information for some services in Java 
	
	// Method 1: Auto-configuration and JNDI
	// The Liberty buildpack automatically generates server.xml configuration 
	// stanzas for the SQL Database service which contain the credentials needed to 
	// connect to the service. The buildpack generates a JNDI name following  
	// the convention of "jdbc/<service_name>" where the <service_name> is the 
	// name of the bound service. 
	// Below we'll do a JNDI lookup for the EntityManager whose persistence 
	// context is defined in web.xml. It references a persistence unit defined 
	// in persistence.xml. In these XML files you'll see the "jdbc/<service name>"
	// JNDI name used.

//	private Connection getConnection() {
//		try {
//			Class.forName("com.ibm.db2.jcc.DB2Driver");
//			String envServices = System.getenv("VCAP_SERVICES");
//			BasicDBObject obj = (BasicDBObject) JSON.parse (envServices);
//			String thekey = null;
//			Set<String> keys = obj.keySet();
//			keys = obj.keySet();
//			for (String eachkey : keys)
//				if (eachkey.contains("dashDB"))
//					thekey = eachkey;
//			BasicDBList list = (BasicDBList) obj.get(thekey);
//			BasicDBObject bludb = (BasicDBObject) list.get("0");
//			bludb = (BasicDBObject) bludb.get("credentials");
//			String jdbcurl =  (String) bludb.get("jdbcurl"); // use ssljdbcurl to connect via SSL
//			String user = (String) bludb.get("username");
//			String password = (String) bludb.get("password");
//			con = DriverManager.getConnection(jdbcurl, user,password);
//			con.setAutoCommit(false);
//			System.out.print("before return"+con);
//			return con;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
	private Connection getConnection() {
		InitialContext ic;
		try {
			ic = new InitialContext();
			return (Connection) ((DataSource) ic.lookup("java:comp/env/jdbc/mydbdatasource")).getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	// Method 2: Parsing VCAP_SERVICES environment variable
    // The VCAP_SERVICES environment variable contains all the credentials of 
	// services bound to this application. You can parse it to obtain the information 
	// needed to connect to the SQL Database service. SQL Database is a service
	// that the Liberty buildpack auto-configures as described above, so parsing
	// VCAP_SERVICES is not a best practice.
	
	// see HelloResource.getInformation() for an example


	private void retrieveURL() {
		String envServices = System.getenv("VCAP_SERVICES");
        if (envServices == null) { searchURL = null; credentials = null; return; }
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
        if (thekey == null) { searchURL = null; credentials = null; return; }        
        BasicDBList list = (BasicDBList) obj.get (thekey);
        obj = (BasicDBObject) list.get ("0");
        obj = (BasicDBObject) obj.get ("credentials");
        searchURL = "https://" + (String) obj.get("host") + "/api/v1/messages/search";
        String creds = (String) obj.get("username");
        creds += ":" + (String) obj.get("password");
		credentials = javax.xml.bind.DatatypeConverter.printBase64Binary(creds.getBytes());
	}


	private String getCreateStatement(String tablename, String columns) {
		String create="CREATE TABLE \"" + tablename + "\"(";
		String[] coldefs = columns.split("\\|");
		for (int i=0; i<coldefs.length; i = i + 3) {
			create += "\"" + coldefs[i] + "\" " + coldefs[i+1] + ",";
		}
//		create +=" ORGANIZE BY ROW";
//		System.out.println("create statement "+create);
		return create.substring(0, create.length()-1) + ") ORGANIZE BY ROW";
	}

	
	private String[] getColumnTypes(String columns) {
		String[] coldefs = columns.split("\\|");
		String[] coltypes = new String[coldefs.length/3];
		for (int i=0; i<coldefs.length; i = i + 3) {
			coltypes[i/3] = coldefs[i+1];
		}
		return coltypes;
	}


	private String[] getJSONPaths(String columns) {
		String[] coldefs = columns.split("\\|");
		String[] colpaths = new String[coldefs.length/3];
		for (int i=0; i<coldefs.length; i = i + 3) {
			colpaths[i/3] = coldefs[i+2];
		}
		return colpaths;
	}


	private JSONObject getNextTweets(String nexturl) {
		JSONObject retval = null;
		try {
	        URL thisUrl = new URL(nexturl);
	        HttpURLConnection urlConnection = (HttpURLConnection) thisUrl.openConnection();
			urlConnection.setConnectTimeout(20000);
			urlConnection.setReadTimeout(20000);
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Authorization", "Basic " + credentials); 
			Reader reader = null;
			if (400 <= urlConnection.getResponseCode()) {
				reader = new InputStreamReader(urlConnection.getErrorStream(), "UTF-8");
				char[] buffer = new char[4096];
				int in;
				StringBuilder sb = new StringBuilder();
				while (0 < (in = reader.read(buffer))) {
					sb.append(buffer, 0, in);
				}
				status = "error";
				phase = "Connection error to " + nexturl + ":" + sb.toString();
				return null;
			}
			reader = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
			char[] buffer = new char[4096];
			int in;
			StringBuilder sb = new StringBuilder();
			while (0 < (in = reader.read(buffer))) {
				sb.append(buffer, 0, in);
			}
			retval = JSONObject.parse(sb.toString());
		} catch (Exception e) {
			status = "error";
			phase = "CAUGHT error " + e.toString();
		}
		
		return retval;
	}
	

	private Object getObject(JSONObject root, String path) {
		String restpath = path;
		JSONObject curobj = root;
		Object object = null;
		try {
			while ( restpath.length() > 0 ) {
				String next = restpath;
				int idx = restpath.indexOf('.');
				if ( idx >= 0 ) {
					next = restpath.substring(0, idx);
					restpath = restpath.substring(idx+1);
					if (curobj.get(next).getClass().getSimpleName().equals("JSONArray")) {
						curobj = (JSONObject) ((JSONArray) curobj.get(next)).get(0);
					} else {
						curobj = (JSONObject) curobj.get(next);
					}
				} else {
					restpath = "";
					object = (Object) curobj.get(next);
				}
			}
		} catch (NullPointerException e) {
			// do nothing, null value returned when path not found
		}
		return object;
	}


	private JSONArray getJSONArray(JSONObject root, String path) {
		String restpath = path;
		JSONObject curobj = root;
		JSONArray value = null;
		while ( restpath.length() > 0 ) {
			String next = restpath;
			int idx = restpath.indexOf('.');
			if ( idx >= 0 ) {
				next = restpath.substring(0, idx);
				restpath = restpath.substring(idx+1);
				curobj = (JSONObject) curobj.get(next);
			} else {
				restpath = "";
				value = (JSONArray) curobj.get(next);
			}
		}
		return value;
	}


	private String getInsertStatement(String tablename, String[] coltypes, String[] colpaths, JSONObject tweet) {
		String insert="INSERT INTO \"" + tablename + "\" VALUES(";
		for (int i=0; i<colpaths.length; i++) {
			Object valobj = getObject(tweet, colpaths[i]);
			if (valobj == null) {
				insert = insert + "null,";
			} else if (coltypes[i].toLowerCase().equals("integer")) {
				insert = insert + (Long) valobj + ",";
			} else if (coltypes[i].toLowerCase().equals("timestamp")) {
				insert = insert + "'" + ((String) valobj).substring(0, 10) + " " + ((String) valobj).substring(11, 19) + "',";
			} else {
				insert = insert + "'" + valobj.toString().replace("'", "''") + "',";
			}
		}
		return insert.substring(0, insert.length()-1) + ")";
	}
	
	
	private boolean insertTweets(String tablename, String[] coltypes, String[] colpaths, JSONArray tweets) {
		Statement stmt;
		String insertQuery = "";
		// insert all tweets
		for (int i = 0; i < 20; i++) {
			try {
				stmt = con.createStatement();
				insertQuery = getInsertStatement(tablename, coltypes, colpaths, (JSONObject) tweets.get(i));
				stmt.executeUpdate(insertQuery);
				stmt.executeUpdate("COMMIT");
				numtweets++;
			} catch (SQLException e) {
				e.printStackTrace();
				status = "error";
				phase = "Could not insert tweet #" + numtweets + "(" + insertQuery + "): " + e.toString();
				return false;
			}
		}
		// commit
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("COMMIT");
		} catch (SQLException e) {
			e.printStackTrace();
			status = "error";
			phase = "Could not commit after " + numtweets + " INSERTs into table " + tablename + ": " + e.toString();
			return false;
		}
		return true;
	}

}

