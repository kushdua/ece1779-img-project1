<%
   boolean loggedIn=false;
   boolean onWelcomeAlready=false;
   String userName = "";
   boolean logoutRequest = false;
   
   if(request!=null)
   {
	   onWelcomeAlready=request.getRequestURI().toString().contains("welcome.jsp");
	   if(onWelcomeAlready && request.getParameter("logout")!=null && request.getParameter("logout").compareTo("true")==0)
	   {
		   //response.sendRedirect("/ece1779-img-project1/servlet/Authenticate");
	       logoutRequest = true;
		   request.getSession().invalidate();
	
		   Cookie[] pageCookies = request.getCookies();
		   for (Cookie c : pageCookies) {
			   if (c.getName().compareTo("loggedIn") == 0) {
				   c.setMaxAge(0);
				   break;
			   }
		   }
		   //Session was invalidated, what would getSession() return? new session?
		   request.getSession().removeAttribute("errorMessage");
		   response.sendRedirect("/ece1779-img-project1/site/welcome.jsp");
		   return;
		}
	   else
	   {
			// URI takes only base URL no parameters so view.jsp?redirect=welcome.jsp should redirect to welcome.jsp?redirect=view.jsp
			if (request.getSession().getAttribute("username") == null) {
				//getServletContext().getRequestDispatcher("/site/welcome.jsp?redirect="+request.getRequestURI()).forward(request, response);
				if (!onWelcomeAlready) {
		            request.getSession().setAttribute("errorMessage", new String("Please login first."));
					response.sendRedirect("/ece1779-img-project1/site/welcome.jsp?redirect="
							+ request.getRequestURI());
					return;
				}
	
				if(request.getCookies()!=null)
				{
		            for (Cookie c : request.getCookies()) {
		                if (c.getName().compareTo("username") == 0) {
		                    userName = c.getValue();
		                    loggedIn = true;
		                }
		            }
				}
			} else {
				userName = request.getSession().getAttribute("username").toString();
				loggedIn = true;
			}
		    
	        if (loggedIn == false && !onWelcomeAlready && !logoutRequest) {
	            request.getSession().setAttribute("errorMessage", new String("Please login first."));
	            //getServletContext().getRequestDispatcher("/site/welcome.jsp?redirect="+request.getRequestURI()).forward(request, response);
	            response.sendRedirect("/ece1779-img-project1/site/welcome.jsp?redirect="
	                    + request.getRequestURI());
	            return;
	        }
	        else if(loggedIn == true && onWelcomeAlready)
	        {
	               response.sendRedirect("/ece1779-img-project1/site/view.jsp");
	               return;
	        }
	   }
   }
%>

<div class="navbar navbar-inverse">
    <div class="navbar-inner">
        <a class="brand" href="#">ECE1779 AWS Project</a>
        <ul class="nav">
            <%= (request==null || request.getRequestURI().contains("welcome.jsp"))?
            		"<li><a href='welcome.jsp' class='active'>Home</a></li>" : 
            		"<li><a href='welcome.jsp'>Home</a></li>" %>
        <% if(loggedIn && request!=null) { %>
            <%= (request.getRequestURI().contains("upload.jsp"))?
                    "<li><a href='upload.jsp' class='active'>Upload</a></li>" : 
                    "<li><a href='upload.jsp'>Upload</a></li>" %>
            <%= (request.getRequestURI().contains("view.jsp"))?
                    "<li><a href='view.jsp' class='active'>View Gallery</a></li>" : 
                    "<li><a href='view.jsp'>View Gallery</a></li>" %>
         <% if(userName.compareTo("root")==0) { %>
            <%= (request.getRequestURI().contains("manager.jsp"))?
                    "<li><a href='manager.jsp' class='active'>Manager UI</a></li>" : 
                    "<li><a href='manager.jsp'>Manager UI</a></li>" %>
         <% } %>
            <%= (request!=null && request.getRequestURI().contains("welcome.jsp?logout=true"))?
                    "<li><a href='welcome.jsp?logout=true' class='active'>Logout</a></li>" : 
                    "<li><a href='welcome.jsp?logout=true'>Logout</a></li>" %>
        <% } %>
        </ul>
    </div>
</div>

<% if(request!=null && request.getSession()!=null && request.getSession().getAttribute("errorMessage")!=null &&
      !request.getSession().getAttribute("errorMessage").toString().isEmpty()) { %>
<div class="alert alert-error">  
  <a class="close" data-dismiss="alert">×</a>  
  <strong>Error: </strong> <%= request.getSession().getAttribute("errorMessage").toString() %> 
  <% request.getSession().removeAttribute("errorMessage"); %> 
</div> 
<% } else if(request!=null && request.getSession()!=null && request.getSession().getAttribute("successMessage")!=null &&
             !request.getSession().getAttribute("successMessage").toString().isEmpty()) { %>
<div class="alert alert-success">  
  <a class="close" data-dismiss="alert">×</a>  
  <strong>Success: </strong> <%= request.getSession().getAttribute("successMessage").toString() %>  
  <% request.getSession().removeAttribute("successMessage"); %>
</div> 
<% } %>