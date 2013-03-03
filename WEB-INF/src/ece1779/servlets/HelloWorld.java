package ece1779.servlets;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class HelloWorld extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet( HttpServletRequest request,
	               HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Hello World!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Hello World! Local copy");
        out.println("</body>");
        out.println("</html>");
    }
	
	public void doPost(HttpServletRequest request,
            HttpServletResponse response)
	throws IOException, ServletException 
	{
		doGet(request,response);
	}
}
