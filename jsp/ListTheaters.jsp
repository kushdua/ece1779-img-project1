<!-- Begin: ListTheatres.jsp -->
<%@ page import="ece1779.jsp.Movieplex" %>
<%@ page import="ece1779.jsp.Theater" %>


<html>
	<head>
		<style>
			tr {font-family:Helvetica, Arial; font-size:9pt;}
		</style>
	</head>
	<body>
		<table>
			<%
				Movieplex moviedata = (Movieplex)application.getAttribute("moviedata");
				
				
				for (int i=0; i < moviedata.theaters.length; i++)
				{
			%>
			<tr><td><hr /></td></tr>
			<tr>
				<td>
					<form action='ShowTheater.jsp'>
						<%= moviedata.theaters[i].name %><br/>
						<%= moviedata.theaters[i].address %><br />
						<%= moviedata.theaters[i].phone %><br />
						<input type="hidden" name='tid' value='<%= moviedata.theaters[i].id%>' />
						<input type="submit" value='Select' />
					</form> 
				</td>
			</tr>
			<%
				}
			%>
		</table>
	</body>
</html>
