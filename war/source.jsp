<%@ page import ="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import ="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import ="com.google.appengine.api.datastore.Entity" %>
<%@ page import ="com.google.appengine.api.datastore.PreparedQuery" %>
<%@ page import  ="com.google.appengine.api.datastore.Query" %>
<%@ page import  ="com.google.appengine.api.datastore.Text" %>
<%@ page language="java" 
    contentType="text/html; charset=UTF-8" %>

<!doctype html>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link rel="shortcut icon" href="favicon.ico" />
<title>USO 800 BASIC source code</title>


<body>
 <%
 	 DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
   String name = request.getParameter("file");
%>		
<h2>soruce code of <%=name%></h2><br> 
<code>
<%
   		Query query = new Query("File");
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		for(Entity e : preparedQuery.asIterable()){
			if(e.getProperty("name").equals(name)){
					Text t=(Text)e.getProperty("content");
					for(String str:t.getValue().split("\n")){
%>		
						<%= str%> <br>
<%
		}
		}
		}
 %>
 </code>
</body>
</html>
