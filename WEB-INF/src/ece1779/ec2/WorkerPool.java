package ece1779.ec2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ece1779.servlets.LoadBalancerLibrary;

public class WorkerPool extends HttpServlet {
	
		private static final long serialVersionUID = 1L;
		
		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			//saving the ManualWorkerPoolSize to file
					
			try {
				
							
				String enteredPoolsize = request.getParameter("manualPoolSizeValue");
				
				LoadBalancerLibrary.getInstance().setManualWorkerPoolSize(enteredPoolsize);
				
				response.setContentType("text/html");
	        	//response.sendRedirect(request.getRequestURI());
	        	response.sendRedirect("/ece1779-img-project1/site/manager.jsp");
				
				} catch (Exception e) {
					e.printStackTrace();
				} 
			
			    }

}
