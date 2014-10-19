package jp.gr.java_conf.utamaro.usobasic.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import jp.gr.java_conf.utamaro.usobasic.client.FileService;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;



import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FileServiceImpl extends RemoteServiceServlet implements FileService{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
	private static TimeZone timezone = TimeZone.getTimeZone("Asia/Tokyo");

	private Entity search(String name){
		Query query = new Query("File");
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		for(Entity fileEntity : preparedQuery.asIterable()){
			if(fileEntity.getProperty("name").equals(name)) return fileEntity;
		}
		return null;
	}
	public String load(String name){
		Entity e=search(name);
		if(e==null) return null;
		Text t=(Text)e.getProperty("content");
		return t.getValue();
	}
	public Boolean save(String name,String content){
		Entity entity = search(name);
		if(entity==null) entity=new Entity("File");
		entity.setProperty("name", name);
		entity.setProperty("content",new Text(content));
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(timezone);
		entity.setProperty("date",sdf.format(new Date()));
		entity.setProperty("date", sdf.format(new Date()));
		datastoreService.put(entity);
		return Boolean.TRUE;
	}
	public ArrayList<String> files(){
		Query query = new Query("File");
		PreparedQuery preparedQuery = datastoreService.prepare(query);
		ArrayList<String> fs=new ArrayList<>();
		for(Entity fileEntity : preparedQuery.asIterable()){
			fs.add((String)fileEntity.getProperty("name")+ " "+fileEntity.getProperty("date"));
		}
		return fs;
	}
	public Boolean kill(String name){
		Entity e=search(name);
		if(e==null) return Boolean.FALSE;
		datastoreService.delete(e.getKey());
		return Boolean.TRUE;
	}
	public Boolean name(String from,String to){
		Entity e=search(from);
		if(e==null) return Boolean.FALSE;
		e.setProperty("name", to);
		datastoreService.put(e);
		return Boolean.TRUE;
	}
}

