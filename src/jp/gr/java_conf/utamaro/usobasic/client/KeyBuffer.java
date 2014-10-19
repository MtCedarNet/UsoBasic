package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

public class KeyBuffer implements KeyDownHandler, KeyPressHandler,KeyUpHandler{
	private class KeyFlag{
		private int[] flag=new int[15];
		public KeyFlag(){
			clear();
		}
		public void clear(){
			for(int i=0;i<flag.length;i++) flag[i]=0xff;
		}
		public void setBit(int port,int bit,boolean isSet){
			if(isSet) flag[port]=flag[port]|(1<<bit);
			else flag[port]=flag[port]&(~(1<<bit));
		}
	}

	protected StringBuilder buffer=new StringBuilder();
	private Environment env=null;
	protected InputHandler inputHandler=null;
	private Logger logger = Logger.getLogger("keybuf");
	private KeyFlag flag=new KeyFlag();

	public int getFlag(int port){
		return flag.flag[port-0xe0];
	}
	public KeyBuffer(Environment env){
		this.env=env;
	}

	public void setInputHandler(InputHandler h){
		inputHandler=h;
	}

	public void clear(){
		flag.clear();
		buffer.setLength(0);
	}

	protected void onKey(){
		if(inputHandler!=null)
			inputHandler.onKey(buffer.toString());
	}
	protected void onEnter(){
		if(inputHandler!=null)
			inputHandler.onEnter(buffer.toString());
	}
	public String getBuffer(){
		return buffer.toString();
	}

	@Override
	public void onKeyPress(KeyPressEvent e) {
		e.preventDefault();
		char c=(char)e.getCharCode();
		if( !e.isAltKeyDown() && !e.isControlKeyDown() 
				&& !e.isMetaKeyDown() && ' '<=c && c<='~'){
			buffer.append(c);
			onKey();
		}
	}

	private int controlKeyDown(int code){
		char c=(char)(code- 'A'+1);
		switch(c){
		case 1:
			return KeyCodes.KEY_END;
		case 3:
			return KeyCodes.KEY_PAUSE;
		case 8:
			return KeyCodes.KEY_BACKSPACE;
		case 9:
			return KeyCodes.KEY_TAB;
		default:
			if(c>0 && code!=KeyCodes.KEY_CTRL){
				buffer.append((char)c);
			}
			return -1;
		}
	}
	protected boolean onHelp(){
		return !env.onHelp.trigger();
	}
	protected boolean onBreak(){
		if(!env.onStop.trigger()){
			throw new BasicError(BasicError.BREAK);
		}else{
			return true;
		}
	}

	private void setFlag(int code,boolean isSet){
		int port=-1;
		int bit=-1;
		if( KeyCodes.KEY_NUM_ZERO <=code && code<= KeyCodes.KEY_NUM_SEVEN){
			port=0;
			bit=code-KeyCodes.KEY_NUM_ZERO;
		}
		if( KeyCodes.KEY_NUM_EIGHT<= code && code<= KeyCodes.KEY_NUM_NINE){
			port=1;
			bit=code-KeyCodes.KEY_NUM_EIGHT;
		}

		if( KeyCodes.KEY_A<= code && code<= KeyCodes.KEY_G){
			port=2;
			bit=code-KeyCodes.KEY_A+1;
		}
		if( KeyCodes.KEY_H<= code && code<= KeyCodes.KEY_O){
			port=3;
			bit=code-KeyCodes.KEY_H;
		}
		if( KeyCodes.KEY_P<= code && code<= KeyCodes.KEY_W){
			port=4;
			bit=code-KeyCodes.KEY_P;
		}
		if( KeyCodes.KEY_X<= code && code<= KeyCodes.KEY_Z){
			port=5;
			bit=code-KeyCodes.KEY_X;
		}
		if( KeyCodes.KEY_ZERO <=code && code<= KeyCodes.KEY_SEVEN){
			port=6;
			bit=code-KeyCodes.KEY_ZERO;
		}
		if( KeyCodes.KEY_EIGHT<= code && code<= KeyCodes.KEY_NINE){
			port=7;
			bit=code-KeyCodes.KEY_EIGHT;
		}
		if( KeyCodes.KEY_F1 <= code && code<= KeyCodes.KEY_F5){
			port=9;
			bit=code-KeyCodes.KEY_F1;
		}
		if( KeyCodes.KEY_F6 <= code && code<= KeyCodes.KEY_F10){
			port=12;
			bit=code-KeyCodes.KEY_F6+2;
		}
		switch(code){
		case KeyCodes.KEY_NUM_MULTIPLY:
			port=1;
			bit=2;
			break;
		case KeyCodes.KEY_NUM_PLUS:
			port=1;
			bit=3;
			break;
		case KeyCodes.KEY_NUM_PERIOD:
			port=1;
			bit=6;
			break;
		case KeyCodes.KEY_ENTER:
			port=1;
			bit=7;
			break;
		case 192: //@
			port=2;
			bit=0;
			break;
		case 219: //[
			port=5;
			bit=3;
			break;
		case 226: // \
			port=5;
			bit=4;
			break;
		case 221: //]
			port=5;
			bit=5;
			break;
		case 222: //^
			port=5;
			bit=6;
			break;
		case 189: //-
			port=5;
			bit=7;
			break;
		case 186: //:
		case 59: 
			port=7;
			bit=2;
			break;
		case 187: //;
		case 61: 
			port=7;
			bit=3;
			break;
		case 188: //,
			port=7;
			bit=4;
			break;
		case 190: //.
			port=7;
			bit=6;
			break;
		case 191: // /
			port=7;
			bit=7;
			break;
		case KeyCodes.KEY_HOME:
			port=8;
			bit=0;
			break;
		case KeyCodes.KEY_UP:
			port=8;
			bit=1;
			break;
		case KeyCodes.KEY_RIGHT:
			port=8;
			bit=2;
			break;
		case KeyCodes.KEY_DELETE:
			port=8;
			bit=3;
			break;
		case KeyCodes.KEY_ALT:
			port=8;
			bit=4;
			break;
		case KeyCodes.KEY_SHIFT:
			port=8;
			bit=6;
			break;
		case KeyCodes.KEY_CTRL:
			port=8;
			bit=7;
			break;
		case KeyCodes.KEY_SPACE:
			port=9;
			bit=6;
			break;
		case KeyCodes.KEY_ESCAPE:
			port=9;
			bit=7;
			break;
		case KeyCodes.KEY_TAB:
			port=10;
			bit=0;
			break;
		case KeyCodes.KEY_DOWN:
			port=10;
			bit=1;
			break;
		case KeyCodes.KEY_LEFT:
			port=10;
			bit=2;
			break;
		case KeyCodes.KEY_NUM_MINUS:
			port=10;
			bit=5;
			break;
		case KeyCodes.KEY_NUM_DIVISION:
			port=10;
			bit=6;
			break;
		case KeyCodes.KEY_CAPS_LOCK:
			port=10;
			bit=7;
			break;
		case KeyCodes.KEY_PAGEUP:
			port=11;
			bit=0;
			break;
		case KeyCodes.KEY_PAGEDOWN:
			port=11;
			bit=1;
			break;
		case KeyCodes.KEY_BACKSPACE:
			port=12;
			bit=0;
			break;
		case KeyCodes.KEY_INSERT:
			port=12;
			bit=7;
			break;
		}
		if(port>=0 && bit>=0){
			flag.setBit(port, bit,isSet);
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent e) {
		int code=e.getNativeKeyCode();
		setFlag(code,false);
		int n=code-KeyCodes.KEY_F1;
		boolean processed=false;
		if( 0<=n  && n<=9){
			processed=true;
			if(!env.onKey[n].trigger()){
				buffer.append(env.tscreen.getFKey(n));
			}
		}

		if(e.isControlKeyDown()){
			int code_=controlKeyDown(code);
			if(code_>0) code=code_;
			processed=true;
		}

		switch(code){
		case KeyCodes.KEY_UP:
			processed=true;
			buffer.append((char)30);
			break;
		case KeyCodes.KEY_DOWN:
			processed=true;
			buffer.append((char)31);
			break;
		case KeyCodes.KEY_LEFT:
			processed=true;
			buffer.append((char)29);
			break;
		case KeyCodes.KEY_RIGHT:
			processed=true;
			buffer.append((char)28);
			break;
		case KeyCodes.KEY_DELETE:
			processed=true;
			buffer.append((char)127);
			break;
		case KeyCodes.KEY_TAB:
			processed=true;
			buffer.append((char)9);
			break;
		case KeyCodes.KEY_BACKSPACE:
			processed=true;
			buffer.append((char)8);
			break;
		case KeyCodes.KEY_PAUSE:
			processed=true;
			if(onBreak()){
				buffer.append((char)3);
			}
			break;
		case KeyCodes.KEY_PAGEDOWN:
			processed=true;
			buffer.append((char)248);
			break;
		case KeyCodes.KEY_ENTER:
			processed=true;
			buffer.append((char)13);
			onEnter();
			break;
		case KeyCodes.KEY_PAGEUP:
			processed=true;
			buffer.append((char)249);
			break;
		case KeyCodes.KEY_HOME:
			processed=true;
			buffer.append((char)13);
			break;
		case KeyCodes.KEY_END:
			processed=true;
			if(onHelp())
				buffer.append((char)1);
			break;
		case KeyCodes.KEY_INSERT:
			processed=true;
			buffer.append((char)18);
			break;
		}
		if(processed){
			e.preventDefault();
			onKey();
		}
	}

	@Override
	public void onKeyUp(KeyUpEvent e) {
		int code=e.getNativeKeyCode();
		setFlag(code,true);

	}
}
