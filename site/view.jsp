<!DOCTYPE html>
<html>
<head>
<title>View Image Gallery</title>
<!-- Sign in template from Bootstrap site modified for ECE1779 AWS project -->
<!-- Bootstrap -->
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
<link href="boostrap/css/bootstrap-responsive.css" rel="stylesheet">

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
</head>
<body>

<div class="navbar navbar-inverse">
	<div class="navbar-inner">
		<a class="brand" href="#">ECE1779 AWS Project</a>
		<ul class="nav">
			<li><a href="welcome.php">Home</a></li>
			<li><a href="upload.php">Upload</a></li>
			<li><a href="view.php">View Gallery</a></li>
			<li><a href="#">Logout</a></li>
		</ul>
	</div>
</div>

<!-- if parameter not specified to page, all uploaded images displayed; else only transformations for specified image are displayed by server side script which writes out page -->
<div class="container">
    <ul class="thumbnails">
    <li class="span4">
    <a href="#" class="thumbnail">
    <img data-src="holder.js/300x200" alt="">
    </a>
    <h3>Thumbnail label</h3>
	<p>Thumbnail caption...</p>
	<a class="btn btn-primary" href="#">View transformations</a>
	<a class="btn" href="#">Download</a>
    </li>
    ...
    </ul>
</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>

