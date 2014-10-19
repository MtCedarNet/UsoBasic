package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatements;
import jp.gr.java_conf.utamaro.usobasic.client.parser.Node;
import jp.gr.java_conf.utamaro.usobasic.client.parser.ParseException;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParser;

public class Context {
	protected ListNode<ASTstatement> ast=null;
	private ListNode<ASTstatement> previousNode=null;
	private Logger logger=Logger.getLogger("context");

	public boolean isDirectMode(){
		return true;
	}

	public void clear(){
		ast=null;
	}
	private void buildAST(String cmd){
		try{
			ListNode<ASTstatement> last=null;
			UsoBasicParser parser=new UsoBasicParser(cmd);
			ASTstatements s=parser.statements();
			for(int i=0;i<s.jjtGetNumChildren();i++){
				Node n=s.jjtGetChild(i);
				if(n instanceof ASTstatement)
					if(ast==null) last=ast=new ListNode<>((ASTstatement)n);
					else last=last.addToNext((ASTstatement)n);
			}
		}catch(ParseException exp){
			logger.log(Level.SEVERE, "", exp);
			throw new BasicError(BasicError.SYNTAX_ERROR);
		}
	}

	protected Context(){}

	public ListNode<ASTstatement> insert(ArrayList<ASTstatement> as) {
		if(as.size()==0) return null;
		ListNode<ASTstatement> n=null;
		ListNode<ASTstatement> last=null;
		for(ASTstatement a:as){
			if(n==null) last=n=new ListNode<ASTstatement>(a);
			else last=last.addToNext(a);
		}
		last.next=ast;
		ast=n;
		return n;
	}

	public Context(String cmd) {
		buildAST(cmd);
	}

	public void toPrevious(){
		ast=previousNode;
	}

	public ListNode<ASTstatement> getCurrentNode(){
		return previousNode;
	}

	public ASTstatement getNextNode(){
		previousNode = ast;
		if(ast!=null)
			ast=ast.next;
		if(previousNode==null) return null;
		return previousNode.value;
	}
	public void astDump(){
		ast.value.dump("");
	}
	public void setNextStatement(ListNode<ASTstatement> node){
		this.ast=node;
	}
}
