<!DOCTYPE html>
<html>
<head>
<title>Upload image</title>
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

<div class="container">
    <h1>Upload an image</h1>
    <div class="container">
		<form action="/ece1779-img-project1/servlet/FileUpload"  enctype="multipart/form-data" method="post">
		<!-- User ID: <input class="span4" type="text" name="userID" placeholder="User ID"><br /> -->
		<input type="hidden" name="userID" value="<%= userName %>" >
		Images to upload: <input class="span10" type="file" name="theFile" placeholder="Image File to Upload" accept="image/jpg,image/jpeg,image/png,image/gif,image/bmp,image/tiff"><br />
		
		<input class="btn btn-large btn-primary" type="submit" value="Send" >
		<input class="btn btn-large btn-primary" type="reset">
		
		</form>
	</div>

</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

