package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class UsoBasic implements EntryPoint {
	private Environment env;
	private static Logger logger =Logger.getLogger("usobasic");

	public void onModuleLoad() {
		setup();
	}
	
	private void setup(){
		env=Environment.getEnvironment();
		boolean autoExec=false;
		NodeList<Element> scripts=Document.get().getElementsByTagName("script");
		for(int i=0;i<scripts.getLength();i++){
			Element script=scripts.getItem(i);
			if(script.getAttribute("type").equals("text/usobasic")){
				autoExec=true;
				String src=script.getAttribute("src");
				if(src==null || src.equals("")){
					env.program.set(script.getInnerText());
					env.executor.execute("run");
				}else{
					load(src);
				}
			}
		}
		if(!autoExec) env.tscreen.fakeStartup();
	}

	private void load(final String url){
		final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);  
		try {
			requestBuilder.sendRequest("GET", new RequestCallback() {  
				@Override  
				public void onError(Request request, Throwable exception) {  
					Window.alert("error while loading "+url);
				}  

				@Override
				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						env.program.set(response.getText());
						env.executor.execute("run");
					}else{
						Window.alert("error while loading "+url);
					}
				}  
			});
		} catch (RequestException e) {
			e.printStackTrace();
		}  
	}
}

