<!DOCTYPE html>
<%@page import="org.apache.tomcat.jni.Library"%>
<%@page import="java.util.Map"%>
<%@page import="ece1779.ec2.WorkerRecord"%>
<%@page import="java.util.HashMap"%>
<%@page import="ece1779.servlets.LoadBalancerLibrary"%>
<%@page import="com.amazonaws.AmazonClientException" %>
<%@page import="com.amazonaws.AmazonServiceException" %>
<%@page import="com.amazonaws.auth.BasicAWSCredentials" %>
<%@page import="com.amazonaws.auth.PropertiesCredentials" %>
<%@page import="com.amazonaws.services.cloudwatch.*" %>
<%@page import="com.amazonaws.services.cloudwatch.model.*" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Vector" %>
<%@page import="java.util.Date" %>

<html>
<head>
<title>Manage Workers</title>
<!-- Sign in template from Bootstrap site modified for ECE1779 AWS project -->
<!-- Bootstrap -->
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
<link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
</head>
<body>

<%
	String managerUserName = "";

	if (request.getSession().getAttribute("username") != null) {
		managerUserName = request.getSession().getAttribute("username").toString();
	} else {
		if(request.getCookies()!=null)
		{
			for (Cookie c : request.getCookies()) {
				if (c.getName().compareTo("username") == 0) {
					managerUserName = c.getValue();
				}
			}
		}
	}
	
	if (managerUserName.compareTo("root") != 0) {
		request.getSession().setAttribute("errorMessage", new String("Only root can access management console"));
		response.sendRedirect("/ece1779-img-project1/site/view.jsp");
		return;
	}
%>

<%
   LoadBalancerLibrary.getInstance().loadConfigParameters();		
%>

<%@ include file="header.jsp" %>

<!-- if parameter not specified to page, all uploaded images displayed; else only transformations for specified image are displayed by server side script which writes out page -->
<div class="container">
	<div class="accordion" id="accordion2">
		<div class="accordion-group">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseWorkers">
					Workers and Utilization
				</a>
			</div>
			 <div id="collapseWorkers" class="accordion-body collapse in">
				<div class="accordion-inner">
				    <table class="table table-striped">
				    	<tr>
				    		<th>Worker</th>
				    		<th>Instance ID</th>
				    		<th>CPU Load (Percent)</th>
				    	</tr>
				    	
<%
BasicAWSCredentials awsCredentials = (BasicAWSCredentials)getServletContext().getAttribute("AWSCredentials");

AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);

try {
        /* out.print("<p>");
        out.print("Namespace = " + namespace + " Metric = " + metricName + " Dimensions = " + dimensions);
        out.print(" Values = " + stats.toString());
        out.println("</p>"); */
        LoadBalancerLibrary.getInstance().updateWorkerStats(getServletContext());
        HashMap<String, WorkerRecord> workerPool = LoadBalancerLibrary.getInstance().getWorkerPool();
        int count = 0;
        
        if(workerPool.size() > 0)
        {
        	//Stats available
        	count++;
        	%> 
        	   <% String managerID = getServletContext().getInitParameter("managerInstanceID");
        	   for(Map.Entry<String, WorkerRecord> o : workerPool.entrySet())
        	   { 
        		    if(o.getValue().isActive() && o.getValue().getInstanceID().compareTo(managerID)!=0)
        		    {
        	   %>
                   <tr>
                      <td>Worker <%= count %></td>
        		      <td><%= o.getValue().getInstanceID() %></td>
        		      <td><%= Double.toString(o.getValue().getCpuLoad()) %></td>
                   </tr>
        	   <%   }
        		    else if(o.getValue().isActive() && o.getValue().getInstanceID().compareTo(managerID)==0)
        		    {
               %>
                   <tr>
                      <td>Manager</td>
                      <td><%= o.getValue().getInstanceID() %></td>
                      <td><%= Double.toString(o.getValue().getCpuLoad()) %></td>
                   </tr>
               <%   }
        	   }
        }
} catch (AmazonServiceException ase) { } catch (AmazonClientException ace) { }
%>
				    </table>
				</div>
			</div>
		</div>
		<div class="accordion-group">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseManualSet">
					Manually set worker pool size (# workers)
				</a>
			</div>
			 <div id="collapseManualSet" class="accordion-body collapse">
				<div class="accordion-inner">
					<form class="form-welcome" action="/ece1779-img-project1/servlet/WorkerPool" method="post">
						<input type="text" name="manualPoolSizeValue" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getSavedPoolSize() %>>
					  <button class="btn btn-large btn-primary" name="manualPoolSizeBtn" type="submit">Submit</button>
					</form>
				</div>
			</div>
		</div>
		<div class="accordion-group">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseAutoScale">
					Auto-scaling policy
				</a>
			</div>
			 <div id="collapseAutoScale" class="accordion-body collapse">
				<div class="accordion-inner">
					<form class="form-welcome" action="/ece1779-img-project1/servlet/Threshold" method="post">
						<h3>CPU threshold for growing worker pool: </h3><input type="text" name="cpuThresholdGrowValue" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getCpuThresholdGrowing() %>>
						<h3>CPU threshold for shrinking worker pool: </h3><input type="text" name="cpuThresholdShrinkValue" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getCpuThresholdShrinking() %>>
						<h3>Ratio by which to expand worker pool (2 = doubles): </h3><input type="text" name="ratioExpandPoolValue" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getRatioExpandPool() %>>
						<h3>Ratio by which to shrink worker pool (4 = 75% off): </h3><input type="text" name="ratioShrinkPoolValue" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getRatioShrinkPool() %>>
                        <h3>Delay between worker pool size adjustments (s): </h3><input type="text" name="poolResizeDelay" class="input-block-level" value=<%= LoadBalancerLibrary.getInstance().getPoolResizeDelay() %>>
					  	<button class="btn btn-large btn-primary" type="submit">Submit</button>
					</form>
				</div>
			</div>
		</div>
	</div>
</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

