<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.sql.*" %>

<html>
	<head>
    <title>Trivial JDBC Example!</title>
	</head>
 
	<body>    
		<h4>Trivial JDBC Example!</h4> 	
		<table border='1'>

	
		  <%
		  Class.forName("com.mysql.jdbc.Driver"); 
	      String url = "jdbc:mysql://ece1779db.cf2zhhwzx2tf.us-east-1.rds.amazonaws.com/ece1779?" + "user=ece1779&password=Sp2012";
	      Connection con = DriverManager.getConnection(url);
	
	      Statement stmt = con.createStatement();
	
	      String query="select * from students";
	    
	      ResultSet rs = stmt.executeQuery(query);
	      
	      while(rs.next()) { 
	      %>
			<tr>
				<td><%= rs.getInt("id") %></td>
				<td><%= rs.getString("name") %></td>
			</tr>
	      
	      <%
	      }
	      rs.close();
	      stmt.close();
	      con.close();
		  %>

		</table>
	</body>   
</html>
