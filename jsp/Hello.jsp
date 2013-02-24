<html>
	<head></head>
		<title>Simple JSP example</title>
	<body>
		<h1>Hi!</h1>
		Your browser is: 
		<%= request.getHeader("User-Agent") %>
	</body>
</html>