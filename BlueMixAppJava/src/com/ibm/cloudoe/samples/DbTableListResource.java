package com.ibm.cloudoe.samples;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/tablelist")
/**
 * CRUD service of list tables. It uses REST style.
 *
 */
public class DbTableListResource {

	private Connection con;

	public DbTableListResource() {
		con = getConnection();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		Statement stmt;
		ResultSet rs;
		String json = "{\"filter\":\"none\", \"body\":[";
		int numtbls = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery("select TABNAME from SYSCAT.TABLES where TABSCHEMA=CURRENT_SCHEMA order by TABNAME");
			while (rs.next()) {
				if (!rs.isFirst()) { json += ", "; }
				json += "{\"name\": \"" + rs.getString(1) + "\"}";
				numtbls++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			json +="{\"error\": \"" + e.toString() + "\"}";
		}
		json += "], \"count\": " + numtbls + "}";
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

	// Method 2: Parsing VCAP_SERVICES environment variable
    // The VCAP_SERVICES environment variable contains all the credentials of 
	// services bound to this application. You can parse it to obtain the information 
	// needed to connect to the SQL Database service. SQL Database is a service
	// that the Liberty buildpack auto-configures as described above, so parsing
	// VCAP_SERVICES is not a best practice.
	
	// see HelloResource.getInformation() for an example

}
