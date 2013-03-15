<!DOCTYPE html>
<html>
<head>
<title>Welcome</title>
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
  
  <form class="form-welcome" action="/ece1779-img-project1/servlet/Authenticate<% 
  if(request != null && request.getParameter("redirect")!=null) { request.setAttribute("redirect",request.getParameter("redirect")); } %>" method="post">
			<h2 class="form-welcome-heading">Please sign in</h2>
			<div class="row">
				<input type="text" name="userNameText"
					class="input-block-level span4" placeholder="User name">
			</div>
			<div class="row">
				<input type="password" name="passText"
					class="input-block-level span4" placeholder="Password">
			</div>
			<input type="checkbox" name="remember-me"
				value="remember-me"> Remember me
		    <br /><br />
			<button class="btn btn-large btn-primary" type="submit"
				value="signinAcc" name="signinAcc">Sign in</button>
			<button class="btn btn-large btn-primary" type="submit"
				value="createAcc" name="createAcc">Create Account</button>
		</form>

</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>