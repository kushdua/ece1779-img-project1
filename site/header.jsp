
<%
   boolean loggedIn=false;
   boolean onWelcomeAlready=request.getRequestURL().toString().contains("welcome.jsp");
   
   if(onWelcomeAlready && request.getParameter("logout")!=null && request.getParameter("logout").compareTo("true")==0)
   {
	   //response.sendRedirect("/ece1779-img-project1/servlet/Authenticate");
	   request.getSession().invalidate();

	   Cookie[] pageCookies = request.getCookies();
	   for (Cookie c : pageCookies) {
		   if (c.getName().compareTo("loggedIn") == 0) {
			   c.setMaxAge(0);
			   break;
		   }
	   }
	   response.sendRedirect("/ece1779-img-project1/site/welcome.jsp");
	}
   else
   {
		// URI takes only base URL no parameters so view.jsp?redirect=welcome.jsp should redirect to welcome.jsp?redirect=view.jsp
		if (request.getSession().getAttribute("username") == null) {
			request.setAttribute("errorMessage", "Please login first.");
			//getServletContext().getRequestDispatcher("/site/welcome.jsp?redirect="+request.getRequestURI()).forward(request, response);
			if (!onWelcomeAlready) {
				response.sendRedirect("/ece1779-img-project1/site/welcome.jsp?redirect="
						+ request.getRequestURI());
			}
		} else {
			if (request.getSession().getAttribute("username") != null) {
				loggedIn = true;
			} else {
				for (Cookie c : request.getCookies()) {
					if (c.getName().compareTo("username") == 0) {
						loggedIn = true;
					}
				}
			}
	
			if (loggedIn == false && !onWelcomeAlready) {
				request.setAttribute("errorMessage", "Please login first.");
				//getServletContext().getRequestDispatcher("/site/welcome.jsp?redirect="+request.getRequestURI()).forward(request, response);
				response.sendRedirect("/ece1779-img-project1/site/welcome.jsp?redirect="
						+ request.getRequestURI());
			}
		}
   }
%>

<div class="navbar navbar-inverse">
    <div class="navbar-inner">
        <a class="brand" href="#">ECE1779 AWS Project</a>
        <ul class="nav">
            <%= (request.getRequestURI().contains("welcome.jsp"))?
            		"<li><a href='welcome.jsp' class='active'>Home</a></li>" : 
            		"<li><a href='welcome.jsp'>Home</a></li>" %>
        <% if(loggedIn) { %>
            <%= (request.getRequestURI().contains("upload.jsp"))?
                    "<li><a href='upload.jsp' class='active'>Upload</a></li>" : 
                    "<li><a href='upload.jsp'>Upload</a></li>" %>
            <%= (request.getRequestURI().contains("view.jsp"))?
                    "<li><a href='view.jsp' class='active'>View Gallery</a></li>" : 
                    "<li><a href='view.jsp'>View Gallery</a></li>" %>
            <%= (request.getRequestURI().contains("manager.jsp"))?
                    "<li><a href='manager.jsp' class='active'>Manager UI</a></li>" : 
                    "<li><a href='manager.jsp'>Manager UI</a></li>" %>
            <%= (request.getRequestURI().contains("welcome.jsp?logout=true"))?
                    "<li><a href='welcome.jsp?logout=true' class='active'>Logout</a></li>" : 
                    "<li><a href='welcome.jsp?logout=true'>Logout</a></li>" %>
        <% } %>
        </ul>
    </div>
</div>

<% if(request.getAttribute("errorMessage")!=null) { %>
<div class="alert alert-error">  
  <a class="close" data-dismiss="alert">×</a>  
  <strong>Error: </strong> <%= request.getAttribute("errorMessage").toString() %>  
</div> 
<% } else if(request.getAttribute("errorMessage")!=null) { %>
<div class="alert alert-success">  
  <a class="close" data-dismiss="alert">×</a>  
  <strong>Success: </strong> <%= request.getAttribute("errorMessage").toString() %>  
</div> 
<% } %>