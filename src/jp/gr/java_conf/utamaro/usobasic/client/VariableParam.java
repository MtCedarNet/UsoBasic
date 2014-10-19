package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.logging.Logger;

public class VariableParam{
	private String name;
	private ArrayList<Literal> arguments=null;
	private static  Literal.TYPE deftype[]=new Literal.TYPE['z'-'a'+1];
	static{
		initDeftype();
	}
	private Logger logger =Logger.getLogger("vparam");

	public int hashCode(){
		return name.hashCode();
	}

	public boolean equals(Object obj){
		if(obj instanceof VariableParam){
			VariableParam v=(VariableParam)obj;
			if(v.name.equals(name)){
				if(v.arguments==null && arguments==null) return true;
				if(v.arguments.size()==arguments.size()){
					for(int i=0;i<arguments.size();i++){
						if(!arguments.get(i).eq(v.arguments.get(i)).isTrue()){
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public static void setDeftype(char c,Literal.TYPE type){
		deftype[c-'a']=type;
	}

	public static void initDeftype(){
		for(int i=0;i<deftype.length;i++) deftype[i]=Literal.TYPE.SINGLE;
	}

	public boolean isArray(){
		return arguments!=null;
	}

	public VariableParam(String name,ArrayList<Literal> arguments){
		this.name=appendType(name);
		this.arguments=arguments;
	}
	public ArrayList<Literal> getArguments(){
		return arguments;
	}
	public String getName(){
		return name;
	}

	public ArrayList<Integer> getIndex(){
		ArrayList<Integer> index=new ArrayList<>();
		for(Literal l:arguments){
			index.add(l.getInteger());
		}
		return index;
	}
	public VariableParam(String name){
		this.name=appendType(name);
	}
	public Literal defaultLiteral(){
		Literal l=null;
		if(name.endsWith("$")){
			l=Literal.EMPTY_STRING;
		}
		else{
			l=new Literal(0,getType());
		}
		return l;
	}

	private static String appendType(String var){
		var=var.toLowerCase();
		if(var.charAt(var.length()-1)=='$' ||
				var.charAt(var.length()-1)=='!' ||
				var.charAt(var.length()-1)=='#' ||
				var.charAt(var.length()-1)=='%' ) return var;
		char start=var.charAt(0);
		switch(deftype[start-'a']){
		case STRING:
			return var+"$";
		case INTEGER:
			return var+"%";
		case SINGLE:
			return var+"!";
		case DOUBLE:
			return var+"#";
		default:
			return var;
		}
	}

	public Literal.TYPE getType(){
		Literal.TYPE type;
		char end=name.charAt(name.length()-1);
		switch(end){
		case '$':
			type=Literal.TYPE.STRING;
			break;
		case '!':
			type=Literal.TYPE.SINGLE;
			break;
		case '#':
			type=Literal.TYPE.DOUBLE;
			break;
		case '%':
			type=Literal.TYPE.INTEGER;
			break;
		default:
			char start=java.lang.Character.toLowerCase(name.charAt(0));
			type=deftype[start-'a'];
			break;
		}
		return type;
	}

}
