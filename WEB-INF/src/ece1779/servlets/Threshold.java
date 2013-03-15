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
				String poolResizeDelay = request.getParameter("poolResizeDelay");
				
				try
				{
					int intCPUThresholdGrowing = Integer.parseInt(cpuThresholdGrowing);
					int intCPUThresholdShrinking = Integer.parseInt(cpuThresholdShrinking);
					int intRatioExpandPool = Integer.parseInt(ratioExpandPool);
					int intRatioShrinkPool = Integer.parseInt(ratioShrinkPool);
					int intPoolResizeDelay = Integer.parseInt(poolResizeDelay);
					
					if(intCPUThresholdGrowing < 0 || intCPUThresholdGrowing > 200)
					{
						request.setAttribute("errorMessage", "CPU Grow Threshold must be between 0 and 200%. You provided: " + cpuThresholdGrowing);
					}
					else if(intCPUThresholdShrinking < 0 || intCPUThresholdShrinking > 200)
					{
						request.setAttribute("errorMessage", "CPU Shrink Threshold must be between 0 and 200%. You provided: " + cpuThresholdGrowing);
					}
					else if(intRatioExpandPool < 0 || intRatioExpandPool > 5)
					{
						request.setAttribute("errorMessage", "Pool Expand ratio must be between 0 and 5. You provided: " + cpuThresholdGrowing);
					}
					else if(intRatioShrinkPool < 0 || intRatioShrinkPool > 5)
					{
						request.setAttribute("errorMessage", "Pool Shrink ratio must be between 0 and 5. You provided: " + cpuThresholdGrowing);
					}
					else if(intPoolResizeDelay < 0 || intPoolResizeDelay > 120)
					{
						request.setAttribute("errorMessage", "Pool Resize delay must be between 0 and 120 seconds. You provided: " + cpuThresholdGrowing);
					}
				}
				catch(NumberFormatException e)
				{
					request.setAttribute("errorMessage", "Invalid input provided.\nCPU grow threshold: " + cpuThresholdGrowing + 
							"\nCPU shrink threshold: " + cpuThresholdShrinking +
							"\nRatio pool expand: " + ratioExpandPool + 
							"\nRatio pool shrink: " + ratioShrinkPool + 
							"\nPool resize delay: " + poolResizeDelay);
				}
				
				LoadBalancerLibrary.getInstance().setThresholdsAndRatios(cpuThresholdGrowing,cpuThresholdShrinking,ratioExpandPool,ratioShrinkPool, poolResizeDelay);
				
				response.setContentType("text/html");
	        	//response.sendRedirect(request.getRequestURI());
	        	response.sendRedirect("/ece1779-img-project1/site/manager.jsp");
				
				} catch (Exception e) {
					e.printStackTrace();
				} 
			
			    }

}
