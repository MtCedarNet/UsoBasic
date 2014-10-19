package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.SimpleNode;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParser;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Token;


public class InterpreterNode extends SimpleNode {
	private ArrayList<Token> token=new ArrayList<>();
	private Logger logger=Logger.getLogger("InterpreterNode");
	private int line=-1;
	public ListNode<ASTstatement> listNode=null;

	public void setLine(int line){
		this.line=line;
	}
	public int getLine(){
		return line;
	}
	public InterpreterNode(int id){
		super(id);
	}
	public InterpreterNode(UsoBasicParser p, int id) {
		super(p, id);
	}


	public void addToken(Token t){
		if(t!=null) token.add(t);
	}
	public int getTokenSize(){
		return token.size();
	}
	public void addToken(Token[] ts){
		for(Token t:ts)
			if(t!=null) token.add(t);
	}
	public Token getToken(){
		return token.get(0);
	}
	public Token getToken(int i){
		return token.get(i);
	}

	public void dump(String prefix) {
		logger.severe(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode)children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

}
