package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileServiceAsync {
	void load(String name, AsyncCallback<String> callback);
	void save(String name,String content, AsyncCallback<Boolean> callback);
	void files(AsyncCallback<ArrayList<String>> callback);
	void kill(String name,AsyncCallback<Boolean> callback);
	void name(String from,String to,AsyncCallback<Boolean> callback);
}
