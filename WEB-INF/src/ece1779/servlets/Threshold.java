package ece1779.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ece1779.servlets.LoadBalancerLibrary;

public class Threshold extends HttpServlet {
	
		private static final long serialVersionUID = 1L;
		
		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			
			//saving the ManualWorkerPoolSize to file
					
			try {
				
				String cpuThresholdGrowing = request.getParameter("cpuThresholdGrowValue");
				String cpuThresholdShrinking = request.getParameter("cpuThresholdShrinkValue");
				String ratioExpandPool = request.getParameter("ratioExpandPoolValue");
				String ratioShrinkPool = request.getParameter("ratioShrinkPoolValue");
				
				LoadBalancerLibrary.getInstance().setThresholdsAndRatios(cpuThresholdGrowing,cpuThresholdShrinking,ratioExpandPool,ratioShrinkPool);
				
				response.setContentType("text/html");
	        	//response.sendRedirect(request.getRequestURI());
	        	response.sendRedirect("/ece1779-img-project1/site/manager.jsp");
				
				} catch (Exception e) {
					e.printStackTrace();
				} 
			
			    }

}
