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
		doPost(request, response);
    }

    // Do this because the servlet uses both post and get
    public void doPost(HttpServletRequest request,
    		HttpServletResponse response)
    				throws IOException, ServletException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        boolean rememberMe = (request.getParameter("remember-me")!=null && request.getParameter("remember-me").compareTo("remember-me")==0);
        String signinAcc = request.getParameter("signinAcc");
        String createAcc = request.getParameter("createAcc");
    	String submit = (signinAcc==null || signinAcc.isEmpty())?createAcc:signinAcc;
    	if(submit!=null)
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
    	      				Cookie cookie = new Cookie("loggedIn", name);
    	      				cookie.setMaxAge(365*24*60*60);
    	      				response.addCookie(cookie);
    	      			}
    					request.getSession().setAttribute("successMessage", new String("Successfully logged in."));
    					if(request.getAttribute("redirect")==null)
    					{
    						//getServletContext().getRequestDispatcher("/site/view.jsp").forward(request, response);
    						response.sendRedirect("/ece1779-img-project1/site/view.jsp");
    						return;
    					}
    					else
    					{
    						//getServletContext().getRequestDispatcher(request.getAttribute("redirect").toString()).forward(request, response);
    						response.sendRedirect("/ece1779-img-project1/"+request.getAttribute("redirect").toString());
    						return;
    					}
    				}
    				else
    				{
    					//Login unsuccessful
    	      			con.close();
    	    			request.getSession().setAttribute("errorMessage", new String("Invalid username/password combination."));
    	    			//getServletContext().getRequestDispatcher("/site/welcome.jsp").forward(request, response);
    	    			response.sendRedirect("/ece1779-img-project1/site/welcome.jsp");
    	    			return;
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
    	      				Cookie cookie = new Cookie("loggedIn", name);
    	      				cookie.setMaxAge(365*24*60*60);
    	      				response.addCookie(cookie);
    	      			}
    					request.getSession().setAttribute("successMessage", new String("Successfully registered."));
    					if(request.getAttribute("redirect")==null)
    					{
    						//getServletContext().getRequestDispatcher("/site/view.jsp").forward(request, response);
    						response.sendRedirect("/ece1779-img-project1/site/view.jsp");
    						return;
    					}
    					else
    					{
    						getServletContext().getRequestDispatcher(request.getAttribute("redirect").toString()).forward(request, response);
    						response.sendRedirect("/ece1779-img-project1/"+request.getAttribute("redirect").toString());
    						return;
    					}
    				}
    				else
    				{
    					//Return error
    	      			con.close();
    	    			request.getSession().setAttribute("errorMessage", new String("Username " + name + " taken. Please try again."));
    	    			//getServletContext().getRequestDispatcher("/site/welcome.jsp").forward(request, response);
    	    			response.sendRedirect("/ece1779-img-project1/site/welcome.jsp");
    	    			return;
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
    	}
		else
		{
			request.getSession().setAttribute("errorMessage", new String("Unknown action"));
			//getServletContext().getRequestDispatcher("/site/welcome.jsp").forward(request, response);
			response.sendRedirect("/ece1779-img-project1/site/welcome.jsp");
			return;
		}
    }
}
