package jp.gr.java_conf.utamaro.usobasic.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

public class Sound {
	private JavaScriptObject oscillator=null;
	private JavaScriptObject audio=null;
	private boolean isLooping=false;

	public static void beep(){
		final Sound beep=new Sound();
		beep.on(2000,false);
		new Timer() {
			public void run() {beep.off();}
		}.schedule(200);
	}

	public static void startup(){
		final Sound s=new Sound();
		s.on(2000,false);
		new Timer() {
			public void run() {
				s.off();
				s.on(1000,false);
				new Timer() {
					public void run() {s.off();}
				}.schedule(200);
			}
		}.schedule(200);
	}

	public void on(boolean isLoop){
		if(isLoop) isLooping=true;
		on(2000,isLoop);
	}

	native private JavaScriptObject on(int freq,boolean isLoop) /*-{
		var osc=this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::oscillator;
		var aud=this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::audio;
		if (!$wnd.AudioContext && !$wnd.webkitAudioContext){
			if(freq==2000 || freq==1000){
				if(!aud){
					aud=$doc.createElement("bgsound");
					$doc.getElementsByTagName('head')[0].appendChild(aud);
				}
				if(isLoop) aud.loop="infinite";
				else aud.loop="1";
				aud.src="freq"+freq+".wav";
				aud.volume="0";
				this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::audio=aud;
			}
		}else{
			var audioCtx = new ($wnd.AudioContext || $wnd.webkitAudioContext)();
			osc = audioCtx.createOscillator();	
			var gainNode = audioCtx.createGain();
			osc.connect(gainNode);
			gainNode.connect(audioCtx.destination);
			gainNode.gain.value = 0.3;
			osc.type = 'square';
			osc.frequency.value = freq; // value in hertz
			osc.start();
			this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::oscillator=osc;
		}
	}-*/;

	native public void off() /*-{
		var osc=this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::oscillator;
		var aud=this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::audio;
		var isL=this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::isLooping;
		if(osc){
			osc.stop();
		}
		if(aud && isL){
			this.@jp.gr.java_conf.utamaro.usobasic.client.Sound::isLooping=false;
			aud.src="";
			aud.volume="-10000";
		}
	}-*/;


}
