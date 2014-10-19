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
<link type="text/css" rel="stylesheet" href="UsoBasic.css">
<link rel="shortcut icon" href="icon/favicon.ico" />
<title>USO 800 BASIC</title>

 <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
 <script type="text/javascript" src="usobasic/usobasic.nocache.js"></script>
 <%
 	 DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
   String name = request.getParameter("file");
   		Query query = new Query("File");
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		for(Entity e : preparedQuery.asIterable()){
			if(e.getProperty("name").equals(name)){
					Text t=(Text)e.getProperty("content");
%>		
	<script type="text/usobasic">
						<%= t.getValue()%>
</script>
<%
		}
		}
 %>
</script>
<script>
	var prefix=["webkit","moz","o","ms"];

function access(isWrite){
    $.ajax({
      url: '/usobasic/comment',
      type:'POST',
      dataType: 'json',
      data : isWrite? "content="+$("#content").val():"content=",
      timeout:60000,
      success: function(data) {
      $("#posted").html("");
      for(var i=data.length-1;i>=0;i--){
      var cont=data[i].content.replace(/\n/g,"<br>");
              $("#posted").append(data[i].number+": "+data[i].date+"<br>"+cont+"<br><br>");
              }
      },
      error: function(XMLHttpRequest, textStatus, errorThrown) {
      }
    });
}

function setup(){
$('#form1').submit(function(event) {
    event.preventDefault();
    access(true);
});
}

function scale(){
	var ele=$("#usobasic_root");
	var w=$(window).width();
	var h=$(window).height();
	var scaleX=(w/700)*0.9;
	var scaleY=(h/400)*0.9;
	var scale=Math.min(scaleX,scaleY);
	for(var i=0;i<prefix.length;i++){
		ele.css("-"+prefix[i]+"-transform","scale("+scaleX+","+scaleY+")");
	}
	var fs=$("#fs");
	fs.css("left",w*0.9+"px");
	
}

function fullScreen(){
	var ele=$("#usobasic_root");
	var isFulled=false;
	for(var i=0;i<prefix.length;i++){
		if (ele.get(0)[prefix[i]+"RequestFullScreen"]) {
			ele.get(0)[prefix[i]+"RequestFullScreen"]();
			isFulled=true;
		}
	}
	if(isFulled){
		scale();
	}
}
var timer=false;

$(function(){
	scale();
	$(window).resize(function(){
    	if (timer !== false) {
        	clearTimeout(timer);
    	}
    	timer = setTimeout(
    		function() {
				scale();
    		}
    	, 200);
	});
});

</script>
<style type="text/css">
body {
 background:#000000;
 color:#ffffff;
 }
#usobasic_root {
	position: absolute;
	top: 0%;
	left: 0%;
	-moz-transform-origin:0 0;
	-o-transform-origin:0 0;
	-webkit-transform-origin:0 0;
	-ms-transform-origin:0 0;
	
}
#fs {
position:absolute;
top:0px;
left:0px;
	z-index:100;
}
#questionair {
	position: absolute;
	top: 0px;
	left: 1050px;
}

</style>
</head>

<button id="fs" onclick="fullScreen()">大</button>
	<div id="usobasic_root"></div>
	<!--
	<div id="questionair">
	ダメ出し・メモ・伝言・覚書・思い出・文句・禿げ増し等、なんでも残していってください。
<form data-abide id="form1">
 <textarea id="content" name="content" rows="4"></textarea>
<input type="submit" value="send">
<input type="button" value="reload" onclick="access(false)">
<input type="reset" value="reset">
</form> 
<div id="posted"></div>
	</div>
-->
</body>
</html>
