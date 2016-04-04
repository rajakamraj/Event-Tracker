package com.ibm.cloudoe.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
@Path("/coordinates")
public class LoadCoordinates {
	private Connection con;

	public LoadCoordinates() {
		con = getConnection();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@FormParam("q") String query, @FormParam("table") String tablename, @FormParam("columns") String columns) {
		Statement stmt;
		ResultSet rs;
		Double lng = 40.71427;		
		String geotype = "{";
		geotype += "\"type\":\"FeatureCollection\",\"features\":";
		geotype += "[";
			
		
		String jsonco = "[";
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String querycheck="select \"smaSentiment\",\"msgBody\",\"msgCoordinates\",\"smaAuthorCountry\",\"smaAuthorCity\" from \""+tablename+"\"where \"msgCoordinates\" is not null";
			System.out.println("query is "+querycheck);
			rs = stmt.executeQuery("select \"smaSentiment\",\"msgCoordinates\",\"smaAuthorCountry\",\"smaAuthorCity\"  from \""+tablename+"\" where \"msgCoordinates\" is not null");
			while (rs.next()) {
				
				
				
				if (!rs.isFirst()) { geotype += ", "; }
				geotype	+= "{\"type\":\"Feature\",";
				geotype += "\"geometry\":{\"type\":\"Point\",\"coordinates\":"+ rs.getString(2) + "},";
				geotype += "\"properties\":{\"COUNTRY\":\"" + rs.getString(3) + "\",";

				geotype += "\"CITY\":\"" + rs.getString(4) + "\",\"SENTIMENT\":\"" + rs.getString(1) + "\"}";
				geotype +="}";
//				jsonco += "{\"smaSentiment\": \"" + rs.getString(1) + "\",";
//				jsonco += "\"msgCoordinates\": \"" + rs.getString(2) + "\",";
//				jsonco += "\"smaAuthorCountry\": \"" + rs.getString(3) + "\",";
//				jsonco += "\"smaAuthorCity\": \"" + rs.getString(4) + "\"}";
			
			}
		} catch (SQLException e) {
			e.printStackTrace();
			geotype +="{\"error\": \"" + e.toString() + "\"}";
		}
		
		jsonco += "]";
		geotype += "],\"name\":\"SampleJSON\"";
		geotype += "}";
		System.out.println("json value is "+geotype);
		return Response.ok(geotype).build();
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
