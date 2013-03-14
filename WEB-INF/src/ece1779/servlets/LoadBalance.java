package ece1779.servlets;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class LoadBalance extends HttpServlet {

	public void doGet( HttpServletRequest request,
	               HttpServletResponse response)
    throws IOException, ServletException
    {
        try {
			LoadBalancerLibrary.getInstance().loadBalance(getServletContext());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void doPost(HttpServletRequest request,
            HttpServletResponse response)
	throws IOException, ServletException 
	{
		doGet(request,response);
	}
}
