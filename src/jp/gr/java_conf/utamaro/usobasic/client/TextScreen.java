package jp.gr.java_conf.utamaro.usobasic.client;


import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;

public class TextScreen extends Composite{
	private final static double VERSION=0.03;
	private final static int MAX_WIDTH=80;
	private final static int MAX_HEIGHT=25;
	private final static int UPDATE_INTERVAL=100;
	private final static int CURSOR_BLINK_INTERVAL=1000;

	private boolean isPendingUpdate=false;
	private boolean isJustAfterUpdate=false;
	private boolean isInsertMode=false;
	private Point cursor=new Point(0,0);
	private int width=80;
	private int height=25;
	private String[] functionKeys={"load \"","auto ","go to ","list ",
			"run\r","save \"","key ","print ","edit\r","cont\r"};
	private boolean isPrintFK=true;
	private int scrollStart=0;
	private int scrollEnd=23;
	private String lastCmd="";
	private boolean isDisplay=true;
	private boolean[] insertRange=new boolean[MAX_WIDTH];
	private Character[][] character=new Character[MAX_WIDTH][MAX_HEIGHT];
	private Environment env;
	private DirectModeKeyBuffer keyHandler;
	private Timer blinker=null;
	private Logger logger = Logger.getLogger("tscreen");

	private DummyInput dummyInput=new DummyInput();

	private class DummyInput extends TextBox {
		private HandlerRegistration h1= null;
		private HandlerRegistration h2=null;
		private HandlerRegistration h3=null;

		private DummyInput(){
			super();
			addStyleName("usobasic_dummy_input");
			addCompositEvent(TextScreen.this,getElement());
		}
		public void setKeyBuffer(KeyBuffer kb){
			if(h1!=null) h1.removeHandler();
			if(h2!=null) h2.removeHandler();
			if(h3!=null) h3.removeHandler();
			h1= addKeyDownHandler(kb);
			h2= addKeyPressHandler(kb);
			h3= addKeyUpHandler(kb);
		}

		private native void addCompositEvent(TextScreen tscreen,Element dummy_input) /*-{
			if(dummy_input.addEventListener){
		dummy_input.addEventListener("compositionstart",function(e){
			dummy_input.value="";
		});

		dummy_input.addEventListener("compositionend",function(e){
			tscreen.@jp.gr.java_conf.utamaro.usobasic.client.TextScreen::print(Ljava/lang/String;Z)(dummy_input.value,false);
		});

			}

	}-*/;
	}

	private class DirectModeKeyBuffer extends KeyBuffer{
		public DirectModeKeyBuffer(){
			super(env);
		}

		private String makeStringFromCurrentLine(){
			StringBuilder str=new StringBuilder();
			int y=cursor.y;
			for(y=cursor.y;y>0 && insertRange[y-1];y--);
			do{
				for(int x=0;x<width;x++){
					if(!character[x][y].isIgnore()){
						str.append(character[x][y].getChar());
					}
				}
			}while(insertRange[y++] && y<height);
			return str.toString();
		}


		private void execute(String cmd){
			try{
				cursorDown(true);
				cursor.x=0;
				if(cmd.matches("^\\s*\\d+.*$")){
					RegExp regExp = RegExp.compile("^\\s*(\\d+)\\s*(.*)$");
					MatchResult matcher = regExp.exec(cmd);
					int line=Integer.parseInt(matcher.getGroup(1));
					String str=matcher.getGroup(2);
					if(str.equals("")){
						env.program.remove(line);
					}else{
						env.program.put(line,str);
					}
				}else{
					lastCmd=cmd;
					isInsertMode=false;
					Context cont=new Context(cmd);
					env.executor.execute(cont);
				}
			}catch(BasicError e){  //syntax error
				Sound.beep();
				print(e.getMessage()+"\r",true);
			}
		}

		protected void onEnter(){}
		private void onEnter_(){
			String cmd=makeStringFromCurrentLine();
			if(inputHandler!=null) inputHandler.onEnter(cmd);
			else{
				if(!cmd.matches("^\\s*$")){
					execute(cmd.trim());
				}else print('\r');
			}
		}
		protected boolean onBreak(){
			return true;
		}
		protected boolean onHelp(){
			return true;
		}

		protected void onKey(){
			super.onKey();
			for(int i=0;i<buffer.length();i++){
				char c=buffer.charAt(i);
				switch(c){
				case 1:
					print(lastCmd,false);
					break;
				case 2:
					cursorLeft();
					while(!cursor.isOrigin() 
							&& character[cursor.x][cursor.y].isSpace())
						cursorLeft();
					while(!cursor.isOrigin() 
							&& !character[cursor.x][cursor.y].isSpace())
						cursorLeft();
					if(!cursor.isOrigin()) cursorRight();
					break;
				case 3:
					cursor.x=0;
					cursorDown(true);
					break;
				case 4: 
					while(!cursor.isLocatedAt(width-1, scrollEnd-1)
							&& !character[cursor.x][cursor.y].isSpace()){
						del();
					}
					break;
				case 5: 
					for(int x=cursor.x;x<width-1;x++){
						character[x][cursor.y].setCharacter(); 
					}
					break;
				case 6: 
					while(!cursor.isLocatedAt(width-1, scrollEnd-1)
							&& !character[cursor.x][cursor.y].isSpace())
						cursorRight();
					while(!cursor.isLocatedAt(width-1, scrollEnd-1)
							&& character[cursor.x][cursor.y].isSpace())
						cursorRight();
					break;
				case 10: 
					if(!isInsertMode){
						insertRange[cursor.y]=true;
						insertRange[cursor.y+1]=true;
					}else{
						scrollDown(cursor.y+1,scrollEnd);
						int xx=0;
						for(int x=cursor.x;x<width;x++,xx++){
							character[xx][cursor.y+1].copyFrom(
									character[x][cursor.y]); 
							character[x][cursor.y].setCharacter(); 
						}
					}
					break;
				case 13:
					onEnter_();
					break; //do not print "\r"
				case 15:
					isDisplay=!isDisplay;
					break;
				case 18:
					isInsertMode=!isInsertMode;
					break;
				case 19:
					if(env.executor.isExecuting())
						env.executor.setSuspend();
					else if(env.executor.isSuspend())
						env.executor.setExecuting();
					break;
				case 21: 
					for(int x=0;x<width-1;x++){
						character[x][cursor.y].setCharacter(); 
					}
					cursor.x=0;
					break;
				case 24: 
					for(cursor.x=width-1;cursor.x>0;cursor.x--){
						if(!character[cursor.x][cursor.y].isSpace()) break; 
					}
					cursorRight();
					break;
				case 127:
					del();
					break;
				case 248:
					scrollDown();
					break;
				case 249:
					scrollUp();
					break;
				default:
					print(c);
				}
			}
			buffer.setLength(0);
			update();
		}
	}

	public void setUpdateIsNeeded(){
		if(!isPendingUpdate){
			isPendingUpdate=true;
			new Timer() {
				public void run() {
					update();
				}
			}.schedule(UPDATE_INTERVAL);
		}
	}

	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}

	public void locate(int x,int y){
		if(x<0 || y<0 || x>width-1 ||y>height-1){
			throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
		}
		cursor.x=x;
		cursor.y=y;
	}

	public void fakeStartup(){
		isPrintFK=false;
		Character.setCursorPrint(false);
		cls();
		Sound.startup();
		for(int i=0;i<6;i++){
			final int ii=i;
			new Timer() {
				public void run() {
					locate(0,0);
					print("MEMORY "+ (ii*128) +"KB OK",false);
				}
			}.schedule(1000*ii);
		}
		final String initMessage="USO 800 BASIC(GWT-JS) version "+VERSION+"\r"+
				"Copyright (C) 2014 by USO Corporation\r";
		new Timer() {
			public void run() {
				Character.setCursorPrint(true);
				isPrintFK=true;
				cls();
				print(initMessage,true);
			}
		}.schedule(10000);
	}

	public void setFocus(boolean isFocus){
		dummyInput.setFocus(isFocus);
	}

	public void setKeyBuffer(KeyBuffer h){
		dummyInput.setKeyBuffer(h);
	}
	public void setDirectModeKeyBuffer(InputHandler h){
		setKeyBuffer(keyHandler);
		keyHandler.setInputHandler(h);
	}

	public TextScreen(Environment env){
		super();
		FlowPanel flowPanel=new FlowPanel();
		FocusPanel focusPanel=new FocusPanel();

		focusPanel.setWidget(flowPanel);
		initWidget(focusPanel);
		addStyleName("usobasic_text_screen");
		this.env=env;
		keyHandler=new DirectModeKeyBuffer();
		for(int y=0;y<MAX_HEIGHT;y++){
			insertRange[y]=false;
			for(int x=0;x<MAX_WIDTH;x++){
				character[x][y]=new Character();
				flowPanel.add(character[x][y]);
			}
			flowPanel.add(new InlineHTML("<br>"));
		}
		flowPanel.add(dummyInput);
		setDirectModeKeyBuffer(null);
		//		startBlinking();
		focusPanel.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				setFocus(true);
			}});
		setUpdateIsNeeded();
	}

	public void stopBlinking(){
		if(blinker!=null){
			blinker.cancel();
			blinker=null;
			Character.clearBlinkcount();
		}
	}

	public void startBlinking(){
		blinker=new Timer() {
			public void run() {
				Character.updateBlinkCount();
				character[cursor.x][cursor.y].update(true);
			}
		};
		blinker.scheduleRepeating(CURSOR_BLINK_INTERVAL);
	}

	private void printFunctionKey(){
		if(isInsertMode){
			character[0][height-1].setCharacter('挿');
			character[2][height-1].setCharacter('入');
		}else{
			character[0][height-1].setCharacter('上');
			character[2][height-1].setCharacter('書');

		}
		character[1][height-1].setIgnore();
		character[3][height-1].setIgnore();

		int x=4;
		for(int i=0;i<10;i++){
			for(int j=0;j<6;j++,x++){
				char c;
				if(j<functionKeys[i].length()){
					c=(char)functionKeys[i].codePointAt(j);
					if(c=='\r') c=0x240d;
				}else{
					c=' ';
				}
				character[x][height-1].setCharacter(c,Character.STATE.REVERSE);
			}
			if(i==4) x+=4;
			else x++;
		}
	}

	private void update(){
		if(isPrintFK) printFunctionKey();
		for(int x=0;x<MAX_WIDTH;x++){
			for(int y=0;y<MAX_HEIGHT;y++){
				character[x][y].update(cursor.x==x && cursor.y==y);
			}
		}
		isJustAfterUpdate=true;
		isPendingUpdate=false;
	}
	
	public boolean isJustAfterUpdate(){
		if(isJustAfterUpdate){
			isJustAfterUpdate=false;
			return true;
		}
		return false;
	}


	private void cursorDown(boolean isScroll){
		if(cursor.y<scrollEnd){
			cursor.y++;
		}else{
			if(isScroll)	scrollUp();
		}
		if(character[cursor.x][cursor.y].isIgnore()) cursorLeft();
	}

	private void  cursorRight(){
		if(cursor.x<width-1){
			cursor.x++;
		}else{
			cursor.x=0;
			cursorDown(true);
		}
		if(character[cursor.x][cursor.y].isIgnore())  cursorRight();
	}

	private void  cursorUp(){
		if(cursor.y>0) cursor.y--;
		if(character[cursor.x][cursor.y].isIgnore())  cursorLeft();
	}

	private void  cursorLeft(){
		if(cursor.x>0){
			cursor.x--;
		}else
			if(cursor.y>0){
				cursor.x=width-1;
				cursor.y--;
			}
		if(character[cursor.x][cursor.y].isIgnore())  cursorLeft();
	}

	private void scrollDown(int start,int end){
		for(int xx=0;xx<MAX_WIDTH;xx++){
			for(int yy=end;yy>start;yy--){
				character[xx][yy].copyFrom(character[xx][yy-1]);
			}
			character[xx][start].setCharacter(' ',"#ffffff",Character.STATE.NORMAL);
		}
		for(int yy=end-1;yy>=start;yy--){
			insertRange[yy]=insertRange[yy-1];
		}
	}

	private void scrollUp(int start,int end){
		for(int xx=0;xx<MAX_WIDTH;xx++){
			for(int yy=start;yy<end;yy++){
				character[xx][yy].copyFrom(character[xx][yy+1]);
			}
			character[xx][end].setCharacter(' ',"#ffffff",Character.STATE.NORMAL);
		}
		for(int yy=start;yy<=end;yy++){
			insertRange[yy]=insertRange[yy+1];
		}
	}
	public void scrollUp(){
		scrollUp(scrollStart,scrollEnd);
	}
	public void scrollDown(){
		scrollDown(scrollStart,scrollEnd);
	}

	public void cls(){
		for(int xx=0;xx<width;xx++){
			for(int yy=scrollStart;yy<=scrollEnd;yy++){
				character[xx][yy].setCharacter();
			}
		}
		for(int i=0;i<MAX_HEIGHT;i++){
			insertRange[i]=false;
		}
		cursor.toOrigin();
		update();
	}

	private void del(){
		int yy=0;
		do{
			int startx=(yy==0?cursor.x:0);
			for(int xx=startx;xx<width-1;xx++){
				character[xx][cursor.y+yy].copyFrom(
						character[xx+1][cursor.y+yy]);
			}
			if(insertRange[cursor.y+yy+1]){
				character[width-1][cursor.y+yy].copyFrom(
						character[0][cursor.y+yy+1]);
			}
			else{
				character[width-1][cursor.y+yy].setCharacter();
			}
			yy++;
		}while(insertRange[cursor.y+yy]);
		if(character[cursor.x][cursor.y].isIgnore()) del();
	}

	private boolean writeControl(char ch){
		switch(ch){
		case 7:
			Sound.beep();
			return true;
		case 8:
			cursorLeft();
			del();
			return true;
		case 9:
			enterTab();
			return true;
		case 10:
			cursorDown(true);
			return true;
		case 11:
			cursor.x=cursor.y=0;
			return true;
		case 12:
			cls();
			return true;
		case 13:
			cursorDown(true);
			cursor.x=0;
			return true;
		case 28:
			cursorRight();
			return true;
		case 29:
			cursorLeft();
			return true;
		case 30:
			cursorUp();
			return true;
		case 31:
			cursorDown(false);
			return true;

		}
		return false;
	}

	private void enterTab(){
		int start_x=cursor.x;
		for(int xx=0;xx<8-(start_x % 8);xx++){
			if(cursor.x<width-1){
				print(' ');
			}
		}
	}

	public void print(char ch){
		if(!writeControl(ch)){
			if(!isInsertMode){
				character[cursor.x][cursor.y].setCharacter(ch);
				if(cursor.x+1<width && character[cursor.x+1][cursor.y].isIgnore())
					character[cursor.x+1][cursor.y].setCharacter();
				cursorRight();
				if(cursor.x==0){
					insertRange[cursor.y-1]=true;
					insertRange[cursor.y]=true;
				}
				if(cursor.x!=0 && ch!=Character.IGNORE_CODE 
						&& !Literal.isHankaku(ch))
					print(Character.IGNORE_CODE);
			} else{
				insert(ch);
				cursorRight();
				if(ch!=Character.IGNORE_CODE && !Literal.isHankaku(ch)){
					insert(Character.IGNORE_CODE);
					cursorRight();
				}
			}
		}
		setUpdateIsNeeded();
	}


	private void insert(char c){
		int yy=0;
		do{
			char last=character[width-1][cursor.y+yy].getCharacter();
			int start_x=(yy==0? cursor.x:0);
			for(int xx=MAX_WIDTH-1;xx>start_x;xx--){
				character[xx][cursor.y+yy].copyFrom(
						character[xx-1][cursor.y+yy]);
			}
			character[start_x][cursor.y+yy].setCharacter(c);
			c=last;
			yy++;
		}while(insertRange[cursor.y+yy]);

		if(c!=' '){
			boolean exist=false;
			for(int xx=0;xx<MAX_WIDTH-1;xx++){
				if(!character[xx][cursor.y+yy].isSpace()) exist=true;
			}
			if(exist){
				scrollUp(cursor.y+yy,scrollEnd);
			}
			character[0][cursor.y+yy].setCharacter(c);
			insertRange[cursor.y]=true;
			insertRange[cursor.y+yy]=true;
		}
	}


	public void  print(String msg,boolean isOKPrint){
		if(!isDisplay) return;
		if(isOKPrint){
			msg+="Ok\r";
		}
		for(int j=0;j<msg.length();j++){
			print((char)msg.charAt(j));
		}
	}

	public void colorAt(Point xy,int c){
		for(int x=0;x<=xy.x;x++){
			for(int y=0;x<=xy.y;y++){
				character[x][y].setFunction(c);
			}
		}
	}
	public void setScrollStart(int start){
		BasicError.check(scrollStart>=0 && scrollStart<height,BasicError.ILLEGAL_FUNCTION_CALL);
		scrollStart=start;
		if(cursor.y<scrollStart) cursor.y=scrollStart;
	}
	public void setScrollCount(int count){
		int scrollEnd_=scrollStart+count-1;
		BasicError.check(scrollEnd_>=0 && scrollEnd_<height,BasicError.ILLEGAL_FUNCTION_CALL);
		scrollEnd=scrollEnd_;
		if(cursor.y>scrollEnd) cursor.y=scrollEnd;
	}
	public void setDisplayCusror(boolean disp){
		isDisplay=disp;
	}
	public Point getCursor(){
		return cursor;
	}
	public void setKey(int k,String str){
		functionKeys[k]=str;
	}
	public String getFKey(int num){
		return functionKeys[num];
	}
	public void setPrintFK(boolean print){
		isPrintFK=print;
		cls();
	}
	public void listKey(){
		for(String k:functionKeys){
			if(k.endsWith("\r"))	print(k,false);
			else print(k+"\r",false);
		}
	}
}
