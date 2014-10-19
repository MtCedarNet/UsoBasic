package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;


public class ErrorInterrupt{
	private enum STATE{
		ON,OFF
	};
	private STATE state=STATE.ON;
	private int line=0;
	private static ProgramContext context=null;
	private ListNode<ASTstatement> errorLocation;
	private static Logger logger = Logger.getLogger("interrupt");

	public ErrorInterrupt(){
	}

	public void setOn(){
		state=STATE.ON;
	}
	public void setOff(){
		state=STATE.OFF;
	}
	public boolean isExecuting(){
		return state==STATE.OFF;
	}

	public static void setContext(ProgramContext context){
		ErrorInterrupt.context=context;
	}
	public void setLine(int line){
		this.line=line;
	}
	public boolean trigger(){
		if(state==STATE.ON && context!=null && line!=0){
			errorLocation=context.getCurrentNode();
			state=STATE.OFF;
			context.goTo(line);
			return true;
		}
		return false;
	}
	public void resume(int line){
		state=STATE.ON;
		if(line>0) context.goTo(line);
		else context.setNextStatement(errorLocation);
	}
	public void resumeNext(){
		state=STATE.ON;
		context.setNextStatement(errorLocation.next);
	}
}
