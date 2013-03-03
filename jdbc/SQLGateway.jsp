<%@ page language="java" contentType="text/html; 
         charset=US-ASCII" pageEncoding="US-ASCII"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.DataSource" %>

<html>
	<head>
    <title>SQLGateway</title>
	</head>
 
	<body>     
		<h4>SQLGateway</h4> 	

		<form action="SQLGateway.jsp">
			<c:choose>
			  <c:when test="${!empty param.query}" >
			 	<textarea name="query" label="Query" cols="80" rows="10"><c:out value="${fn:trim(param.query)}" /></textarea>
			  </c:when>
			  <c:otherwise>
				<textarea name="query" label="Query" cols="80" rows="10"></textarea>
			  </c:otherwise>  
		 	</c:choose>
    		<input type="submit" />
		</form>		
		
		<%
			Connection con = null;
		
		%>
		
		
		<c:catch var="e">
		
		<c:if test="${!empty param.query}" >
		
			<% 	
				String query = request.getParameter("query");
			
				// Get DB connection from pool
			    DataSource dbcp = (DataSource)application.getAttribute("dbpool");
	%>
	DBCP = <%= dbcp %><%
			    con = dbcp.getConnection();
	%>; CON = <%= con  %><%
			    // Execute SQL query
			    Statement stmt = con.createStatement();
			    ResultSet rs = stmt.executeQuery(query);
	
			    ResultSetMetaData rsmeta = rs.getMetaData();
			    int numberOfColumns = rsmeta.getColumnCount();
			%>
			    
			<table border="1">
			<tr>
			     
			<%     
			    // Print column names
			    for (int x=1; x <= numberOfColumns; x++)  {
			%>
			    	
				<th> <%= rsmeta.getColumnName(x) %> </th>
				
			<%  } %>
				
			</tr>
	
			<% 
			    // Print tuples
			    while(rs.next()) {
				
			%>		
					<tr>
						<% for (int x=1; x <= numberOfColumns; x++) {
						%>
							<td> <%=  rs.getString(x) %> </td>
						<% } %>
					</tr>
				
			
			<%
				}
			
			%>
			
			</table>
			<%
				rs.close();
				con.close();
			%>
			</c:if>
			
			</c:catch>

		 <c:if test="${e!=null}">
 			<c:out value="${e.message}" />
 			<% 
 			
 			if(con!=null) con.close();
 			
 			%>
		 </c:if>
			
	</body>   
</html>
 
	    
	    
