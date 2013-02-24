<!-- Begin: ShowTheatre.jsp -->
<%@ page import="ece1779.jsp.Movieplex" %>
<%@ page import="ece1779.jsp.Theater" %>
<%@ page import="ece1779.jsp.Movie" %>
<%@ page import="ece1779.jsp.Showtime" %>
<%@ page import="java.util.Vector" %>


<!--  Example of JSP Declaration -->
<!--  This code will be compiled outside the main JSP function -->

<%!		 
	Theater getTheater(Movieplex moviedata, int tid) {
		for (int i=0; i < moviedata.theaters.length; i++) {
			if (moviedata.theaters[i].id == tid)
				return moviedata.theaters[i];
		}
		return null;	
	} 

	Movie getMovie(Movieplex moviedata, int mid) {
		for (int i=0; i < moviedata.movies.length; i++) {
			if (moviedata.movies[i].id == mid)
				return moviedata.movies[i];
		}
		return null;	
	} 

	Vector getMoviesForTheater(Movieplex moviedata, int tid) {
		Vector movies = new Vector();
		for (int i=0; i < moviedata.showtimes.length; i++) {
			Showtime s = moviedata.showtimes[i];
			if (s.theaterid == tid) {
				Movie movie = getMovie(moviedata, s.movieid);
				if (!movies.contains(movie)) {
					movies.addElement(movie);
				}
			}
		}
		return movies;
	}	
%>

<html>
	<head>
		<style>
			tr {font-family:Helvetica, Arial; font-size:9pt;}
		</style>
	</head>
	<body>
		<table>
			<tr><td><hr /></td></tr>
			<%
				Movieplex moviedata = (Movieplex)application.getAttribute("moviedata");
				int tid = new Integer(request.getParameter("tid")).intValue();
				Theater theater = this.getTheater(moviedata,tid);
			%>
			<tr>
				<td>
					<%= theater.name %><br/>
					<%= theater.address %><br />
					<%= theater.phone %><br />
				</td>
			</tr>
			<%
				Vector movies =	getMoviesForTheater(moviedata,tid);
				for (int i=0; i < movies.size(); i++) {
					Movie m = (Movie)movies.elementAt(i);	
			%>
					<tr><td><hr /></td></tr>

					<tr>
						<td>
							<%= m.name %><br/>
							Director:<%= m.director %><br />
							Synopsis:<%= m.synopsis %><br />
						</td>
					</tr>
			<%
				}
			%>				
		</table>
	</body>
</html>
