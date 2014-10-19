package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTlabel;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstateDATA;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatements;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Node;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ParseException;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParser;

public class ProgramContext extends Context{
	private ProgramBuffer program=null;
	private Logger logger=Logger.getLogger("pcontext");
	private HashMap<Integer,ListNode<ASTstatement>> astLocation=new HashMap<>();
	private HashMap<String,Integer> labels= new HashMap<>();
	private ListNode<ASTstatement> dataLocation=null;
	private int currentData=-1;
	private Stack<ListNode<ASTstatement>> stack=new Stack<>();

	public void clear(){
		super.clear();
		astLocation.clear();
		labels.clear();
		dataLocation=null;
		currentData=-1;
		stack.clear();
	}
	public void gosub(int line){
		stack.push(ast);
		goTo(line);
	}
	public void resume(int line){
		stack.pop();
		goTo(line);
	}
	public void resume(){
		ast=stack.pop();
	}
	private void buildAST(){
		SortedMap<Integer, String> c=program.tailMap(0);
		ListNode<ASTstatement> node=null;
		for(int line:c.keySet()){
			try{
				UsoBasicParser parser=new UsoBasicParser(program.get(line));
				ASTstatements s=parser.statements();
				for(int i=0;i<s.jjtGetNumChildren();i++){
					ASTstatement nn=null;
					Node n=s.jjtGetChild(i);
					if(n instanceof ASTstatement){
						nn=(ASTstatement)n;
					}
					if(n instanceof ASTlabel){
						nn=new ASTuserStatement();
						ASTlabel label=(ASTlabel)n;
						labels.put("*"+label.getToken().image, line);
					}
					node=add(node,nn,line);
					if(i==0) astLocation.put(line,node);
				}
			}catch(ParseException exp){
				throw new BasicError(BasicError.SYNTAX_ERROR,line);
			}
		}
		restore(0);
	}

	private ListNode<ASTstatement> add(ListNode<ASTstatement>last,ASTstatement nn,int line){
		nn.setLine(line);
		if(ast==null) last=ast=new ListNode<ASTstatement>(nn);
		else last=last.addToNext(nn);
		return last;
	}

	public String getNextData(){
		BasicError.check(dataLocation!=null && dataLocation.next!=null,BasicError.OUT_OF_DATA);
		ASTstateDATA d=(ASTstateDATA)dataLocation.value.jjtGetChild(0);
		if(currentData>=d.getTokenSize()){
			toNextDataLocation(dataLocation.next);
			return getNextData();
		}
		String image=d.getToken(currentData++).image;
		if(image.startsWith("\"")) image=image.substring(1);
		if(image.endsWith("\"")) image=image.substring(0,image.length()-2);
		return image;
	}

	private boolean toNextDataLocation(ListNode<ASTstatement> loc){
		for(ListNode<ASTstatement>node=loc;node!=null;node=node.next){
			if(node.value.jjtGetNumChildren()>0){
				Node n=node.value.jjtGetChild(0);
				if(n instanceof ASTstateDATA){
					dataLocation=node;
					currentData=0;
					return true;
				}
			}
		}
		return false;
	}

	public void restore(int line){
		for(;line<=program.getLastLine();line++){
			if(astLocation.get(line)==null) continue;
			if(toNextDataLocation(astLocation.get(line))) return;
		}
	}

	public ProgramContext(ProgramBuffer program) {
		super();
		ErrorInterrupt.setContext(this);
		Interrupt.setExecutor(this);
		this.program=program;
		buildAST();
	}
	public boolean isDirectMode(){
		return false;
	}

	public void goTo(int line){
		ListNode<ASTstatement> i=astLocation.get(line);
		BasicError.check(i!=null,BasicError.UNDEFINED_LINE_NUMBER);
		ast=i;
	}
	public Integer getLineFromLabel(String label){
		if(!label.startsWith("*")) label="*"+label;
		Integer i= labels.get(label);
		BasicError.check(i!=null,BasicError.UNDEFINED_LABEL);
		return i;
	}
}
