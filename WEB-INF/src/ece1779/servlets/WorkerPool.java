package ece1779.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentials;

import ece1779.servlets.LoadBalancerLibrary;

public class WorkerPool extends HttpServlet {
	
		private static final long serialVersionUID = 1L;
		
		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			//saving the ManualWorkerPoolSize to file
					
			try {
				
							
				String enteredPoolsize = request.getParameter("manualPoolSizeValue");
				
				try
				{
					int intEnteredPoolSize = Integer.parseInt(enteredPoolsize);
					LoadBalancerLibrary.getInstance().setManualWorkerPoolSize(enteredPoolsize);
					LoadBalancerLibrary.getInstance().increaseWorkerPoolSize(intEnteredPoolSize, (AWSCredentials)getServletContext().getAttribute("AWSCredentials"));
				}
				catch(NumberFormatException e)
				{
					request.setAttribute("errorMessage", "Invalid integer argument provided: " + enteredPoolsize);
				}
				catch(Exception e)
				{
					request.setAttribute("errorMessage", "Unable to save manual pool size to file.");
				}
				
				request.setAttribute("successMessage", "Successfully updated worker pool size to " + enteredPoolsize + ". Please wait while the instances are started");
				
				response.setContentType("text/html");
	        	//response.sendRedirect(request.getRequestURI());
	        	response.sendRedirect("/ece1779-img-project1/site/manager.jsp");
				
				} catch (Exception e) {
					e.printStackTrace();
				} 
			
			    }

}
