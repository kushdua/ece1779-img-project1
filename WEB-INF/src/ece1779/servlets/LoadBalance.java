package ece1779.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

public class LoadBalance extends HttpServlet {
	
	//mgr config
	int poolSize = 2;
	
	//auto scaling policy config
	int cpuGrowThreshold = 5;
	int cpuShrinkThreshold = 1;
	int ratioGrow = 2;
	int ratioShrink = 2;
	
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
		doPost(request, response);
    }

    // Do this because the servlet uses both post and get
    public void doPost(HttpServletRequest request,
    		HttpServletResponse response)
    				throws IOException, ServletException {
    	try {
			response.getWriter().println("Instance ID = " + retrieveInstanceId());
		} catch (Exception e) {
			response.getWriter().println("Couldn't retrieve instance ID - error: " + e.getMessage());
		}
    	
    	
    }
    
    public static String retrieveInstanceId() throws Exception 
    {
    	String EC2Id = "";
    	String inputLine;
    	URL EC2MetaData = new URL("http://169.254.169.254/latest/meta-data/instance-id");
    	URLConnection EC2MD = EC2MetaData.openConnection();
    	BufferedReader in = new BufferedReader(
    			new InputStreamReader(
    					EC2MD.getInputStream()));
    	while ((inputLine = in.readLine()) != null)
    	{	
    		EC2Id = inputLine;
    	}
    	in.close();
    	return EC2Id;
    }
}
