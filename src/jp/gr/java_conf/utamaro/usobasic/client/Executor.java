package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

import jp.gr.java_conf.utamaro.usobasic.client.parser.*;

public class Executor {

	private enum STATE {
		EXECUTING,STOP,SUSPEND,WAIT_INPUT;
	}

	private boolean tron=false;
	private Context current=null;
	private UsoBasicVisitor visitor=null;
	private Environment env=null;
	private int errorLine=-1;
	private int errorId=-1;
	private STATE state=STATE.STOP;
	private ASTstatement nowNode=null;
	private Logger logger =Logger.getLogger("executor");


	public void tron(){
		tron=true;
	}
	public void troff(){
		tron=false;
	}
	public void setSuspend(){
		state=STATE.SUSPEND;
	}
	public void setWait(){
		state=STATE.WAIT_INPUT;
	}
	public void setExecuting(){
		state=STATE.EXECUTING;
		exec();
	}
	public boolean isSuspend(){
		return state==STATE.SUSPEND;
	}
	public boolean isStop(){
		return state==STATE.STOP;
	}
	public boolean isWait(){
		return state==STATE.WAIT_INPUT;
	}
	public boolean isExecuting(){
		return state==STATE.EXECUTING;
	}

	public int getErrorId(){
		return errorId;
	}
	public int getErrorLine(){
		return errorLine;
	}
	private boolean handleException(Throwable t){
		logger.log(Level.SEVERE,"",t);

		BasicError e=null;
		for(Throwable u=t;u!=null;u=u.getCause()){
			if(u instanceof BasicError){
				e=(BasicError)u;
				break;
			}
		}
		if(nowNode!=null) errorLine=nowNode.getLine();
		if(e!=null){
			errorId=e.getId();
			if(e.getId()!=BasicError.BREAK && env.onError.trigger()){
				return true;
			}else{
				stop();
				Sound.beep();
				if(current==null || current.isDirectMode()){
					env.tscreen.print(e.getMessage()+"\r",false);
				}else{
					env.tscreen.print(e.getMessage()
							+" in "+errorLine+"\r",false);
				}
				return false;
			}
		}else{
			stop();
			Sound.beep();
			if(current==null || current.isDirectMode()){
				env.tscreen.print("Internal Error"+"\r",false);
			}else{
				env.tscreen.print("Internal Error"
						+" in "+errorLine+"\r",false);
			}
			logger.log(Level.SEVERE,"",t);
			return false;
		}
	}
	private void setupExceptionHandler(){
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable t) {
				handleException(t);
				env.tscreen.print("",true);
				logger.log(Level.SEVERE,"!!!unhandled!!!:",t);
			}
		});
	}


	public Executor(Environment env){
		this.env=env;
		setupExceptionHandler();
	}

	public void stopCheck(){
		try{
			BasicError.check(!env.onError.isExecuting(),BasicError.NO_RESUME);
			if(visitor!=null){
				visitor.endCheck();
			}
		}catch(BasicError e){ //for/while without next/wend
			handleException(e);
		}
	}

	public void stop(){
		if(visitor!=null){
			visitor.end();
		}
		env.executingKeyBuffer.clear();
		state=STATE.STOP;
		env.tscreen.setDirectModeKeyBuffer(null);
		env.onError.setOn();
		if(current!=null)
			current.clear();
	}

	public void execute(String cmd){
		execute(new Context(cmd));
	}

	public void execute(Context context){
		env.executingKeyBuffer.clear();
		nowNode=null;
		current=context;
		env.tscreen.setKeyBuffer(env.executingKeyBuffer);
		visitor = new UsoBasicVisitor(env);
		if(state!=STATE.EXECUTING){
			state=STATE.EXECUTING;
			exec();
		}
	}
	private void exec(){
		boolean cont=false;
		long start=new Date().getTime();
		for(long current=new Date().getTime();
				 (cont=executeNextChild()) && !env.tscreen.isJustAfterUpdate() &&current-start<100;
				current=new Date().getTime());
		if(!cont && state!=STATE.WAIT_INPUT)
			env.tscreen.print("",true);
		if(cont){
			new Timer() {
				public void run() {
					exec();
				}
			}.schedule(0);
		}
	}

	private boolean executeNextChild() {
		if(state!=STATE.EXECUTING) return false;
		nowNode=current.getNextNode();
		if(nowNode!=null){
			int line=nowNode.getLine();
			if(tron && line!=-1){
				env.tscreen.print("["+line+"]",false);
			}
			try{
				nowNode.jjtAccept(visitor,null);
			}catch(BasicError e){ //rte
				return handleException(e);
			}
			return true;
		}
		else{
			stopCheck();
			stop();
			return false;
		}
	}

	public ProgramContext getProgramContext(){
		BasicError.check(current instanceof ProgramContext, BasicError.ILLEGAL_DIRECT);
		return (ProgramContext)current;
	}

	public Context getContext(){
		return current;
	}

}
