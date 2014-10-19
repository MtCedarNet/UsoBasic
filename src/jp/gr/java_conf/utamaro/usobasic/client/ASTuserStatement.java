package jp.gr.java_conf.utamaro.usobasic.client;

import jp.gr.java_conf.utamaro.usobasic.client.parser.ASTstatement;
import jp.gr.java_conf.utamaro.usobasic.client.parser.UsoBasicParserVisitor;

class ASTuserStatement extends ASTstatement {
	public ASTuserStatement(){
		super(-1);
	}

	public Object jjtAccept(UsoBasicParserVisitor visitor, Object data){
		return null;
	};
}
