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
			PreparedStatement stmt = con.prepareStatement("select key1,key2,key3,key4 from images where login=?;");
			stmt.setString(1, userName);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next())
			{
				//Login successful
      			//con.close();
				//session.setAttribute("username", name);
				%>
      			 <li class="span4">
	                 <a href="#" class="thumbnail">
	                     <img data-src="s3.amazonaws.com/ece1779-group1/<%=rs.getInt(1)%>" alt="">
	                 </a>
                 	<!--<h2>Thumbnail label</h2> -->
	     	       	<!-- <p>Thumbnail caption...</p> -->
	     	       	<a class="btn btn-primary" href="#">View transformations</a>
	     	       	<a class="btn" href="#">Download</a>
             	</li>
             <%
			}
			else
			{
				//Login unsuccessful
      			con.close();
    			//request.getSession().setAttribute("errorMessage", new String("Invalid username/password combination."));
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

%>
       
    </ul>
</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

