package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.InlineHTML;

public class Character extends InlineHTML { 
	public enum STATE {
		NORMAL,
		SECRET,
		BLINK,
		REVERSE,
		REVERSE_SECRET,
		REVERSE_BLINK
	}
	private static String[] COLOR={"#000000","#0000ff","#ff0000","#ff00ff"
		,"#00ff00","#00ffff","#ffff00","#ffffff"};

	public  final static char IGNORE_CODE=0xffff;
	private static STATE default_state=STATE.NORMAL;
	private static boolean isCursorPrint=true;
	private static String default_color=COLOR[7];

	private static int blinkCount=0;
	private STATE state=STATE.NORMAL;
	private char c=' ';
	private boolean isDirty=true;
	private String color=COLOR[7];
	private Logger logger = Logger.getLogger("char");

	public static void clearBlinkcount(){
		blinkCount=0;
	}
	
	public static void updateBlinkCount(){
		if(++blinkCount>4) blinkCount=0;
	}


	public Character(char c){
		this.c=c;
	}

	public static void setCursorPrint(boolean p){
		isCursorPrint=p;
	}


	public static void setDefaultColor(int c){
		if(c>COLOR.length){
			throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
		}
		default_color=COLOR[c];
	}
	public static void setDefaultFunction(int s){
		default_color=COLOR[s];
	}
	public void setFunction(int s){
		color=COLOR[s];
	}
	public static void setDefaultState(STATE s){
		default_state=s;
	}
	public static STATE getDefaultState(){
		return default_state;
	}

	public boolean isSpace(){
		return c==' ';
	}

	public Character(){
		super();
	}
	public char getChar(){
		return c;
	}
	public boolean copyFrom(Character cf){
		if(cf.color==color &&	cf.state==state &&	cf.c==c){
			return false;
		}
		color=cf.color;
		state=cf.state;
		c=cf.c;
		isDirty=true;
		return true;
	}

	public boolean setIgnore(){
		return setCharacter(IGNORE_CODE);
	}
	public boolean isIgnore(){
		return c==IGNORE_CODE;
	}
	public char getCharacter(){
		return c;
	}

	public boolean setCharacter(){
		return setCharacter(' ');
	}

	public boolean setCharacter(char cc,String color,STATE s){
		if(!this.color.equals(color) ||	state!=s ||	c!=cc){
			this.color=color;
			state=s;
			c=cc;
			isDirty=true;
			return true;
		}
		return false;
	}


	public boolean setCharacter(char cc,STATE s){
		return setCharacter(cc,default_color,s);
	}
	public boolean setCharacter(char cc){
		return setCharacter(cc,default_color,default_state);
	}
	public boolean update(boolean isCursor){
		if(!isCursor && !isDirty) return false;
		if(isCursor) isDirty=true;
		else isDirty=false;
		if(isCursorPrint && blinkCount%2==0 && isCursor){
			updateWithCursor();
		}else{
			updateWithoutCusor();
		}
		String write;
		if(c==IGNORE_CODE) write="";
		else 
			if(c==' ') write="&nbsp;";
			else{
				if(c=='\\') write="¥";
				else write=""+c;
			}
		setHTML(write);
		return true;
	}

	private void updateWithoutCusor(){
		Style style=getElement().getStyle();
		switch(state){
		case NORMAL:
			style.setBackgroundColor("transparent");
			style.setColor(color);
			break;
		case SECRET:
			style.setBackgroundColor("transparent");
			style.setColor("transparent");
			break;
		case BLINK:
			isDirty=true;
			style.setBackgroundColor("transparent");
			if(blinkCount%4==0) style.setColor("transparent");
			else style.setColor(color);
			break;
		case REVERSE:
			style.setBackgroundColor(color);
			style.setColor("#000000");
			break;
		case REVERSE_SECRET:
			style.setBackgroundColor(color);
			style.setColor(color);
			break;
		case REVERSE_BLINK:
			isDirty=true;
			style.setBackgroundColor(color);
			if(blinkCount%4==0) style.setColor(color);
			else style.setColor("transparent");
			break;
		}
	}

	private void updateWithCursor(){
		Style style=getElement().getStyle();
		switch(state){
		case NORMAL:
			style.setBackgroundColor(color);
			style.setColor("#000000");
			break;
		case SECRET:
			style.setBackgroundColor(color);
			style.setColor("#000000");
			break;
		case BLINK:
			isDirty=true;
			style.setBackgroundColor(color);
			if(blinkCount%4==0) style.setColor(color);
			else style.setColor("#000000");
			break;
		case REVERSE:
			style.setBackgroundColor("#000000");
			style.setColor(color);
			break;
		case REVERSE_SECRET:
			style.setBackgroundColor("#000000");
			style.setColor(color);
			break;
		case REVERSE_BLINK:
			isDirty=true;
			style.setBackgroundColor("#000000");
			if(blinkCount%4==0) style.setColor("#000000");
			else style.setColor(color);
			break;
		}
	}

}
