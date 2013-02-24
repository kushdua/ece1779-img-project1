<%@ page language="java" contentType="text/html; 
         charset=US-ASCII" pageEncoding="US-ASCII"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
    <title>Values of query parameter p</title>
  </head>
  <body>
    <h2>Values of query parameter p</h2>
    <p>run this with
      <ul> <li>no query string</li>
           <li>query string <tt>?p=1</tt></li>
           <li>query string <tt>?p=1&amp;p=2</tt></li>
      </ul>
    </p>
    <c:if test="${param.p == null}"> param.p not defined<br /> </c:if>
    param.p first value: ${param.p}<br />
    param.p all values: ${fn:join(paramValues.p,",")}
  </body>
</html>
