package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.logging.Logger;


public class Interrupt{
	public enum STATE{
		ON,OFF,STOP
	};
	private STATE state=STATE.OFF;
	private int line=0;
	private boolean pending=false;
	private static ProgramContext context=null;
	private static Logger logger = Logger.getLogger("interrupt");

	public Interrupt(){
	}
	
	public void setState(STATE s){
		state=s;
		if(state==STATE.ON && pending){
			pending=false;
			trigger();
		}
	}

	public static void setExecutor(ProgramContext context){
		Interrupt.context=context;
	}
	public void setLine(int line){
		this.line=line;
	}
	public boolean trigger(){
		if(state==STATE.STOP && context!=null){
			pending=true;
			return true;
		}
		if(state==STATE.ON && context!=null && line!=0){
			context.gosub(line);
			return true;
		}
		return false;
	}
}
