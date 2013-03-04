<!DOCTYPE html>
<html>
<head>
<title>Manage Workers</title>
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

<!--  
<div class="navbar navbar-inverse">
	<div class="navbar-inner">
		<a class="brand" href="#">ECE1779 AWS Project</a>
		<ul class="nav">
			<li><a href="welcome.jsp">Home</a></li>
			<li><a href="upload.jsp">Upload</a></li>
			<li><a href="view.jsp">View Gallery</a></li>
            <li><a href="manage.jsp" class="active">Manager UI</a></li>
			<li><a href="welcome.jsp?logout=true">Logout</a></li>
		</ul>
	</div>
</div>
-->

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
				    		<th>Worker</td>
				    		<th>Load</td>
				    	</tr>
						<tr>
							<td>Worker 1</td>
							<td>0 <!-- retrieve value from metrics gathering here --></td>
						</tr>
						<tr>
							<td>Worker 2</td>
							<td>0 <!-- retrieve value from metrics gathering here --></td>
						</tr>
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
					<form class="form-welcome">
						<input type="text" name="manualPoolSizeValue" class="input-block-level" value="2">
					  <button class="btn btn-large btn-primary" type="submit">Submit</button>
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
					<form class="form-welcome">
						<h2>CPU threshold for growing worker pool: </h2><input type="text" name="cpuThresholdGrowValue" class="input-block-level" value="2">
						<h3>CPU threshold for shrinking worker pool: </h2><input type="text" name="cpuThresholdShrinkValue" class="input-block-level" value="2">
						<h3>Ratio by which to expand worker pool (2 = doubles): </h2><input type="text" name="ratioExpandPoolValue" class="input-block-level" value="2">
						<h3>Ratio by which to shrink worker pool (4 = 75% off): </h2><input type="text" name="ratioShrinkPoolValue" class="input-block-level" value="2">
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

