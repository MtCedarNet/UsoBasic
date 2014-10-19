package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTlineNumber;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatements;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Node;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ParseException;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Token;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProgramBuffer {
	private DialogBox box=new DialogBox(false,false);
	private TextArea textArea = new TextArea();
	private Button close= new Button("close");
	private Button run= new Button("run");
	private Button cls= new Button("cls");
	private Button renum= new Button("renum");
	private TreeMap<Integer,String> program= new TreeMap<>();

	private Logger logger = Logger.getLogger("progbuf");

	private int skipSpace(int loc,String str){
		for(char c=str.charAt(loc);c==' ' && c=='\t';c=str.charAt(++loc));
		return loc;
	}

	private void searchLineNumber(Node n,ArrayList<Token> t){
		for(int i=0;i<n.jjtGetNumChildren();i++){
			InterpreterNode nn=(InterpreterNode)n.jjtGetChild(i);
			if(nn instanceof ASTlineNumber){
				t.add(nn.getToken());
			}else{
				searchLineNumber(nn,t);
			}
		}
	}


	public void renum(int from,int old,int step){
		int current=from;
		TreeMap<Integer,String> newProgram= new TreeMap<>();
		HashMap<Integer,Integer> oldNew=new HashMap<>();
		for(Map.Entry<Integer, String> kv:program.entrySet()){
			int lineNumber=kv.getKey();
			if(lineNumber>=old){
				oldNew.put(lineNumber,current);
				current+=step;
			}
		}
		for(Map.Entry<Integer, String> kv:program.entrySet()){
			int lineNumber=kv.getKey();
			String stm=kv.getValue();
			Integer newLine=oldNew.get(lineNumber);
			if(newLine==null){
				newLine=lineNumber;
			}
			try{
				UsoBasicParser parser=new UsoBasicParser(stm);
				ASTstatements s=parser.statements();
				ArrayList<Token>t=new ArrayList<>();
				searchLineNumber(s,t);
				for(int i=t.size()-1;i>=0;i--){
					Token tt=t.get(i);
					if(tt!=null){
						int nl=oldNew.get(Integer.parseInt(tt.image));
						stm=stm.substring(0,tt.beginColumn-1)+nl+stm.substring(tt.endColumn,stm.length());
					}
				}
			}catch(ParseException exp){
				throw new BasicError(BasicError.SYNTAX_ERROR,lineNumber);
			}
			newProgram.put(newLine, stm);
		}
		program=newProgram;
		copyBufferToEditor();
	}

	public int getLastLine(){
		if(program.size()>0)
			return program.lastKey();
		return -1;
	}
	private void copyBufferToEditor(){
		StringBuilder buf=new StringBuilder();
		for(Map.Entry<Integer, String> kv:program.entrySet()){
			int lineNumber=kv.getKey();
			String cmd=kv.getValue();
			buf.append(lineNumber+" "+cmd+"\n");

		}
		textArea.setText(buf.toString());
	}
	private void copyEditorToBuffer(){
		program.clear();
		String[] strs=textArea.getText().split("\n");
		RegExp regExp = RegExp.compile("^\\s*(\\d+)\\s*(.+)$");
		int last=-1;
		for(String str:strs){
			if(str.matches("^\\s*$")) continue;
			MatchResult matcher = regExp.exec(str);
			BasicError.check(matcher.getGroupCount()!=0,BasicError.DIRECT_STATEMENT_IN_FILE);
			int lineNumber=Integer.parseInt(matcher.getGroup(1));
			if(last>lineNumber){
				throw new BasicError(BasicError.LINE_NUMBER_IS_ILLEGAL,lineNumber);
			}
			String cmd=matcher.getGroup(2);
			program.put(lineNumber, cmd);
			last=lineNumber;
		}
	}
	public String getAll(){
		return textArea.getText();
	}
	public void put(int line,String cmd){
		program.put(line,cmd);
		copyBufferToEditor();
	}
	public void clear(){
		program.clear();
		copyBufferToEditor();
	}
	public void remove(int i){
		String result=program.get(i);
		BasicError.check(result!=null,BasicError.UNDEFINED_LINE_NUMBER);
		program.remove(i);
		copyBufferToEditor();
	}
	public String get(int i){
		return program.get(i);
	}
	public SortedMap<Integer, String> tailMap(int i){
		return program.tailMap(i);
	}

	public void set(String prog){
		textArea.setText(prog);
		copyEditorToBuffer();
	}

	public ProgramBuffer(final Environment env){
		box.setText("edit");
		close.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				box.hide();
			}
		});
		HorizontalPanel hpanel = new HorizontalPanel();
		run.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				copyEditorToBuffer();
				env.executor.execute("run");
			}
		});
		cls.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				env.executor.execute("cls 3");
			}
		});
		renum.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				copyEditorToBuffer();
				env.executor.execute("renum 1000");
			}
		});
		hpanel.add(run);
		hpanel.add(cls);
		hpanel.add(close);
		hpanel.add(renum);
		VerticalPanel vpanel = new VerticalPanel();
		vpanel.add(hpanel);
		textArea.addKeyDownHandler(new KeyDownHandler(){
			@Override
			public void onKeyDown(KeyDownEvent e) {
				if(e.getNativeKeyCode()==KeyCodes.KEY_ENTER){
					copyEditorToBuffer();
				}
			}
		});
		vpanel.add(textArea);
		box.setWidget(vpanel);
	}

	public void showEditor(){
		copyBufferToEditor();
		box.show();
	}
}
