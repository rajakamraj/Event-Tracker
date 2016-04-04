package com.ibm.cloudoe.samples;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ibm.nosql.json.api.BasicDBList;
import com.ibm.nosql.json.api.BasicDBObject;
import com.ibm.nosql.json.util.JSON;

@Path("/loadsentiment")
public class LoadSentiment {
	private Connection con;

	public LoadSentiment() {
		con = getConnection();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@FormParam("q") String query, @FormParam("table") String tablename, @FormParam("columns") String columns) {
		Statement stmt;
		ResultSet rs;
		String json = "[";
		int numtbls = 0;
		int smaPositive=0;
		int smaNegative=0;
		int smaNeutral=0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String querycheck="select \"smaSentiment\" from \""+tablename+"\"";
			System.out.println("query is "+querycheck);
			rs = stmt.executeQuery("select \"smaSentiment\" from \""+tablename+"\"");
			while (rs.next()) {
				if(rs.getString(1)!= null)
				{
					if(rs.getString(1).equals("POSITIVE"))
					{
						smaPositive++;
					}
					else if(rs.getString(1).equals("NEGATIVE"))
					{
						smaNegative++;
					}
					else
					{
						smaNeutral++;
					}
				}
				else
				{
					smaNeutral++;
				}
				
				
				
//				if (!rs.isFirst()) { json += ", "; }
//				json += "{\"name\": \"" + rs.getString(1) + "\",";
//				json += "\"value\": \"" + rs.getString(2) + "\"}";
				numtbls++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			json +="{\"error\": \"" + e.toString() + "\"}";
		}
		String positive="POSITIVE";
		String negative="NEGATIVE";
		String neutral="NEUTRAL";
		json += "{\"name\": \"" + positive + "\",";
		json += "\"value\": " + smaPositive + "}";
		json += ", ";
		json += "{\"name\": \"" + negative + "\",";
		json += "\"value\": " + smaNegative + "}";
		json += ", ";
		json += "{\"name\": \"" + neutral + "\",";
		json += "\"value\": " + smaNeutral + "}";
		json += "]";
		System.out.println("json value is "+json);
		return Response.ok(json).build();
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
}
