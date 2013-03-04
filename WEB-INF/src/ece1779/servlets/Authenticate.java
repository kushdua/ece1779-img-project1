package ece1779.servlets;

import java.io.IOException;
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

public class Authenticate extends HttpServlet {
    public void doGet(HttpServletRequest request,
	              HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        boolean rememberMe = !request.getParameter("remember-me").isEmpty();
        
    	String submit = request.getParameter("submit");
    	if(!submit.isEmpty())
    	{
    		if(submit.compareTo("signinAcc")==0)
    		{
    			//Try to sign in using provided credentials
    			String name = request.getParameter("userNameText");
    			String pass = request.getParameter("passText");
    			
    			Connection con = null;
    			
    			try {
    				// Get DB connection from pool
    				DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");

    				con = dbcp.getConnection();

    				// Execute SQL query
    				PreparedStatement stmt = con.prepareStatement("select login, password from users where login=? and password=?;");
    				stmt.setString(1, name);
    				stmt.setString(2, pass);
    				
    				ResultSet rs = stmt.executeQuery();
    				
    				if(rs.next())
    				{
	    				//Login successful
    	      			con.close();
    	      			session.setAttribute("username", name);
    	      			if(rememberMe)
    	      			{
    	      				response.addCookie(new Cookie("loggedIn", name));
    	      			}
    					request.setAttribute("successMessage", "Successfully logged in.");
    	    			getServletContext().getRequestDispatcher("/view.jsp").forward(request, response);
    				}
    				else
    				{
    					//Login unsuccessful
    	      			con.close();
    	    			request.setAttribute("error", "Invalid username/password combination.");
    	    			getServletContext().getRequestDispatcher("/welcome.jsp").forward(request, response);
    				}
    			}
    			catch(Exception ex) {
    	    		try {
    	                getServletContext().log(ex.getMessage());  
    	      		}
    	      		catch (Exception e) {
    	                  e.printStackTrace();
    	      		}
    	      	}    	
    	      	finally {
    	      		try {
    	      			con.close();
    	      		}
    	      		catch (Exception e) {
    	                  e.printStackTrace();
    	      		}
    	      	}
    		}
    		else if(submit.compareTo("createAcc")==0)
    		{
    			//Try to create account using provided credentials
    			String name = request.getParameter("userNameText");
    			String pass = request.getParameter("passText");
    			
    			Connection con = null;
    			
    			try {
    				// Get DB connection from pool
    				DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");

    				con = dbcp.getConnection();

    				// Execute SQL query
    				PreparedStatement stmt = con.prepareStatement("insert into users(login,password) values(?,?);");
    				stmt.setString(1, name);
    				stmt.setString(2, pass);
    				
    				int numRowsMod = stmt.executeUpdate();
    				
    				if(numRowsMod==1)
    				{
    					//Successful register
    	      			con.close();
    	      			session.setAttribute("username", name);
    	      			if(rememberMe)
    	      			{
    	      				response.addCookie(new Cookie("loggedIn", name));
    	      			}
    					request.setAttribute("successMessage", "Successfully registered.");
    	    			getServletContext().getRequestDispatcher("/view.jsp").forward(request, response);
    				}
    				else
    				{
    					//Return error
    	      			con.close();
    	    			request.setAttribute("errorMessage", "Username " + name + " taken. Please try again.");
    	    			getServletContext().getRequestDispatcher("/welcome.jsp").forward(request, response);
    				}
    			}
    			catch(Exception ex) {
    	    		try {
    	                getServletContext().log(ex.getMessage());  
    	      		}
    	      		catch (Exception e) {
    	                  e.printStackTrace();
    	      		}
    	      	}    	
    	      	finally {
    	      		try {
    	      			con.close();
    	      		}
    	      		catch (Exception e) {
    	                  e.printStackTrace();
    	      		}
    	      	}
    		}
    		else
    		{
    			if(request.getParameter("logout").compareTo("true")==0)
    			{
    				session.invalidate();

    		    	Cookie [] pageCookies = request.getCookies();
    		    	for(Cookie c : pageCookies)
    		    	{
    		    		if(c.getName().compareTo("loggedIn")==0)
    		    		{
    		    			c.setMaxAge(0);
    		    			break;
    		    		}
    		    	}
    			}
    			else
    			{
	    			request.setAttribute("error", "Unknown action");
    			}
    			getServletContext().getRequestDispatcher("/welcome.jsp").forward(request, response);
    		}
    	}
    }

    // Do this because the servlet uses both post and get
    public void doPost(HttpServletRequest request,
    		HttpServletResponse response)
    				throws IOException, ServletException {
    	doGet(request,response);
    }
}
