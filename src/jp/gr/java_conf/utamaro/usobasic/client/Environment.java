package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.FileSystem.File;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTexpression;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Token;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParserConstants;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParserVisitor;


import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;


public class Environment {

	private class Deffn{
		public ASTexpression expression;
		public List<VariableParam> params;

		public Deffn(ASTexpression expression,List<VariableParam> params){
			this.expression=expression;
			this.params=params;
		}
	}

	private class Field{
		public VariableParam variable;
		public int length;
		public Field(VariableParam variable,int length){
			this.variable=variable;
			this.length=length;
		}
	}

	public TextScreen tscreen=new TextScreen(this);
	public  ProgramBuffer program= new ProgramBuffer(this);
	public  ErrorInterrupt onError=new ErrorInterrupt();
	public  Interrupt[] onKey=new Interrupt[12];
	public  Interrupt onHelp=new Interrupt();
	public  Interrupt onPen=new Interrupt();
	public  Interrupt onStop=new Interrupt();
	public  Interrupt onTime=new Interrupt();
	public KeyBuffer executingKeyBuffer=new KeyBuffer(this);
	public FileSystem fileSystem=FileSystem.getFileSystem();
	public Executor executor=new Executor(this);

	private GraphicScreen[] gscreen=
			new GraphicScreen[GraphicScreen.CANVAS_NUMBER+1];
	private static HashMap<String,Deffn> deffns= new HashMap<>();
	private int activeScreen=0;
	private ArrayList<Field> field= new ArrayList<>();
	private HashMap<String,Literal> variable = new HashMap<>();
	private HashMap<String,VArray> array= new HashMap<>();
	private FileSystem.File[] files=new FileSystem.File[255];
	private Sound beep=new Sound();
	private ArrayList<String> common= new ArrayList<>();
	private Logger logger =Logger.getLogger("environment");
	private Literal inputResult=null;

	private static Environment env=null;

	public Literal getLiteral(VariableParam vparam){
		String var=vparam.getName();
		Literal l=null;
		if(var.startsWith("fn")){
			l=evalDeffn(vparam);
		}else{
			if(vparam.isArray()){
				VArray v=array.get(var);
				if(v==null){
					v=makeDefaultArray(var,vparam.getIndex());
				}else{
					l=v.get(vparam.getIndex());
				}
			}else{
				l=variable.get(var);
			}
			if(l==null){
				l=vparam.defaultLiteral();
			}
		}
		return l;
	}
	private void checkSettable(Literal.TYPE t1,Literal.TYPE t2){
		boolean ng=(t1==Literal.TYPE.STRING && t2!=Literal.TYPE.STRING) ||
				(t1!=Literal.TYPE.STRING && t2==Literal.TYPE.STRING);
		BasicError.check(!ng, BasicError.TYPE_MISMATCH);
	}

	public void setToVariable(VariableParam vparam,Literal l){
		String var=vparam.getName();
		if(var.startsWith("fn")) new BasicError(BasicError.SYNTAX_ERROR);
		checkSettable(vparam.getType(),l.getType());
		l.changeType(vparam.getType());
		if(vparam.isArray()){
			VArray v=array.get(var);
			if(v==null){
				v=makeDefaultArray(var,vparam.getIndex());
			}
			v.set(vparam.getIndex(), l);
		}else{
			variable.put(var,l);
		}
	}

	public void setToVariable(VariableParam vparam,double n){
		BasicError.check(vparam.getType()!=Literal.TYPE.STRING,BasicError.TYPE_MISMATCH);
		setToVariable(vparam,new Literal(n,vparam.getType()));
	}
	public void setToVariable(VariableParam vparam,String str){
		BasicError.check(vparam.getType()==Literal.TYPE.STRING,BasicError.TYPE_MISMATCH);
		setToVariable(vparam,new Literal(str,vparam.getType()));
	}

	private Environment(){
		for(int i=0;i<onKey.length;i++) onKey[i]=new Interrupt();
	}

	private void setupScreen(){
		RootPanel root=RootPanel.get("usobasic_root");
		root.add(tscreen,0,0);
		for(int i=0;i<GraphicScreen.CANVAS_NUMBER+1;i++){
			gscreen[i]=new GraphicScreen();
			Canvas c=gscreen[i].getCanvas();
			c.addStyleName("usobasic_graphic_screen");
			c.getElement().getStyle().setZIndex(7-i);
			c.getElement().getStyle().setVerticalAlign(VerticalAlign.BOTTOM);
			root.add(c,0,0);
		}
		tscreen.setFocus(true);
		screen(0,1);
	}

	public void screen(int active,int display){
		if(active>=0) activeScreen=active;
		if(display%16==0){
			gscreen[0].hide();
			gscreen[1].hide();
		}
		if(display==1){
			gscreen[0].show();
			gscreen[1].hide();
		}
		if(display==33){
			gscreen[0].hide();
			gscreen[1].show();
		}
	}

	public GraphicScreen getActiveScreen(){
		return gscreen[activeScreen];
	}


	public static Environment getEnvironment(){
		if(env==null){
			env=new Environment();
			env.setupScreen();
		}
		return env;
	}

	public void beep(int no){
		if(no==0) beep.off();
		else beep.on(true);
	}
	public void beep(){
		Sound.beep();
	}
	private void loadInternal(String filename){
		FileSystem.File f=fileSystem.searchFile(filename);
		BasicError.check(f!=null, BasicError.FILE_NOT_FOUND);
		for(String str:f.getContent().split("\n")){
			RegExp regExp = RegExp.compile("^\\s*(\\d+)(.+)$");
			MatchResult matcher = regExp.exec(str);
			BasicError.check(matcher==null,
					BasicError.DIRECT_STATEMENT_IN_FILE);
			program.put(Integer.parseInt(matcher.getGroup(1)),
					matcher.getGroup(2));
		}
	}
	public void load(String filename){
		program.clear();
		loadInternal(filename);
		variable.clear();
		array.clear();
	}
	public void chain(String filename,int from,boolean isAll){
		loadInternal(filename);
		if(!isAll){
			variable.clear();
			array.clear();
		}
		//run
	}
	public void clear(){
		variable.clear();
		array.clear();
		VariableParam.initDeftype();
		deffns.clear();
	}
	public void closeAll(){
		for(int i=0;i<files.length;i++){
			if(files[i]!=null) files[i]=null;
		}
	}
	public void close(int f){
		BasicError.check(files[f]==null,BasicError.BAD_FILE_NUMBER);
		files[f]=null;
	}
	public boolean eof(int f){
		BasicError.check(files[f]==null,BasicError.BAD_FILE_NUMBER);
		return files[f].eof();
	}
	public void common(String[] com){
		for(String c:com){
			common.add(c);
		}
	}

	public void delete(int from,int to){
		for(int i=from;i<=to;i++){
			program.remove(i);
		}
	}
	public VArray dim(VariableParam vparam){
		String ary=vparam.getName();
		BasicError.check(array.get(ary)==null,
				BasicError.DUPULICATE_DEFINITION); 
		VArray varray=new VArray(vparam.getIndex());
		array.put(ary,varray);
		return varray;
	}
	public void erase(VariableParam ary){
		array.remove(ary.getName());
	}

	private VArray makeDefaultArray(String ary,ArrayList<Integer>  index){
		BasicError.check(index.size()==1 && index.get(0)<10,
				BasicError.SUBSCRIPT_OUT_OF_RANGE);
		VArray v=new VArray();
		array.put(ary,v);
		return v;
	}





	public void setDeffn(ArrayList<VariableParam> vparam,ASTexpression expression){
		Deffn deffn=new Deffn(expression,vparam.subList(1, vparam.size()));
		deffns.put("fn"+vparam.get(0).getName(), deffn);
	}
	public Literal evalDeffn(VariableParam vparam){
		Deffn fn=deffns.get(vparam.getName());
		BasicError.check(fn!=null, BasicError.SYNTAX_ERROR);

		HashMap<String,Literal> var = new HashMap<>(variable);

		ArrayList<Literal>params=vparam.getArguments();
		if(params!=null){
			for(int i=0;i<Math.min(params.size(), fn.params.size());i++){
				variable.put(fn.params.get(i).getName(), params.get(i));
			}
		}
		UsoBasicVisitor visitor = new UsoBasicVisitor(env);
		Literal l=(Literal)fn.expression.jjtAccept(visitor,null);
		variable=var;
		return l;
	}
	public void setField(int fno,ArrayList<VariableParam> var,ArrayList<Integer> size){
		for(int i=0;i<Math.min(var.size(), size.size());i++){
			Field f=new Field(var.get(i),size.get(i));
			field.add(f);
		}
	}
	public void lset(String var,String val){
		for(Field f:field){
			if(f.variable.equals(val)){
				StringBuilder buf=new StringBuilder();
				buf.append(val);
				for(int i=0;i<f.length-val.length();i++) buf.append(" ");
				variable.put(var, new Literal(buf.toString(),
						Literal.TYPE.STRING));
				return ;
			}
		}
	}
	public void rset(String var,String val){
		for(Field f:field){
			if(f.variable.equals(val)){
				StringBuilder buf=new StringBuilder();
				for(int i=0;i<f.length-val.length();i++) buf.append(" ");
				buf.append(val);
				variable.put(var, new Literal(buf.toString(),
						Literal.TYPE.STRING));
				return ;
			}
		}
	}
	public String print(ArrayList<Literal> ls,ArrayList<Token> ts){
		StringBuilder buf=new StringBuilder();
		boolean isLast=false;
		for(int i=0;i<ls.size();i++){
			Literal l=ls.get(i);
			String val=null;
			if(ls.get(i).isEmpty()) continue;
			if(l.getType()!=Literal.TYPE.STRING && l.getInteger()>=0) val=" "+l.convertToString(); 
			else val=l.convertToString();
			buf.append(val);
			if(i<ts.size()){
				Token t=ts.get(i);
				if(t.kind==UsoBasicParserConstants.COMMA){
					for(int j=0;j<14-val.length();j++) buf.append(" ");
				}
				isLast=true;
			}else	isLast=false;
		}
		if(!isLast) buf.append("\r");
		return buf.toString();
	}

	private int getNormalChar(String str,int loc){
		char c=str.charAt(loc);
		if(c=='_') return loc+1; 
		if(c!='!' && c!='&' && c!='@' && c!='#' && c!='.' &&
				c!='+' && c!='-' && c!=',' && c!='^') return loc;
		return -1;
	}

	private int parseFormat(String sformat,int from,Literal l,StringBuilder result){
		StringBuilder format=new StringBuilder();
		boolean isIgnore=false;
		int spc=0;

		boolean isAnd=false;
		int i=0;
		boolean finished=false;
		for(i=from;i<sformat.length()&&!finished;i++){
			char c=sformat.charAt(i);
			if(isIgnore){
				result.append(c);
				isIgnore=false;
				continue;
			}
			switch(c){
			case '@':
				BasicError.check(l.getType()==Literal.TYPE.STRING,
				BasicError.TYPE_MISMATCH);
				result.append(l.getString());
				finished=true;
				break;
			case '!':
				BasicError.check(l.getType()==Literal.TYPE.STRING,
				BasicError.TYPE_MISMATCH);
				result.append(l.getString().charAt(0));
				finished=true;
				break;
			case '&':
				BasicError.check(l.getType()==Literal.TYPE.STRING,
				BasicError.TYPE_MISMATCH);
				if(!isAnd) isAnd=true;
				else{
					String str=l.getString().substring(0,spc+2);
					result.append(str);
					finished=true;
				}
				break;
			case ' ':
				if(isAnd) spc++;
				else result.append(c);
				break;
			case '#':
				c='0';
			case '.':
			case ',':
			case '+':
			case '-':
				format.append(c);
				char cc=sformat.charAt(i+1);
				if(i==sformat.length()-1|| (cc!='#' && cc!='.' && cc!=',')){
					try{
						String formatted = NumberFormat.getFormat(
								format.toString()).format(l.getDouble());
						StringBuilder buf=new StringBuilder();
						boolean isFirst=true;
						for(int j=0;j<formatted.length();j++){
							c=formatted.charAt(j);
							if(isFirst && c=='0'){
								buf.append(" ");
							}
							else{
								buf.append(c);
								isFirst=false;
							}

						}
						result.append(buf);
					}catch(IllegalArgumentException  e){
						throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
					}
					finished=true;
				}
				break;
			case '_':
				isIgnore=true;
				break;
			case '^':
				if(i+3<sformat.length() && format.charAt(i+1)=='^' && 
				format.charAt(i+2)=='^' && format.charAt(i+3)=='^'){
					format.append("E00");
					try{
						String formatted = NumberFormat.getFormat(
								format.toString()).format(l.getDouble());
						result.append(formatted);
					}catch(IllegalArgumentException  e){
						throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
					}
					finished=true;
				}else{
					result.append(c);
				}
				break;
			default:
				result.append(c);
			}
		}
		for(;i<sformat.length();i++){
			int loc=getNormalChar(sformat,i);
			if(loc!=-1){
				result.append(sformat.charAt(loc));
				i=loc;
			}
			else break;
		}
		return i;
	}
	public String printUsing(String format,ArrayList<Literal> ls){
		BasicError.check(format.contains("#") ||format.contains("!") ||format.contains("&") ||
				format.contains("@"),BasicError.ILLEGAL_FUNCTION_CALL);
		StringBuilder result=new StringBuilder();
		int from=0;
		for(Literal l:ls){
			if(l.isEmpty()) continue;
			from=parseFormat(format,from,l,result);
			if(from>=format.length()) from=0;
		}
		if(!ls.get(ls.size()-1).isEmpty()) result.append("\r");
		return result.toString();
	}
	public String write(ArrayList<Literal> ls){
		StringBuilder buf=new StringBuilder();
		for(int i=0;i<ls.size();i++){
			Literal l=ls.get(i);
			String val=null;
			if(l.getType()==Literal.TYPE.STRING)
				val="\""+l.getString()+"\""; 
			else val=l.convertToString();
			buf.append(val);
			if(i!=ls.size()-1) buf.append(",");
		}
		buf.append("\r");
		return buf.toString();
	}
	public void replace(VariableParam vparam,int from,int len,String var){
		String orig=getLiteral(vparam).getString();
		len=Math.min(len, var.length());
		len=Math.min(len, orig.length()-from);
		String result=new StringBuilder(orig).replace(from, from+len, var).toString();
		setToVariable(vparam,result);
	}
	public Literal search(VariableParam var,int target,int from,int step){
		VArray v=array.get(var.getName());
		BasicError.check(v!=null && v.getMaxIndex().size()==1, 
				BasicError.ILLEGAL_FUNCTION_CALL);
		for(int i=from;i<v.getMaxIndex().get(0);i+=step){
			if(v.get(i).getInteger()==target){
				return new Literal(i,Literal.TYPE.INTEGER);
			}
		}
		return Literal.MINUS_ONE;
	}
	private String trim(Point pt0,String cmd){
		Point pt1=tscreen.getCursor();
		if(pt0.y==pt1.y){
			int from,to;
			if(pt0.x<pt1.x){
				from=pt0.x;
				to=pt1.x;
			}
			else{
				from=pt1.x;
				to=pt0.x;
			}
			int loc=0;
			StringBuilder str=new StringBuilder();
			for(int i=0;i<cmd.length();i++){
				if(from<=loc && loc<=to) str.append(cmd.charAt(i));
				if(Literal.isHankaku(cmd.charAt(i))) loc++;
				else loc+=2;
			}
			cmd=str.toString();
		}
		return cmd.substring(0,cmd.length()-1);
	}
	public void input(String prompt,Token t,
			final ArrayList<VariableParam>variables){
		if(prompt.startsWith("\"")) prompt=prompt.substring(1);
		if(prompt.endsWith("\"")) prompt=prompt.substring(0,prompt.length()-1);
		if(t==null || (t!=null && t.kind==UsoBasicParserConstants.SEMICOLON)){
			prompt=prompt+"? ";
		}
		final String ppt=prompt;
		tscreen.print(ppt,false);
		final Point  pt0=new Point(tscreen.getCursor().x,tscreen.getCursor().y);
		InputHandler ihandler=new InputHandler(){
			Point pt0_=pt0;

			@Override
			public void onKey(String cmd){
				if(cmd.length()>0 && cmd.charAt(0)==3 && !onStop.trigger()){
					throw new BasicError(BasicError.BREAK);
				}
			}
			@Override
			public void onEnter(String cmd){
				cmd=trim(pt0_,cmd);
				tscreen.print('\r');
				String[] values=cmd.split(",");
				if(values.length!=variables.size()) redo();
				try{
					for(int i=0;i<variables.size();i++){
						VariableParam v=variables.get(i);
						if(values[i].startsWith("\"")) 
							values[i]=values[i].substring(1);
						if(values[i].endsWith("\"")) 
							values[i]=values[i].substring(0, values[i].length()-1);
						Literal l=new Literal(values[i],v.getType());
						setToVariable(v,l);
						tscreen.setKeyBuffer(executingKeyBuffer);
						executor.setExecuting();
					}
				}catch(BasicError e){
					redo();
				}
			}
			private void redo(){
				tscreen.print("?Redo from start\r",false);
				tscreen.print(ppt,false);
				pt0_=new Point(tscreen.getCursor().x,tscreen.getCursor().y);
			}
		};
		executor.setWait();
		tscreen.setDirectModeKeyBuffer(ihandler);
	}
	public Literal input$(final int count){
		if(inputResult==null && !executor.isWait()){
			executor.setWait();
			InputHandler ihandler=new InputHandler(){
				@Override
				public void onKey(String cmd){
					if(cmd.length()>0 && cmd.charAt(0)==3 && !onStop.trigger()){
						throw new BasicError(BasicError.BREAK);
					}
					if(cmd.length()>=count){
						inputResult=new Literal(cmd.substring(0,count),Literal.TYPE.STRING);
						executingKeyBuffer.setInputHandler(null);
						executingKeyBuffer.clear();
						executor.setExecuting();
					}
				};
			};
			executingKeyBuffer.setInputHandler(ihandler);
		}
		Literal r=inputResult;
		if(inputResult!=null){
			inputResult=null;
		}
		return r;
	}
	public void lineInput(String prompt,final VariableParam vparam){
		if(prompt.startsWith("\"")) prompt=prompt.substring(1);
		if(prompt.endsWith("\"")) prompt=prompt.substring(0,prompt.length()-1);

		final String ppt=prompt;
		tscreen.print(ppt,false);
		final Point  pt0=new Point(tscreen.getCursor().x,tscreen.getCursor().y);
		InputHandler ihandler=new InputHandler(){
			@Override
			public void onKey(String cmd){
				if(cmd.length()>0 && cmd.charAt(0)==3 && !onStop.trigger()){
					throw new BasicError(BasicError.BREAK);
				}
			}
			@Override
			public void onEnter(String cmd){
				cmd=trim(pt0,cmd);
				Literal l=new Literal(cmd.toString(),Literal.TYPE.STRING);
				setToVariable(vparam,l);
				tscreen.setKeyBuffer(executingKeyBuffer);
				executor.setExecuting();
			}
		};
		executor.setWait();
		tscreen.setDirectModeKeyBuffer(ihandler);
	}
	public Literal inkey(){
		String buf=executingKeyBuffer.getBuffer();
		executingKeyBuffer.clear();
		if(buf.length()>0){
			Literal l=new Literal(buf.charAt(0)+"",
					Literal.TYPE.STRING);
			return l;
		}
		return Literal.EMPTY_STRING;
	}
	public void files(){
		HashMap<String ,File> files=fileSystem.flist();
		for(String f:files.keySet()){
			tscreen.print(f+"\r",false);
		}
	}
	public void list(int from,int to){
		SortedMap<Integer,String> list=program.tailMap(from);
		ArrayList<Integer> keys = new ArrayList<Integer>(list.keySet());
		ArrayList<ASTstatement> ast=new ArrayList<>();
		for(int j=0;j<keys.size();j++){
			int line=keys.get(j);
			if(line<=to){
				final String str=line+" "+list.get(line)+"\r";
				ast.add(new ASTuserStatement(){
					@Override
					public Object jjtAccept(UsoBasicParserVisitor visitor,
							Object data) {
						env.tscreen.print(str,false);
						return null;
					}
				});
			}
		}
		if(ast.size()>0){
			executor.getContext().insert(ast);
		}
	}
	public File getOpenedFile(int fno){
		return files[fno];
	}
	public void getData(int fno,int loc){
		File f=getFile(fno);
		if(loc>0) f.setLoc(loc);
		String str=f.getContent(256);
		int i=0;
		for(Field ff:field){
			setToVariable(ff.variable,new Literal(str.substring(i,i+ff.length),Literal.TYPE.STRING));
			i+=ff.length;
		}
	}
	public File getFile(int fno){
		File f=files[fno];
		BasicError.check(f!=null, BasicError.FILE_NOT_OPEN);
		return f;
	}
	public void inputSharp(int fno,ArrayList<VariableParam> vparams){
		File f=getFile(fno);
		for(VariableParam vparam:vparams){
			Literal l=f.input(vparam.getType());
			setToVariable(vparam,l);
		}
	}
	public void lineInputSharp(int fno,VariableParam vparam){
		File f=getFile(fno);
		Literal l=f.input(vparam.getType());
		setToVariable(vparam,l);
	}

	private final FileServiceAsync svc =
			GWT.create( FileService.class );

	public void loadRemote(String name){
		executor.setWait();
		svc.load(name, new AsyncCallback<String>(){
			@Override
			public void onFailure(Throwable caught) {
				throw new BasicError(BasicError.DISK_I_O_ERROR);
			}

			@Override
			public void onSuccess(String result) {
				BasicError.check(result!=null,BasicError.BAD_FILE_NAME);
				program.set(result);
				executor.setExecuting();
			}
		});
	}
	public void saveRemote(String name){
		executor.setWait();
		String content=program.getAll();
		svc.save(name, content,new AsyncCallback<Boolean>(){
			@Override
			public void onFailure(Throwable caught) {
				throw new BasicError(BasicError.DISK_I_O_ERROR);
			}

			@Override
			public void onSuccess(Boolean result) {
				executor.setExecuting();
			}
		});
	}
	public void filesRemote(){
		executor.setWait();
		svc.files(new AsyncCallback<ArrayList<String>>(){
			@Override
			public void onFailure(Throwable caught) {
				throw new BasicError(BasicError.DISK_I_O_ERROR);
			}

			@Override
			public void onSuccess(ArrayList<String> result) {
				for(String str:result){
					tscreen.print(str+"\r",false);
				}
				executor.setExecuting();
			}
		});
	}
	public void killRemote(String name){
		executor.setWait();
		svc.kill(name,new AsyncCallback<Boolean>(){
			@Override
			public void onFailure(Throwable caught) {
				throw new BasicError(BasicError.DISK_I_O_ERROR);
			}

			@Override
			public void onSuccess(Boolean result) {
				BasicError.check(result,BasicError.FILE_NOT_FOUND);
				executor.setExecuting();
			}
		});
	}
	public void nameRemote(String from,String to){
		executor.setWait();
		svc.name(from,to,new AsyncCallback<Boolean>(){
			@Override
			public void onFailure(Throwable caught) {
				throw new BasicError(BasicError.DISK_I_O_ERROR);
			}

			@Override
			public void onSuccess(Boolean result) {
				BasicError.check(result,BasicError.FILE_NOT_FOUND);
				executor.setExecuting();
			}
		});
	}
}
