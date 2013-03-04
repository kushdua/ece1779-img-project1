<!DOCTYPE html>
<html>
<head>
<title>Welcome</title>
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

<!--  Look at request attributes for success/errorMessage and display as appropriate
      Add redirect GET parameters and isLoggedIn check (session and cookie) -->

<%@ include file="header.jsp" %>

<div class="container">
  
  <form class="form-welcome" action="/servlet/Authenticate" method="post">
    <h2 class="form-welcome-heading">Please sign in</h2>
    <input type="text" name="userNameText" class="input-block-level" placeholder="User name">
    <input type="password" name="passText" class="input-block-level" placeholder="Password">
    <label class="checkbox">
      <input type="checkbox" value="remember-me"> Remember me
    </label>
    <button class="btn btn-large btn-primary" type="submit" value="signinAcc">Sign in</button>
    <button class="btn btn-large btn-primary" type="submit" value="createAcc">Create Account</button>
  </form>

</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>