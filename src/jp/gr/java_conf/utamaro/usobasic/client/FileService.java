package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("file")
public interface FileService extends RemoteService{
	String load(String name);
	Boolean save(String name,String content);
	ArrayList<String> files();
	Boolean kill(String name);
	Boolean name(String from,String to);
}
