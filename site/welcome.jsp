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

<div class="navbar navbar-inverse">
	<div class="navbar-inner">
		<a class="brand" href="#">ECE1779 AWS Project</a>
		<ul class="nav">
			<li><a href="#">Home</a></li>
		</ul>
	</div>
</div>

<div class="container">

  <form class="form-welcome">
    <h2 class="form-welcome-heading">Please sign in</h2>
    <input type="text" class="input-block-level" placeholder="Email address">
    <input type="password" class="input-block-level" placeholder="Password">
    <label class="checkbox">
      <input type="checkbox" value="remember-me"> Remember me
    </label>
    <button class="btn btn-large btn-primary" type="submit">Sign in</button>
    <button class="btn btn-large btn-primary" type="submit">Create Account</button>
  </form>

</div> <!-- /container -->

<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
</body>
</html>