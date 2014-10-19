package jp.gr.java_conf.utamaro.usobasic.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import net.arnx.jsonic.JSON;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Key;


@SuppressWarnings("serial")
public class CommentServlet extends HttpServlet {

	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
	private static TimeZone timezone = TimeZone.getTimeZone("Asia/Tokyo");

	private int getLastNumber(){
        Key key = KeyFactory.createKey("CommentNumber", 1);
        Entity entity;
        int n;
        try {
			entity= datastoreService.get(key);
			n=Integer.parseInt(entity.getProperty("number").toString());
			entity.setProperty("number",++n);
			
		} catch (EntityNotFoundException e) {
			entity= new Entity(key);
			n=0;
		}
        entity.setProperty("number",Integer.toString(n));
		datastoreService.put(entity);
        return n;
	}
	private String escape(String str){
		return str.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">", "&gt;") 
				.replaceAll("\"","&quot;").replaceAll("'", "&#39;");
	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws IOException {
		
		String content = request.getParameter("content");
		if(!content.equals("")){
			Entity entity=new Entity("Comment");
			entity.setProperty("number",getLastNumber());
			entity.setProperty("content",new Text(content));
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(timezone);
			entity.setProperty("date",sdf.format(new Date()));
			datastoreService.put(entity);
		}
		Query query = new Query("Comment");
		query.addSort("number", SortDirection.ASCENDING);
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		ArrayList<Map<String,String>> lists=new ArrayList<>();
		for(Entity e: preparedQuery.asIterable()){
			HashMap<String,String> m=new HashMap<>();
			m.put("content", escape(((Text)(e.getProperty("content"))).getValue()));
			m.put("date", e.getProperty("date").toString());			
			m.put("number", e.getProperty("number").toString());			
			lists.add(m);
		}
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		if(lists.size()>0){
			String s=JSON.encode(lists);
			out.print(s);
		}
		else
			out.print("");
	}
}