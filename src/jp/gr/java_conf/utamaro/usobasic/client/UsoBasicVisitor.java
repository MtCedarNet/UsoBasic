package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.gr.java_conf.utamaro.usobasic.client.parser.*;
import jp.gr.java_conf.utamaro.usobasic.client.InterpreterNode;;

public class UsoBasicVisitor extends UsoBasicParserDefaultVisitor{
	private class ForLoop{
		private VariableParam vparam;
		private Literal to;
		private Literal step=Literal.ONE;
		private ListNode<ASTstatement> forStart;

		public ForLoop(VariableParam v,Literal from,Literal to,Literal step){
			this.step=step;
			this.forStart=context.getCurrentNode().next;
			this.vparam=v;
			this.to=to;
			BasicError.check(from.getType()!=Literal.TYPE.STRING, BasicError.TYPE_MISMATCH);
			BasicError.check(to.getType()!=Literal.TYPE.STRING, BasicError.TYPE_MISMATCH);
			env.setToVariable(v,from);
		}
		public boolean isContinuable(){
			Literal now=env.getLiteral(vparam);
			boolean cont1=step.le(Literal.ZERO).isTrue() && now.ge(to).isTrue();
			boolean cont2=step.ge(Literal.ZERO).isTrue() && now.le(to).isTrue();
			return cont1 || cont2;
		}
		public boolean next(){
			Literal now=env.getLiteral(vparam).add(step);
			env.setToVariable(vparam,now);
			if( isContinuable()){
				context.setNextStatement(forStart); 
				return true;
			}
			return false;
		}
		public boolean equals(Object obj){
			if(! (obj instanceof ForLoop)) return false;
			ForLoop f=(ForLoop)obj;
			return f.vparam.equals(vparam);
		}
	}
	private class WhileLoop{
		private ASTexpression expression;
		private ListNode<ASTstatement> whiletart;

		public WhileLoop(ASTstateWHILE node,ASTexpression expression){
			this.whiletart=context.getCurrentNode().next;
			this.expression=expression;
		}
		public boolean wend(){
			Literal l=(Literal)expression.jjtAccept(UsoBasicVisitor.this, null);
			if(l.isTrue()){
				context.setNextStatement(whiletart); 
				return true;
			}
			return false;
		}
	}

	public void end(){
		forStack.clear();
		whileStack.clear();
		forMap.clear();
	}
	public void endCheck(){
		BasicError.check(forStack.size()==0, BasicError.FOR_WITHOUT_NEXT);
		BasicError.check(whileStack.size()==0, BasicError.WHILE_WITHOUT_WEND);
	}



	private Environment env;
	private Context context;
	private Executor executor;
	private Logger logger=Logger.getLogger("vis");
	private Stack<ForLoop> forStack=new Stack<>();
	private HashMap<VariableParam,ForLoop> forMap= new HashMap<>();
	private Stack<WhileLoop> whileStack=new Stack<>();

	public UsoBasicVisitor(Environment env){
		this.env=env;
		this.executor=env.executor;
		this.context=executor.getContext();
	}


	private int acceptIfExist(Node node,int num,int def){
		if(node.jjtGetNumChildren()>num){
			Literal l=(Literal)(node.jjtGetChild(num).jjtAccept(this, null));
			if(!l.isEmpty()){
				return l.getInteger();
			}
		}
		return def;
	}
	private double acceptIfExist(Node node,int num,double def){
		if(node.jjtGetNumChildren()>num){
			Literal l=(Literal)(node.jjtGetChild(num).jjtAccept(this, null));
			if(!l.isEmpty()){
				return l.getDouble();
			}
		}
		return def;
	}
	private String acceptIfExist(Node node,int num,String def){
		if(node.jjtGetNumChildren()>num){
			Literal l=(Literal)(node.jjtGetChild(num).jjtAccept(this, null));
			if(!l.isEmpty()){
				return l.getString();
			}
		}
		return def;
	}

	public Object visit(ASTstatement node, Object data){
		if(node.jjtGetNumChildren()>0)
			return node.jjtGetChild(0).jjtAccept(this, null);
		else
			return null;
	}

	private Literal acceptLiteral(Node node,int num){
		if(node.jjtGetNumChildren()<num) return null;
		return (Literal)(node.jjtGetChild(num).jjtAccept(this, null));
	}

	private ArrayList<Literal> acceptLiteral(Node node,int from,int count){
		ArrayList<Literal> literals=new ArrayList<>();
		for(int i=from;i<from+count;i++){
			literals.add((Literal)(node.jjtGetChild(i).jjtAccept(this, null)));
		}
		return literals;
	}

	private ArrayList<Literal> acceptAllChild(Node node){
		return acceptLiteral(node,0,node.jjtGetNumChildren());
	}
	@Override
	public Object defaultVisit(SimpleNode node, Object data){
		throw new BasicError(BasicError.FEATURE_NOT_AVAILABLE);
	}
	@Override
	public Object visit(ASTexpression node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0);
	}
	@Override
	public Object visit(ASTopEQV node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).eqv(l.get(1));
	}
	@Override
	public Object visit(ASTopIMP node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).imp(l.get(1));
	}
	@Override
	public Object visit(ASTopXOR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).xor(l.get(1));
	}
	@Override
	public Object visit(ASTopOR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).or(l.get(1));
	}
	@Override
	public Object visit(ASTopAND node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).and(l.get(1));
	}
	@Override
	public Object visit(ASTopNOT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).not();
	}
	@Override
	public Object visit(ASTopEQL node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).eq(l.get(1));
	}
	@Override
	public Object visit(ASTopNEQ node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).neq(l.get(1));
	}
	@Override
	public Object visit(ASTopLT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).lt(l.get(1));
	}
	@Override
	public Object visit(ASTopLE node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).le(l.get(1));
	}
	@Override
	public Object visit(ASTopGT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).gt(l.get(1));
	}
	@Override
	public Object visit(ASTopGE node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).ge(l.get(1));
	}
	@Override
	public Object visit(ASTopADD node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).add(l.get(1));
	}
	@Override
	public Object visit(ASTopMINUS node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).minus(l.get(1));
	}
	@Override
	public Object visit(ASTopMOD node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).mod(l.get(1));
	}
	@Override
	public Object visit(ASTopIDIV node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).idiv(l.get(1));
	}
	@Override
	public Object visit(ASTopMUL node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).mul(l.get(1));
	}
	@Override
	public Object visit(ASTopDIV node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).div(l.get(1));
	}
	@Override
	public Object visit(ASTopNEG node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).negative();
	}
	@Override
	public Object visit(ASTopPOS node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0);
	}
	@Override
	public Object visit(ASTopPOW node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).pow(l.get(1));
	}
	@Override
	public Object visit(ASTid node, Object data){
		VariableParam v=(VariableParam)node.jjtGetChild(0).jjtAccept(this, null);
		return env.getLiteral(v);
	}
	@Override
	public Object visit(ASTfuncABS node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).abs();
	}
	@Override
	public Object visit(ASTfuncAKCNV node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).akcnv();
	}
	@Override
	public Object visit(ASTfuncASC node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).asc();
	}

	@Override
	public Object visit(ASTfuncATN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).atn();
	}
	@Override
	public Object visit(ASTfuncCDBL node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).cdbl();
	}
	public Object visit(ASTfuncCSRLIN node, Object data){
		return new Literal(env.tscreen.getCursor().y,Literal.TYPE.INTEGER);
	}
	public Object visit(ASTfuncPOS node, Object data){
		return new Literal(env.tscreen.getCursor().x,Literal.TYPE.INTEGER);
	}
	@Override
	public Object visit(ASTfuncCHR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).chr();
	}
	@Override
	public Object visit(ASTfuncCINT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).cint();
	}
	@Override
	public Object visit(ASTfuncCOS node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).cos();
	}
	@Override
	public Object visit(ASTfuncCSNG node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).csng();
	}
	//	@Override
	//	public Object visit(ASTfuncCVD node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).cvd();
	//	}
	//	@Override
	//	public Object visit(ASTfuncCVI node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).cvi();
	//	}
	//	@Override
	//	public Object visit(ASTfuncCVS node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).cvs();
	//	}
	@Override
	public Object visit(ASTfuncDATE node, Object data){
		return Literal.date();
	}
	//	public Object visit(ASTfuncEOF node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		if(env.eof(l.get(0).getInteger())){
	//			return Literal.MINUS_ONE;
	//		}
	//		return Literal.ZERO;
	//
	//	}
	@Override
	public Object visit(ASTfuncEXP node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).exp();
	}
	@Override
	public Object visit(ASTfuncERR node, Object data){
		return new Literal(executor.getErrorId(),Literal.TYPE.INTEGER);
	}
	@Override
	public Object visit(ASTfuncERL node, Object data){
		return new Literal(executor.getErrorLine(),Literal.TYPE.INTEGER);
	}
	@Override
	public Object visit(ASTfuncFIX node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).fix();
	}
	//	@Override
	//	public Object visit(ASTfuncFPOS node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		int loc=env.getOpenedFile(l.get(0).getInteger()).getLoc();
	//		return new Literal(loc,Literal.TYPE.INTEGER);
	//	}
	@Override
	public Object visit(ASTfuncHEX node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).hex();
	}
	public Object visit(ASTfuncINKEY node, Object data){
		return env.inkey();
	}
	@Override
	public Object visit(ASTfuncINSTR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int loc=0;
		int num=0;
		if(l.size()==3){
			loc=l.get(num++).getInteger();
		}
		return l.get(num++).instr(loc,l.get(num++).getString());
	}
	@Override
	public Object visit(ASTfuncINT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).toInt();
	}
	@Override
	public Object visit(ASTfuncJIS node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).jis();
	}
	@Override
	public Object visit(ASTfuncKACNV node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).kacnv();
	}
	@Override
	public Object visit(ASTfuncKLEN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int func=0;
		if(l.size()==2) func=l.get(1).getInteger();
		return l.get(0).klen(func);
	}
	@Override
	public Object visit(ASTfuncKMID node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int end=-1;
		if(l.size()==3) end=l.get(2).getInteger();
		return l.get(0).kmid(l.get(1).getInteger(),end);
	}
	@Override
	public Object visit(ASTfuncKNJ node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).knj();
	}
	@Override
	public Object visit(ASTfuncLEFT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int len=l.get(1).getInteger();
		return l.get(0).left(len);
	}
	@Override
	public Object visit(ASTfuncLEN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).len();
	}
	//	@Override
	//	public Object visit(ASTfuncLOC node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		int loc= env.getOpenedFile(l.get(0).getInteger()).getLoc();
	//		return new Literal(loc,Literal.TYPE.INTEGER);
	//	}
	//	public Object visit(ASTfuncLOF node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		int loc= env.getOpenedFile(l.get(0).getInteger()).size()/256;
	//		return new Literal(loc,Literal.TYPE.INTEGER);
	//	}
	@Override
	public Object visit(ASTfuncLOG node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).log();
	}

	@Override
	public Object visit(ASTfuncMID node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int end=-1;
		if(l.size()==3) end=l.get(2).getInteger();
		return l.get(0).mid(l.get(1).getInteger(),end);
	}
	//	@Override
	//	public Object visit(ASTfuncMKD node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).mkd();
	//	}
	//	@Override
	//	public Object visit(ASTfuncMKI node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).mki();
	//	}
	//	@Override
	//	public Object visit(ASTfuncMKS node, Object data){
	//		ArrayList<Literal> l=acceptAllChild(node);
	//		return l.get(0).mks();
	//	}
	@Override
	public Object visit(ASTfuncOCT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).oct();
	}
	@Override
	public Object visit(ASTfuncRIGHT node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int len=l.get(1).getInteger();
		return l.get(0).right(len);
	}
	@Override
	public Object visit(ASTfuncRND node, Object data){
		int func=acceptIfExist(node,0,1);
		return Literal.rnd(func);
	}
	@Override
	public Object visit(ASTfuncSEARCH node, Object data){
		VariableParam vparam=new VariableParam(node.getToken().image);
		int target=acceptIfExist(node,1,0);
		int from=acceptIfExist(node,2,0);
		int step=acceptIfExist(node,3,1);
		return env.search(vparam, target, from, step);
	}
	@Override
	public Object visit(ASTfuncSGN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).sgn();
	}
	@Override
	public Object visit(ASTfuncSIN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).sin();
	}
	@Override
	public Object visit(ASTfuncSPACE node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		int len=l.get(0).getInteger();
		return Literal.space(len);
	}
	@Override
	public Object visit(ASTfuncSQR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).sqr();
	}
	@Override
	public Object visit(ASTfuncSTR node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).str();
	}
	@Override
	public Object visit(ASTfuncSTRING node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		char c;
		if(l.get(1).getType()!=Literal.TYPE.STRING){
			c=(char)l.get(1).getInteger();
		}
		else c=l.get(1).getString().charAt(0);
		return Literal.string(l.get(0).getInteger(),c);
	}
	@Override
	public Object visit(ASTfuncTAN node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).tan();
	}
	@Override
	public Object visit(ASTfuncTIME node, Object data){
		return Literal.time();
	}
	@Override
	public Object visit(ASTfuncVAL node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		return l.get(0).val();
	}
	@Override
	public Object visit(ASTintConst node, Object data){
		Token t=node.getToken();
		switch(t.kind){
		case UsoBasicParserConstants.INTEGER16:
			return new Literal(t.image.substring(2), 16);
		case UsoBasicParserConstants.INTEGER8:
			return new Literal(t.image.substring(1), 8);
		case UsoBasicParserConstants.INTEGER8O:
			return new Literal(t.image.substring(2), 8);
		case UsoBasicParserConstants.INTEGER:
		case UsoBasicParserConstants.DOUBLE:
		case UsoBasicParserConstants.SINGLE:
			return new Literal(t.image, Literal.TYPE.INTEGER);
		}
		return null;
	}
	@Override
	public Object visit(ASTsingleConst node, Object data){
		Token t=node.getToken();
		switch(t.kind){
		case UsoBasicParserConstants.INTEGER:
		case UsoBasicParserConstants.SINGLE :
		case UsoBasicParserConstants.SINGLE_EXP:
		case UsoBasicParserConstants.DOUBLE:
			return new Literal(t.image, Literal.TYPE.SINGLE);
		}
		return null;
	}
	@Override
	public Object visit(ASTdoubleConst node, Object data){
		Token t=node.getToken();
		switch(t.kind){
		case UsoBasicParserConstants.INTEGER :
		case UsoBasicParserConstants.SINGLE :
		case UsoBasicParserConstants.DOUBLE:
			return new Literal(t.image, Literal.TYPE.DOUBLE);
		case UsoBasicParserConstants.DOUBLE_EXP :
			String str = t.image.toString().replaceAll("[Dd]", "e");
			return new Literal(str, Literal.TYPE.DOUBLE);
		}
		return null;
	}
	@Override
	public Object visit(ASTstringConst node, Object data){
		if(node.getTokenSize()==0) return new Literal();
		String str = node.getToken().image.toString().
				replaceAll("^\"", "").replaceAll("\"$", "");
		return new Literal(str, Literal.TYPE.STRING);
	}

	public Object visit(ASTempty node, Object data){
		return Literal.EMPTY;
	}
	@Override
	public Object visit(ASTstatePRINT node,Object data){
		ArrayList<Literal> literals=new ArrayList<>();
		ArrayList<Token> tokens=new ArrayList<>();
		Literal last=null;
		for(int i=0;i<node.jjtGetNumChildren();){
			if(node.jjtGetChild(i) instanceof ASTtab){
				literals.add((Literal)node.jjtGetChild(i++).jjtAccept(this, last));
			}else{
				last=acceptLiteral(node,i++);
				literals.add(last);
			}
			if(i<node.jjtGetNumChildren()){
				InterpreterNode n=(InterpreterNode)node.jjtGetChild(i++);
				tokens.add(n.getToken());
			}
		}
		String str=env.print(literals, tokens);
		env.tscreen.print(str,false);
		return (Boolean)true;

	}

	@Override
	public Object visit(ASTstatePRINTUSING node,Object data){
		ArrayList<Literal> literals=new ArrayList<>();
		String format=acceptLiteral(node,0).getString();
		for(int i=1;i<node.jjtGetNumChildren();){
			literals.add(acceptLiteral(node,i++));
			if(i<node.jjtGetNumChildren()) i++;
		}
		String str=env.printUsing(format,literals);
		env.tscreen.print(str,false);
		return (Boolean)true;

	}
	public Object visit(ASTstateBEEP node, Object data){
		ArrayList<Literal> l=acceptAllChild(node);
		if(l.size()==0 || l.get(0).isEmpty()) env.beep();
		else env.beep(l.get(0).getInteger());
		return (Boolean)true;
	}
	public Object visit(ASTstateCIRCLE node, Object data){
		int x=acceptIfExist(node,0,-1);
		int y=acceptIfExist(node,1,-1);
		int r=acceptIfExist(node,2,-1);
		int color1=acceptIfExist(node,3,-1);
		double start_angle=acceptIfExist(node,4,0.0);
		double end_angle=acceptIfExist(node,5,Math.PI*2);
		double ratio=acceptIfExist(node,6,1.0);
		boolean isF=false;
		if(node.getTokenSize()!=0) isF=true;
		int color2=acceptIfExist(node,8,-1);
		env.getActiveScreen().circle(false, x, y, 
				r,color1, start_angle, end_angle, ratio, isF, color2);
		return (Boolean)true;
	}
	//	public Object visit(ASTstateCLOSE node, Object data){
	//		ArrayList<Literal> ls=acceptAllChild(node);
	//		if(ls.size()==0 || ls.get(0).isEmpty()) env.closeAll();
	//		else{
	//			for(Literal l:ls){
	//				env.close(l.getInteger());
	//			}
	//		}
	//		return (Boolean)true;
	//	}
	public Object visit(ASTstateCLEAR node, Object data){
		env.clear();
		return (Boolean)true;
	}
	public Object visit(ASTstateCLS node, Object data){
		int func=acceptIfExist(node,0,1);
		if(func==1 || func==3) env.tscreen.cls();
		if(func==2 || func==3) env.getActiveScreen().cls();
		return (Boolean)true;
	}
	public Object visit(ASTstateCOLOR node, Object data){
		int func=acceptIfExist(node,0,-1);
		int bg=acceptIfExist(node,1,-1);
		if(func!=-1)	Character.setDefaultColor(func);
		if(bg!=-1)	GraphicScreen.setBGColor(bg);
		if(func==-1 && bg==-1) GraphicScreen.initColor();
		return (Boolean)true;
	}
	public Object visit(ASTstateCOLOR_EQ node, Object data){
		int pal=acceptIfExist(node,0,-1);
		int color=acceptIfExist(node,1,-1);
		env.getActiveScreen().setColor(pal, color);
		return (Boolean)true;
	}
	public Object visit(ASTstateCONSOLE node, Object data){
		int ss=acceptIfExist(node,0,-1);
		int se=acceptIfExist(node,1,-1);
		int fk=acceptIfExist(node,2,-1);
		if(ss!=-1) env.tscreen.setScrollStart(ss);
		if(se!=-1) env.tscreen.setScrollCount(se);
		if(fk!=-1) {
			env.tscreen.setPrintFK(fk!=0);
			env.tscreen.cls();
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateDATA node, Object data){
		return (Boolean)true;
	}
	public Object visit(ASTstateDEFFN node, Object data){
		ArrayList<VariableParam> ls=new ArrayList<>();
		InterpreterNode in=(InterpreterNode)node;
		ls.add(new VariableParam(in.getToken(0).image));
		for(int i=1;i<in.getTokenSize();i++){
			ls.add(new VariableParam(in.getToken(i).image));
		}
		ASTexpression e=(ASTexpression)node.jjtGetChild(node.jjtGetNumChildren()-1);
		env.setDeffn(ls,e);
		return (Boolean)true;
	}
	private void setDeftype(Node node,Literal.TYPE type){
		for(int i=0;i<node.jjtGetNumChildren();i++){
			InterpreterNode n=(InterpreterNode)node.jjtGetChild(i);
			if(n.getTokenSize()==1){
				VariableParam.setDeftype(n.getToken().image.charAt(0),type);
			}
			if(n.getTokenSize()==2){
				char from=n.getToken(0).image.charAt(0);
				char to=n.getToken(1).image.charAt(0);
				for(int j=from;j<=to;j++)
					VariableParam.setDeftype((char)j,type);
			}
		}
	}

	public Object visit(ASTvariable node, Object data){
		InterpreterNode n=(InterpreterNode)node.jjtGetChild(0);
		String var=n.getToken().image;
		if(node.jjtGetNumChildren()==1){
			return new VariableParam(var);
		}
		ArrayList<Literal> ls=new ArrayList<>();
		for(int i=1;i<node.jjtGetNumChildren();i++){
			ls.add(acceptLiteral(node,i));
		}			
		return new VariableParam(var,ls);
	}
	public Object visit(ASTarray node, Object data){
		InterpreterNode n=(InterpreterNode)node.jjtGetChild(0);
		String var=n.getToken().image;
		ArrayList<Literal> ls=new ArrayList<>();
		for(int i=1;i<node.jjtGetNumChildren();i++){
			ls.add(acceptLiteral(node,i));
		}			
		return new VariableParam(var,ls);
	}
	public Object visit(ASTstateDEFINT node, Object data){
		setDeftype(node,Literal.TYPE.INTEGER);
		return (Boolean)true;
	}
	public Object visit(ASTstateDEFSNG node, Object data){
		setDeftype(node,Literal.TYPE.SINGLE);
		return (Boolean)true;
	}
	public Object visit(ASTstateDEFDBL node, Object data){
		setDeftype(node,Literal.TYPE.DOUBLE);
		return (Boolean)true;
	}
	public Object visit(ASTstateDEFSTR node, Object data){
		setDeftype(node,Literal.TYPE.STRING);
		return (Boolean)true;
	}
	public Object visit(ASTstateDIM node, Object data){
		for(int i=0;i<node.jjtGetNumChildren();i++){
			VariableParam array=(VariableParam)(node.jjtGetChild(i).
					jjtAccept(this, null));
			env.dim(array);
		}			
		return (Boolean)true;
	}
	public Object visit(ASTstateEDIT node, Object data){
		env.program.showEditor();
		return (Boolean)true;
	}
	public Object visit(ASTstateLET node, Object data){
		Literal l=acceptLiteral(node,1);
		VariableParam v=(VariableParam)node.jjtGetChild(0).jjtAccept(this, null);
		env.setToVariable(v, l);
		return (Boolean)true;
	}
	public Object visit(ASTstateEND node, Object data){
		env.executor.stop();
		return (Boolean)true;
	}
	public Object visit(ASTstateERASE node, Object data){
		for(int i=0;i<node.jjtGetNumChildren();i++){
			InterpreterNode n=(InterpreterNode)node.jjtGetChild(i);
			String ary=n.getToken().image;
			env.erase(new VariableParam(ary));
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateERROR node, Object data){
		ArrayList<Literal> ls=acceptAllChild(node);
		throw new BasicError(ls.get(0).getInteger());
	}
	//	public Object visit(ASTstateFIELD node, Object data){
	//		int fno=acceptLiteral(node,0).getInteger();
	//		ArrayList<Integer> size=new ArrayList<>();
	//		ArrayList<VariableParam> var=new ArrayList<>();
	//		for(int i=1;i<node.jjtGetNumChildren();){
	//			Literal l=acceptLiteral(node,i++);
	//			VariableParam v=(VariableParam)node.jjtGetChild(i++).jjtAccept(this, null);
	//			var.add(v);
	//			size.add(l.getInteger());
	//		}
	//		env.setField(fno, var, size);
	//		return (Boolean)true;
	//	}
	//	public Object visit(ASTstateFILES node, Object data){
	//		env.files();
	//		return (Boolean)true;
	//	}
	private void skipToNext(VariableParam v){
		Node n;
		int forNum=1;
		while ((n=context.getNextNode())!=null){
			if(n instanceof ASTstateFOR){
				forNum++;
			}
			if(n instanceof ASTstateNEXT){
				int cnum=n.jjtGetNumChildren();
				if(cnum==0){
					if(--forNum==0){
						break;
					}
				}
				else{
					if(forNum<=cnum){
						VariableParam vp=(VariableParam)n.jjtGetChild(
								cnum-forNum).jjtAccept(this, null);
						BasicError.check(vp.equals(v), BasicError.FOR_WITHOUT_NEXT);
						break;
					}else{
						forNum-=cnum;
					}
				}
			}
		}
	}
	public Object visit(ASTvariables node,Object data){
		ArrayList<VariableParam> variables=new ArrayList<>();
		for(int loc=0;loc<node.jjtGetNumChildren();loc++){
			variables.add((VariableParam)node.jjtGetChild(loc).jjtAccept(this, null));
		}
		return variables;
	}
	public Object visit(ASTstateFOR node, Object data){
		VariableParam v=(VariableParam)node.jjtGetChild(0).jjtAccept(this, null);
		Literal from=acceptLiteral(node,1);
		Literal to=acceptLiteral(node,2);
		Literal step=new Literal(acceptIfExist(node,3,1.0),Literal.TYPE.DOUBLE);
		ForLoop f=new ForLoop(v,from,to,step);
		if(f.isContinuable()){
			forMap.put(v, f);
			forStack.add(f);
		}else{
			skipToNext(v);
		}
		return (Boolean)true;
	}
	private boolean next(ForLoop f_){
		if(!f_.next()){
			forStack.pop();
			forMap.remove(f_.vparam);
			return false;
		}
		return true;
	}

	public Object visit(ASTstateNEXT node, Object data){
		if(node.jjtGetNumChildren()==0){
			try{
				next(forStack.lastElement());
			}catch(NoSuchElementException e){
				throw new BasicError(BasicError.NEXT_WITHOUT_FOR);
			}
		}else{
			@SuppressWarnings("unchecked")
			ArrayList<VariableParam> variables=(ArrayList<VariableParam>)node.
			jjtGetChild(0).jjtAccept(this, null);
			for(VariableParam v:variables){
				ForLoop f_=forMap.get(v);
				if(next(f_)) break;
			}
		}
		return (Boolean)true;
	}
	//	public Object visit(ASTstateGET node, Object data){
	//		int fno=acceptIfExist(node,0,-1);
	//		int loc=acceptIfExist(node,1,-1);
	//		env.getData(fno, loc);
	//		return (Boolean)true;
	//	}
	public Object visit(ASTstateGET_AT node, Object data){
		InterpreterNode n=(InterpreterNode)node.jjtGetChild(0);
		int x1=acceptIfExist(n,0,-1);
		int y1=acceptIfExist(n,1,-1);
		int x2=acceptIfExist(n,2,-1);
		int y2=acceptIfExist(n,3,-1);
		if(n.getTokenSize()!=0){
			x2+=x1;
			y2+=y1;
		}
		VariableParam v=(VariableParam)node.jjtGetChild(1).jjtAccept(this, null);
		env.getActiveScreen().getImageData(x1, y1, x2, y2, v);
		return (Boolean)true;
	}
	public Object visit(ASTlineNumber node, Object data){
		if(node.getTokenSize()==0){
			return null;
		}
		if(node.getToken(0).kind==UsoBasicParserConstants.INTEGER){
			return Integer.parseInt(node.getToken(0).image);
		}else{
			ProgramContext pc=executor.getProgramContext();
			return pc.getLineFromLabel(node.getToken(0).image);
		}
	}
	public Object visit(ASTstateGOSUB node, Object data){
		int line =(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		executor.getProgramContext().gosub(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateGOTO node, Object data){
		int line =(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		executor.getProgramContext().goTo(line);
		return (Boolean)true;
	}

	public Object visit(ASTstateHELP_ONOFF node, Object data){
		if(node.getToken().kind==UsoBasicParserConstants.HELP_ON){
			env.onHelp.setState(Interrupt.STATE.ON);
		}
		if(node.getToken().kind==UsoBasicParserConstants.HELP_OFF){
			env.onHelp.setState(Interrupt.STATE.OFF);
		}
		if(node.getToken().kind==UsoBasicParserConstants.HELP_STOP){
			env.onHelp.setState(Interrupt.STATE.STOP);
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateSTOP_ONOFF node, Object data){
		if(node.getToken().kind==UsoBasicParserConstants.STOP_ON){
			env.onStop.setState(Interrupt.STATE.ON);
		}
		if(node.getToken().kind==UsoBasicParserConstants.STOP_OFF){
			env.onStop.setState(Interrupt.STATE.OFF);
		}
		if(node.getToken().kind==UsoBasicParserConstants.STOP_STOP){
			env.onStop.setState(Interrupt.STATE.STOP);
		}
		return (Boolean)true;
	}
	private void setKey(int kind,int key){
		if(kind==UsoBasicParserConstants.ON){
			env.onKey[key].setState(Interrupt.STATE.ON);
		}
		if(kind==UsoBasicParserConstants.OFF){
			env.onKey[key].setState(Interrupt.STATE.OFF);
		}
		if(kind==UsoBasicParserConstants.STOP){
			env.onKey[key].setState(Interrupt.STATE.STOP);
		}		
	}
	public Object visit(ASTstateKEY_ONOFF node, Object data){
		int k=acceptIfExist(node,0,-1);
		if(k!=-1){
			setKey(node.getToken().kind,k-1);
		}else{
			for(int i=0;i<10;i++){
				setKey(node.getToken().kind,i);
			}
		}
		return (Boolean)true;
	}

	public void execIfState(InterpreterNode node){
		ArrayList<ASTstatement> ast=new ArrayList<>();
		for(int i=0;i<node.jjtGetNumChildren();i++){
			InterpreterNode n=(InterpreterNode)node.jjtGetChild(i);
			if(n instanceof ASTlineNumber){
				int line=(Integer)n.jjtAccept(this, null);
				executor.getProgramContext().goTo(line);
				return;
			}else{
				if(node.listNode==null) ast.add((ASTstatement)n);
			}
		}
		if(node.listNode==null){
			ListNode<ASTstatement> ln=context.insert(ast);
			node.listNode=ln;
		}else{
			context.setNextStatement(node.listNode);
		}

	}
	public Object visit(ASTstateIF node, Object data){
		Literal cond=acceptLiteral(node,0);
		if(cond.isTrue()){
			execIfState((InterpreterNode)node.jjtGetChild(1));
		}else{
			if(node.jjtGetNumChildren()==3){
				execIfState((InterpreterNode)node.jjtGetChild(2));
			}
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateINPUT node, Object data){
		InterpreterNode n=(InterpreterNode)node;
		String prompt="";
		Token t=null;
		int loc=0;
		if(n.getTokenSize()==1){
			prompt=n.getToken().image;
			InterpreterNode n0=(InterpreterNode)n.jjtGetChild(0);
			t=n0.getToken();
			loc++;
		}
		@SuppressWarnings("unchecked")
		ArrayList<VariableParam> variables=
		(ArrayList<VariableParam>)n.jjtGetChild(loc).jjtAccept(this, null);
		env.input(prompt, t, variables);
		return (Boolean)true;
	}
	//	public Object visit(ASTstateINPUT_S node, Object data){
	//		int fno=acceptLiteral(node,0).getInteger();
	//		@SuppressWarnings("unchecked")
	//		ArrayList<VariableParam> variables=
	//		(ArrayList<VariableParam>)node.jjtGetChild(1).jjtAccept(this, null);
	//		env.inputSharp(fno, variables);
	//		return (Boolean)true;
	//	}
	public Object visit(ASTfuncINP node, Object data){
		int port=acceptLiteral(node,0).getInteger();
		BasicError.check(0xe0<=port && port<=0xec,BasicError.FEATURE_NOT_AVAILABLE);
		return new Literal(env.executingKeyBuffer.getFlag(port),Literal.TYPE.INTEGER);
	}
	public Object visit(ASTfuncINPUT node, Object data){
		int count=acceptLiteral(node,0).getInteger();
		Literal l;
		if(node.jjtGetNumChildren()==2){
			int fno=acceptLiteral(node,1).getInteger();
			l=new Literal(env.getFile(fno).getContent(count),Literal.TYPE.STRING);
		}else{
			l=env.input$(count);
			if(l==null){
				context.toPrevious();
				l=Literal.EMPTY_STRING;
			}
		}
		return l;
	}
	public Object visit(ASTstateKEY node, Object data){
		int kno=acceptLiteral(node,0).getInteger();
		String str=acceptLiteral(node,1).getString();
		BasicError.check(kno>0, BasicError.ILLEGAL_FUNCTION_CALL);
		env.tscreen.setKey(kno-1, str);
		return (Boolean)true;
	}
	public Object visit(ASTstateKEYLIST node, Object data){
		env.tscreen.listKey();
		return (Boolean)true;
	}
	//	public Object visit(ASTstateKILL node, Object data){
	//		String f=acceptIfExist(node,0,"");
	//		File file=env.fileSystem.searchFile(f);
	//		BasicError.check(file!=null, BasicError.BAD_FILE_NAME);
	//		env.fileSystem.remove(file);
	//		return (Boolean)true;
	//	}
	public Object visit(ASTstateLIST node, Object data){
		if(node.jjtGetNumChildren()>0){
			Point range=(Point)node.jjtGetChild(0).jjtAccept(this, null);
			env.list(range.x,range.y);
		}else{
			env.list(0, Integer.MAX_VALUE);
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateRUN node, Object data){
		env.clear();
		InterpreterNode n= (InterpreterNode)node;
		if(node.jjtGetNumChildren()==0){
			if(n.getTokenSize()==0){
				ProgramContext pc=new ProgramContext(env.program);
				executor.execute(pc);
			}else{
				throw new BasicError(BasicError.FEATURE_NOT_AVAILABLE);
			}
		}else{
			int line=(Integer)node.jjtGetChild(1).jjtAccept(this, null);
			ProgramContext pc=new ProgramContext(env.program);
			pc.goTo(line);
			executor.execute(pc);
		}
		return (Boolean)true;
	}
	public Object visit(ASTlineNumberRange node,Object data){
		Point p=new Point(0,Integer.MAX_VALUE);
		InterpreterNode n=(InterpreterNode)node.jjtGetChild(0);
		if(n instanceof ASTlineNumberFrom){
			p.x=(Integer)n.jjtGetChild(0).jjtAccept(this, null);
			if(n.jjtGetNumChildren()==2){
				p.y=(Integer)n.jjtGetChild(1).jjtAccept(this, null);
			}else{
				if(n.getTokenSize()==0) p.y=p.x;
			}
		}else{
			p.y=(Integer)n.jjtGetChild(0).jjtAccept(this, null);
		}
		return p;
	}
	public Object visit(ASTstateLINE node, Object data){
		int x1=0;
		int y1=0;
		int num=0;
		boolean isStep=true;
		Node n=node.jjtGetChild(num);
		if(n instanceof ASTpoint1){
			isStep=false;
			x1=acceptIfExist(n,0,-1);
			y1=acceptIfExist(n,1,-1);
			n=node.jjtGetChild(++num);
		}
		int x2=acceptIfExist(node,num++,-1);
		int y2=acceptIfExist(node,num++,-1);
		int color1=-1;
		Token bf=null;
		int color2;
		if(num<node.jjtGetNumChildren()) color1=acceptIfExist(node,num++,-1);
		if(num<node.jjtGetNumChildren())
			bf=((InterpreterNode)node.jjtGetChild(num++)).getToken();
		if(num<node.jjtGetNumChildren()) color2=acceptIfExist(node,num++,color1);
		else color2=color1;
		if(bf==null){
			env.getActiveScreen().line(x1, y1, isStep, x2, y2, false, color1);
		}else{
			boolean isFill=false;
			if(bf.kind==UsoBasicParserConstants.BF) isFill=true;
			env.getActiveScreen().rect(x1, y1, x2-x1, y2-y1, isFill, color1,color2);
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateLINEINPUT node, Object data){
		InterpreterNode n=(InterpreterNode)node;
		String prompt="";
		int loc=0;
		if(n.getTokenSize()==1){
			prompt=n.getToken().image;
			loc++;
		}
		VariableParam variable=
				(VariableParam)n.jjtGetChild(loc).jjtAccept(this, null);
		env.lineInput(prompt, variable);
		return (Boolean)true;
	}
	//	public Object visit(ASTstateLINEINPUT_S node, Object data){
	//		int fno=acceptLiteral(node,0).getInteger();
	//		VariableParam variable=
	//		(VariableParam)node.jjtGetChild(1).jjtAccept(this, null);
	//		env.lineInputSharp(fno, variable);
	//		return (Boolean)true;
	//	}
	public Object visit(ASTstateLOCATE node, Object data){
		int x=acceptIfExist(node,0,0);
		int y=acceptIfExist(node,1,-1);
		env.tscreen.locate(x, y);
		if(node.jjtGetNumChildren()==3){
			int sw=acceptIfExist(node,1,-1);
			if(sw==0) env.tscreen.setDisplayCusror(false);
			else env.tscreen.setDisplayCusror(true);
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateNEW node, Object data){
		env.program.clear();
		return (Boolean)true;
	}
	public Object visit(ASTstateMID node, Object data){
		int num=0;
		VariableParam variable=
				(VariableParam)node.jjtGetChild(num++).jjtAccept(this, null);
		int from=acceptIfExist(node,num++,0);
		int len=Integer.MAX_VALUE;
		if(node.jjtGetNumChildren()==4){
			len=acceptIfExist(node,num++,0);
		}
		String str=acceptIfExist(node,num,"");
		env.replace(variable, from-1, len, str);
		return (Boolean)true;
	}
	public Object visit(ASTstateONERROR node, Object data){
		int line=(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		env.onError.setLine(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateONHELP node, Object data){
		int line=(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		env.onHelp.setLine(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateONSTOP node, Object data){
		int line=(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		env.onStop.setLine(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateONGO node, Object data){
		int sw=acceptIfExist(node,0,-1)-1;
		BasicError.check(sw>=0, BasicError.ILLEGAL_FUNCTION_CALL);
		if(sw<node.jjtGetNumChildren()+1){
			int line=(Integer)node.jjtGetChild(sw+1).jjtAccept(this, null);
			Token t=((InterpreterNode)node).getToken(0);
			if(t.kind==UsoBasicParserConstants.GOSUB){
				executor.getProgramContext().gosub(line);
			}else{
				executor.getProgramContext().goTo(line);
			}
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateONKEY node, Object data){
		for(int i=0;i<node.jjtGetNumChildren();i++){
			Integer line=(Integer)node.jjtGetChild(i).jjtAccept(this, null);
			if(line!=null){
				BasicError.check(line>0, BasicError.ILLEGAL_FUNCTION_CALL);
				env.onKey[i].setLine(line);
			}
		}
		return (Boolean)true;
	}
	/* not work correctly because of antialiasing */
	//	public Object visit(ASTstatePAINT node, Object data){
	//		int x=acceptIfExist(node,0,-1);
	//		int y=acceptIfExist(node,1,-1);
	//		int surface=acceptIfExist(node,2,-1);
	//		int boundary=acceptIfExist(node,3,surface);
	//		env.getActiveScreen().paint(x, y, false, surface, boundary);
	//		return (Boolean)true;
	//	}
	public Object visit(ASTstatePOINT node, Object data){
		int x=acceptIfExist(node,0,-1);
		int y=acceptIfExist(node,1,-1);
		env.getActiveScreen().setLP(x, y);
		return (Boolean)true;
	}
	//disabled due to antialias
	//	public Object visit(ASTfuncPOINT node, Object data){
	//		int fc=acceptIfExist(node,0,-1);
	//		return env.getActiveScreen().getLP(fc);
	//	}
	public Object visit(ASTstatePRESET node, Object data){
		int x=acceptIfExist(node,0,-1);
		int y=acceptIfExist(node,1,-1);
		int color=acceptIfExist(node,2,-1);
		if(color!=-1)
			env.getActiveScreen().preset(false,x, y);
		else
			env.getActiveScreen().pset(false, x, y, color);
		return (Boolean)true;

	}
	public Object visit(ASTstatePSET node, Object data){
		int x=acceptIfExist(node,0,-1);
		int y=acceptIfExist(node,1,-1);
		int color=acceptIfExist(node,2,-1);
		env.getActiveScreen().pset(false, x, y, color);
		return (Boolean)true;

	}
	public Object visit(ASTstatePUT_AT node, Object data){
		InterpreterNode n=(InterpreterNode)node;
		int x=acceptIfExist(n,0,-1);
		int y=acceptIfExist(n,1,-1);
		VariableParam v=null;
		int kanji=-1;
		if(n.getTokenSize()==0){
			v=(VariableParam)node.jjtGetChild(2).jjtAccept(this, null);
		}else{
			kanji=acceptIfExist(node,2,-1);
		}
		GraphicScreen.PUT_MODE mode=GraphicScreen.PUT_MODE.XOR;
		if(node.jjtGetNumChildren()>3){
			InterpreterNode nn=(InterpreterNode)node.jjtGetChild(3);
			switch(nn.getToken().kind){
			case UsoBasicParserConstants.PSET:
				mode=GraphicScreen.PUT_MODE.PSET;
				break;
			case UsoBasicParserConstants.PRESET:
				mode=GraphicScreen.PUT_MODE.PRESET;
				break;
			case UsoBasicParserConstants.OR:
				mode=GraphicScreen.PUT_MODE.OR;
				break;
			case UsoBasicParserConstants.AND:
				mode=GraphicScreen.PUT_MODE.AND;
				break;
			}
		}
		int fg=acceptIfExist(node,4,-1);
		int bg=acceptIfExist(node,5,-1);
		if(v!=null)		env.getActiveScreen().putImageData(x, y, v, mode);
		else env.getActiveScreen().putString(x, y,(char)kanji+"", fg, bg, mode);
		return (Boolean)true;
	}
	public Object visit(ASTstateRANDOMIZE node, Object data){
		int seed=acceptIfExist(node,0,-1);
		if(seed!=-1){
			Literal.randomize(seed);
		}else{
			Literal.randomize();
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateRENUM node, Object data){
		int from=10;
		int old=0;
		int step=10;
		for(int i=0;i<node.jjtGetNumChildren();i++){
			InterpreterNode n=(InterpreterNode)node.jjtGetChild(i);
			int tmp=acceptIfExist(n,0,-1);
			if(n instanceof ASTrenumNew && tmp!=-1){
				from=tmp;
			}
			if(n instanceof ASTrenumOld && tmp!=-1){
				old=tmp;
			}
			if(n instanceof ASTrenumStep && tmp!=-1){
				step=tmp;
			}
		}
		env.program.renum(from, old, step);
		return (Boolean)true;
	}

	public Object visit(ASTstateRESTORE node, Object data){
		int line=0;
		if(node.jjtGetNumChildren()>0){
			line=(Integer)node.jjtGetChild(0).jjtAccept(this, null);
		}
		executor.getProgramContext().restore(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateRESUME node, Object data){
		int line=acceptIfExist(node,0,0);
		env.onError.resume(line);
		return (Boolean)true;
	}
	public Object visit(ASTstateRESUMENEXT node, Object data){
		env.onError.resumeNext();
		return (Boolean)true;
	}

	@Override
	public Object visit(ASTstateRETURN node, Object data){
		if(node.jjtGetNumChildren()==1){
			int line=(Integer)node.jjtGetChild(0).jjtAccept(this, null);
			executor.getProgramContext().resume(line);
		}else
			executor.getProgramContext().resume();
		return (Boolean)true;
	}
	public Object visit(ASTstateROLL node, Object data){
		int x= acceptIfExist(node,0,0);
		int y= acceptIfExist(node,1,0);
		boolean isY=false;
		if(node.getTokenSize()>0 
				&& node.getToken().kind==UsoBasicParserConstants.Y){
			isY=true;
		}
		env.getActiveScreen().roll(x, y, isY);
		return (Boolean)true;
	}
	public Object visit(ASTspc node, Object data){
		int num=acceptIfExist(node,0,-1)%env.tscreen.getWidth();
		if(num<0) num=0;
		StringBuilder str=new StringBuilder();
		for(int i=0;i<num;i++) str.append(' ');
		return new Literal(str.toString(),Literal.TYPE.STRING);
	}
	public Object visit(ASTtab node, Object data){
		int lastSize=0;
		if(data!=null)
			lastSize=((Literal)data).convertToString().length();
		int num=acceptIfExist(node,0,-1)%env.tscreen.getWidth();
		if(num<0) num=0;
		StringBuilder str=new StringBuilder();
		if(num<lastSize) {
			str.append('\r');
		}else{
			num=num-lastSize;
		}
		for(int i=0;i<num;i++) str.append(' ');
		return new Literal(str.toString(),Literal.TYPE.STRING);
	}
	public Object visit(ASTstateREAD node, Object data){
		Node n=node.jjtGetChild(0);
		for(int i=0;i<n.jjtGetNumChildren();i++){
			VariableParam v=(VariableParam)n.jjtGetChild(i).jjtAccept(this, null);
			String d=executor.getProgramContext().getNextData();
			try{
				switch(v.getType()){
				case INTEGER:
					env.setToVariable(v,Integer.parseInt(d));
					break;
				case SINGLE:
				case DOUBLE:
					env.setToVariable(v,Double.parseDouble(d));
					break;
				case STRING:
					env.setToVariable(v,d);
					break;
				default:
				}
			}catch(BasicError e){
				throw e;
			}catch(Exception e){
				throw new BasicError(BasicError.SYNTAX_ERROR);
			}
		}
		return (Boolean)true;
	}
	public Object visit(ASTstateSCREEN node, Object data){
		int active=acceptIfExist(node,2,-1);
		int display=acceptIfExist(node,3,-1);
		env.screen(active,display);
		return (Boolean)true;
	}
	public Object visit(ASTstateSWAP node, Object data){
		VariableParam v1=(VariableParam)node.jjtGetChild(0).jjtAccept(this, null);
		VariableParam v2=(VariableParam)node.jjtGetChild(1).jjtAccept(this, null);
		Literal l1=env.getLiteral(v1);
		Literal l2=env.getLiteral(v2);
		env.setToVariable(v2, l1);
		env.setToVariable(v1, l2);
		return (Boolean)true;
	}

	public Object visit(ASTstateSTOP node, Object data){
		throw new BasicError(BasicError.BREAK);
	}
	public Object visit(ASTstateTRON node, Object data){
		executor.tron();
		return (Boolean)true;
	}
	public Object visit(ASTstateTROFF node, Object data){
		executor.troff();
		return (Boolean)true;
	}
	public Object visit(ASTstateWHILE node, Object data){
		ASTexpression expression=(ASTexpression)node.jjtGetChild(0);
		WhileLoop w=new WhileLoop(node,expression);
		Literal l=(Literal)expression.jjtAccept(this, null);
		if(l.isTrue()){
			whileStack.add(w);
		}else{
			skipToWhile();
		}
		return (Boolean)true;
	}
	private void skipToWhile(){
		Node n;
		int whileNum=1;
		while ((n=context.getNextNode())!=null){
			if(n instanceof ASTstateWHILE){
				whileNum++;
			}
			if(n instanceof ASTstateWEND){
				if(--whileNum==0) break;
			}
		}
	}
	public Object visit(ASTstateWEND node, Object data){
		WhileLoop f=whileStack.lastElement();
		if(!f.wend()){
			whileStack.remove(f);
		}
		return (Boolean)true;
	}
	@Override
	public Object visit(ASTstateWRITE node,Object data){
		ArrayList<Literal> literals=new ArrayList<>();
		for(int i=0;i<node.jjtGetNumChildren();){
			literals.add(acceptLiteral(node,i++));
			if(i<node.jjtGetNumChildren()) i++;
		}
		String str=env.write(literals);
		env.tscreen.print(str,false);
		return (Boolean)true;

	}
	@Override
	public Object visit(ASTstateLOAD node, Object data){
		String name=acceptLiteral(node,0).getString();
		env.loadRemote(name);
		return (Boolean)true;
	}
	public Object visit(ASTstateSAVE node, Object data){
		String name=acceptLiteral(node,0).getString();
		env.saveRemote(name);
		return (Boolean)true;
	}
	public Object visit(ASTstateFILES node, Object data){
		env.filesRemote();
		return (Boolean)true;
	}
	public Object visit(ASTstateKILL node, Object data){
		String name=acceptLiteral(node,0).getString();
		env.killRemote(name);
		return (Boolean)true;
	}
	public Object visit(ASTstateNAME node, Object data){
		String from=acceptLiteral(node,0).getString();
		String to=acceptLiteral(node,1).getString();
		env.nameRemote(from,to);
		return (Boolean)true;
	}
	public Object visit(ASTstateWIDTH node, Object data){
		env.tscreen.cls();
		return (Boolean)true;
	}

}
