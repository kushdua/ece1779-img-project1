<!DOCTYPE html>
<html>
<head>
<title>View Image Gallery</title>
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

<%@ include file="header.jsp" %>

<%@page import = "java.sql.Connection"%>
<%@page import = "javax.sql.DataSource"%>
<%@page import = "java.sql.PreparedStatement"%>
<%@page import = "java.sql.ResultSet"%>
<!-- if parameter not specified to page, all uploaded images displayed; else only transformations for specified image are displayed by server side script which writes out page -->
<div class="container">
    <ul class="thumbnails">

<%
    /*
    Purpose: Login to the database and access the user images saved
    */
    if(userName!=null) {
		Connection con = null;
		
		try {
			// Get DB connection from pool
			DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");

			con = dbcp.getConnection();

			// Execute SQL query
			PreparedStatement stmt = con.prepareStatement("select key1,key2,key3,key4 from images where userId IN (SELECT id FROM users WHERE login=?);");
			stmt.setString(1, userName);
			
			ResultSet rs = stmt.executeQuery();
			int numPrinted = 0;
			
			while(rs.next())
			{
				//Login successful
      			//con.close();
				//session.setAttribute("username", name);
				numPrinted++; %>
				 <li><span class="span3">Original Image</span><span class="span6">Transformations</span></li>
      			 <li>
	                 <a href="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(1)%>" class="thumbnail span3">
	                     <img src="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(1)%>" alt="">
	                 </a>
	                 
                     <a href="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(2)%>" class="thumbnail span2">
                         <img src="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(2)%>" alt="">
                     </a>
                     
                     <a href="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(3)%>" class="thumbnail span2">
                         <img src="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(3)%>" alt="">
                     </a>
                     
                     <a href="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(4)%>" class="thumbnail span2">
                         <img src="http://s3.amazonaws.com/ece1779-group1/<%=rs.getString(4)%>" alt="">
                     </a>
             	</li>
             <%}
			if(numPrinted==0)
			{ %>

			<div class="alert alert-error">
				<a class="close" data-dismiss="alert">×</a>
				<strong>No uploaded images.</strong>
			</div>

			<% }
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

%>
       
    </ul>
</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

