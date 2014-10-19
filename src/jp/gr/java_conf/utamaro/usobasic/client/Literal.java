package jp.gr.java_conf.utamaro.usobasic.client;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Int32Array;

public class Literal {
	public enum TYPE {
		INTEGER,
		SINGLE,
		DOUBLE,
		STRING,
		EMPTY
	}
	private double number=Double.NaN;
	private String string=null;
	private TYPE type=TYPE.EMPTY;

	private char[] zenkaku={'ア','イ','ウ','エ','オ','カ','キ','ク',
			'ケ','コ','サ','シ','ス','セ','ソ','タ','チ','ツ','テ','ト',
			'ナ','ニ','ヌ','ネ','ノ','ハ','ヒ','フ','ヘ','ホ','マ','ミ',
			'ム','メ','モ','ヤ','ユ','ヨ','ラ','リ','ル','レ','ロ','ワ',
			'ヲ','ン','ァ','ィ','ゥ','ェ','ォ','ャ','ュ','ョ','ッ','゛','゜'};
	private char[] hankaku={'ｱ','ｲ','ｳ','ｴ','ｵ','ｶ','ｷ','ｸ','ｹ',
			'ｺ','ｻ','ｼ','ｽ','ｾ','ｿ','ﾀ','ﾁ','ﾂ','ﾃ','ﾄ','ﾅ','ﾆ','ﾇ',
			'ﾈ','ﾉ','ﾊ','ﾋ','ﾌ','ﾍ','ﾎ','ﾏ','ﾐ','ﾑ','ﾒ','ﾓ','ﾔ','ﾕ',
			'ﾖ','ﾗ','ﾘ','ﾙ','ﾚ','ﾛ','ﾜ','ｦ','ﾝ','ｧ','ｨ','ｩ','ｪ','ｫ',
			'ｬ','ｭ','ｮ','ｯ','ﾞ','ﾟ'};

	private static Random random=new Random(0);
	private static double previousRnd=random.nextDouble();
	private static long seed=0;
	public final static Literal ZERO=new Literal(0,TYPE.INTEGER);
	public final static Literal ONE=new Literal(1,TYPE.INTEGER);
	public final static Literal MINUS_ONE=new Literal(-1,TYPE.INTEGER);
	public final static Literal EMPTY_STRING=new Literal("",TYPE.STRING);
	public final static Literal EMPTY=new Literal("",TYPE.EMPTY);

	private static Logger logger=Logger.getLogger("lite");

	public static Literal intnum(int num){
		return new Literal(num,TYPE.INTEGER);
	}

	private static  boolean is1ByteHankaku(char c){
		return (' '<=c && c <= '~') ;
	}
	private static boolean is2ByteHankaku(char c){
		return  (c==0xa5 || c==(char)0x203e 
				|| (c >= (char)0xff61 && c <= (char)0xff9f));

	}

	public static boolean isHankaku(char c){
		return (is1ByteHankaku(c) || is2ByteHankaku(c));
	}

	private void check(boolean ok){
		BasicError.check(type!=TYPE.EMPTY,BasicError.MISSING_OPERAND);
		BasicError.check(ok,BasicError.TYPE_MISMATCH);
	}

	public TYPE getType(){
		return type;
	}

	public double getDouble(){
		check (type!=TYPE.STRING);
		return number;
	}
	public int getInteger(){
		check (type!=TYPE.STRING);

		return (int)Math.round(number);
	}
	public float getISingle(){
		check (type!=TYPE.STRING);
		return new BigDecimal(number).setScale(7, BigDecimal.ROUND_HALF_UP).
				floatValue();
	}
	public String convertToString(){
		if(type==TYPE.DOUBLE || type==TYPE.SINGLE){
			return Double.toString(number);
		}
		if(type==TYPE.INTEGER){
			return Integer.toString(getInteger());
		}
		if(type==TYPE.STRING){
			return string;
		}
		return "";
	}
	public String getString(){
		check (type==TYPE.STRING);
		return string;
	}
	public Literal(String string){
		this.string=string;
		type=TYPE.STRING;
	}
	public Literal(){
	}
	public Literal(double number,TYPE type){

		this.number=number;
		this.type=type;
	}
	public Literal(String number,int radix){
		this.number=Integer.parseInt(number,radix);
		this.type=TYPE.INTEGER;
	}
	public Literal(String number,TYPE type){
		this.type=type;
		try{
		if(type==TYPE.DOUBLE || type==TYPE.SINGLE){
			this.number=Double.parseDouble(number);
		}
		if(type==TYPE.INTEGER){
			this.number=Integer.parseInt(number);
		}
		}catch(NumberFormatException e){
			throw new BasicError(BasicError.TYPE_MISMATCH);
		}
		if(type==TYPE.STRING){
			this.string=number;
		}
	}

	private TYPE determinType(Literal  l,TYPE atLeast){
		TYPE t=l.getType();
		check(type!=TYPE.STRING && t!=TYPE.STRING);
		if(type==TYPE.DOUBLE || t==TYPE.DOUBLE || atLeast==TYPE.DOUBLE)
			return TYPE.DOUBLE;
		if(type==TYPE.SINGLE|| t==TYPE.SINGLE || atLeast==TYPE.SINGLE)
			return TYPE.SINGLE;
		return TYPE.INTEGER;
	}
	private TYPE determinType(TYPE atLeast){
		check(type!=TYPE.STRING);
		if(type==TYPE.DOUBLE || atLeast==TYPE.DOUBLE) return TYPE.DOUBLE;
		if(type==TYPE.SINGLE|| atLeast==TYPE.SINGLE) return TYPE.SINGLE;
		return TYPE.INTEGER;
	}
	public Literal add(Literal l){
		if(type==TYPE.STRING && l.type==TYPE.STRING){
			return new Literal(string.concat(l.getString()),type);
		}
		check (type!=TYPE.STRING && l.type!=TYPE.STRING); 
		double r=number+l.number;
		return new Literal(r,determinType(l,null));
	}
	public Literal negative(){
		check(type!=TYPE.STRING );
		return new Literal(-number,type);
	}
	public Literal minus(Literal l){
		check(type!=TYPE.STRING && l.type!=TYPE.STRING);
		return add(l.negative());
	}
	public Literal mul(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		double r=number*l.number;
		return new Literal(r,determinType(l,null));
	}
	public Literal div(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		double r=number/l.number;
		return new Literal(r,determinType(l,TYPE.SINGLE));
	}
	public Literal idiv(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r=getInteger()/l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal mod(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r=getInteger()%l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal pow(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		double r=Math.pow(number,l.number);
		return new Literal(r,determinType(l,TYPE.SINGLE));
	}
	public Literal not(){
		check(type!=TYPE.STRING );
		return new Literal(~getInteger(),TYPE.INTEGER);
	}
	public Literal and(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r=getInteger()&l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal or(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r=getInteger()|l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal xor(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r=getInteger()^l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal imp(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		int r = (~getInteger())|l.getInteger();
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal eqv(Literal l){
		check(type!=TYPE.STRING || l.type!=TYPE.STRING);
		Literal imp0=imp(l);
		Literal imp1=l.imp(this);
		return imp0.and(imp1);
	}
	private boolean _eq(Literal l){
		if( (type!=TYPE.STRING && l.type==TYPE.STRING) 
				|| (type==TYPE.STRING && l.type!=TYPE.STRING)){
			return false;
		}
		if(type==TYPE.STRING && l.type==TYPE.STRING){
			if(getString().equals(l.getString())) return true;
			else return false;
		}else{
			if(getDouble() == l.getDouble()) return true;
			else return false;
		}
	}

	private boolean _gt(Literal l){
		if(type==TYPE.STRING && l.type==TYPE.STRING){
			if(getString().compareTo(l.getString())>0) return true;
			else return false;
		}else{
			if(getDouble() > l.getDouble()) return true;
			else return false;
		}
	}

	public Literal eq(Literal l){
		int r;
		if(_eq(l)) r=-1;else r=0;
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal gt(Literal l){
		if( (type!=TYPE.STRING && l.type==TYPE.STRING) 
				|| (type==TYPE.STRING && l.type!=TYPE.STRING)){
			return null;
		}
		int r;
		if(_gt(l)) r=-1;else r=0;
		return new Literal(r,TYPE.INTEGER);
	}

	public Literal ge(Literal l){
		int r=0;
		if(_gt(l)) r=-1;
		if(_eq(l)) r=-1;
		return new Literal(r,TYPE.INTEGER);
	}
	public Literal lt(Literal l){
		return l.gt(this);
	}
	public Literal le(Literal l){
		return l.ge(this);
	}
	public Literal neq(Literal l){
		int r=0;
		if(!_eq(l)) r=-1;
		return new Literal(r,TYPE.INTEGER);
	}
	public boolean isTrue(){
		check (type!=TYPE.STRING);
		if(number==0) return false;
		return true;
	}
	public boolean isEmpty(){
		return (type==TYPE.EMPTY);
	}
	public boolean changeType(TYPE type){
		if(type==TYPE.STRING || this.type==TYPE.STRING) return false;
		this.type=type;
		return true;
	}
	public Literal abs(){
		check(type!=TYPE.STRING );
		double r=Math.abs(number);
		return new Literal(r,determinType(TYPE.SINGLE));
	}
	public Literal akcnv(){
		check(type==TYPE.STRING );
		StringBuilder str=new StringBuilder();
		for(int i=0;i<string.length();i++){
			char c=string.charAt(i);
			if(c==' ') c=0x3000;
			if('!'<=c && c<='~') c=(char)(c-(char)0x21+(char)0xff01);
			for(int j=0;j<hankaku.length;j++){
				if(hankaku[j]==c) c=zenkaku[j];
			}
			str.append(c);
		}
		return new Literal(str.toString(),TYPE.STRING);
	}
	public Literal asc(){
		check(type==TYPE.STRING );
		return new Literal(string.charAt(0),TYPE.INTEGER);
	}
	public Literal atn(){
		check(type!=TYPE.STRING );
		return new Literal(Math.atan(number),determinType(TYPE.SINGLE));
	}
	public Literal tan(){
		check(type!=TYPE.STRING );
		return new Literal(Math.tan(number),determinType(TYPE.SINGLE));
	}
	public Literal cdbl(){
		return new Literal(number,TYPE.DOUBLE);
	}
	public Literal chr(){
		char c=(char)getInteger();
		return new Literal(c+"",TYPE.STRING);
	}
	public Literal cint(){
		check(type!=TYPE.STRING );
		return new Literal(number,TYPE.INTEGER);
	}
	public Literal sin(){
		check(type!=TYPE.STRING );
		return new Literal(Math.sin(number),determinType(TYPE.SINGLE));
	}
	public Literal log(){
		check(type!=TYPE.STRING );
		return new Literal(Math.log(number),determinType(TYPE.SINGLE));
	}
	public Literal sqr(){
		check(type!=TYPE.STRING );
		return new Literal(Math.sqrt(number),determinType(TYPE.SINGLE));
	}
	public Literal cos(){
		check(type!=TYPE.STRING );
		return new Literal(Math.cos(number),determinType(TYPE.SINGLE));
	}
	public Literal csng(){
		check(type!=TYPE.STRING );
		return new Literal(number,TYPE.SINGLE);
	}
	public Literal cvi(){
		check(type!=TYPE.INTEGER );
		StringBuilder buf=new StringBuilder();
		buf.append((char)(getInteger() &0xffff));
		buf.append((char)((getInteger()>>16) &0xffff));
		return new Literal(buf.toString(),TYPE.STRING);
	}
	public Literal cvs(){
		check(type==TYPE.SINGLE );
		//for GWT floatToRawIntBits
		Int32Array wia = Int32ArrayNative.create(1);
		Float32Array wfa = Float32ArrayNative.create(wia.buffer(), 0, 1);

		wfa.set(0,(float)number);
		char c0=(char)(wia.get(0) &0xffff);
		//		char c0=(char)(Float.floatToRawIntBits((float)number) &0xffff);
		return new Literal(""+c0,TYPE.STRING);
	}
	public Literal cvd(){
		check(type==TYPE.DOUBLE );
		//for GWT doubleToRawLongBits
		Int32Array wia = Int32ArrayNative.create(2);
		Float64Array wfa = Float64ArrayNative.create(wia.buffer(), 0, 1);

		wfa.set(0,number);
		//		long l=Double.doubleToRawLongBits(number);
		char c0=(char)(wia.get(0) &0xffff);
		char c1=(char)(wia.get(1)  &0xffff);
		return new Literal(""+c1+c0,TYPE.STRING);
	}
	public Literal mki(){
		check(type==TYPE.STRING );
		int c=((int)string.charAt(0)<<16)+(int)string.charAt(1);
		return new Literal(c,TYPE.INTEGER);
	}
	public Literal mks(){
		check(type==TYPE.STRING );
		int c=(int)string.charAt(0);

		Int32Array wia = Int32ArrayNative.create(1);
		Float32Array wfa = Float32ArrayNative.create(wia.buffer(), 0, 1);
		wia.set(0,c);
		double c0=wfa.get(0);
		//		double c0=(double)(Float.intBitsToFloat(c));
		return new Literal(c0,TYPE.SINGLE);
	}
	public Literal mkd(){
		check(type==TYPE.STRING );
		//for GWT doubleToRawLongBits
		Int32Array wia = Int32ArrayNative.create(2);
		Float64Array wfa = Float64ArrayNative.create(wia.buffer(), 0, 1);
		wia.set(0,string.charAt(0));
		wia.set(1,string.charAt(1));
		double n=wfa.get(0);
		//		long c=((long)string.charAt(1)<<16)+(long)string.charAt(0);
		//		double n=Double.longBitsToDouble(c);
		return new Literal(n,TYPE.DOUBLE);
	}
	public static Literal date(){
		 DateTimeFormat fmt = DateTimeFormat.getFormat("yy/MM/dd");
		return new Literal(fmt.format(new Date()),TYPE.STRING);
	}
	public Literal exp(){
		check(type!=TYPE.STRING );
		return new Literal(Math.exp(number),determinType(TYPE.SINGLE));
	}
	public Literal toInt(){
		check(type!=TYPE.STRING );
		return new Literal(Math.floor(number),TYPE.INTEGER);
	}
	public Literal hex(){
		check(type!=TYPE.STRING );
		return new Literal(Integer.toHexString(getInteger()),TYPE.STRING);
	}
	public Literal oct(){
		check(type!=TYPE.STRING );
		return new Literal(Integer.toString(getInteger(),8),TYPE.STRING);
	}
	public Literal instr(int loc,String target){
		check(type==TYPE.STRING );
		int i=string.indexOf(target,loc)+1;
		return new Literal(i,TYPE.INTEGER);
	}
	public Literal kinstr(int loc,String target){
		return instr(loc,target);
	}
	public Literal fix(){
		check(type!=TYPE.STRING );
		if(number>0) return toInt();
		return new Literal(Math.ceil(number),TYPE.INTEGER);
	}
	public Literal jis(){
		check(type==TYPE.STRING );
		return new Literal(Integer.toHexString(string.charAt(0)),TYPE.STRING);
	}
	public Literal kacnv(){
		check(type==TYPE.STRING );
		StringBuilder str=new StringBuilder();
		for(int i=0;i<string.length();i++){
			char c=string.charAt(i);
			for(int j=0;j<zenkaku.length;j++){
				if(zenkaku[j]==c) c=hankaku[j];
			}
			str.append(c);
		}
		return new Literal(str.toString(),TYPE.STRING);
	}
	public Literal kext(int extZenkaku){
		check(type==TYPE.STRING );
		BasicError.check((extZenkaku==0 || extZenkaku==1),
				BasicError.ILLEGAL_FUNCTION_CALL );
		StringBuilder str=new StringBuilder();
		for(int i=0;i<string.length();i++){
			char c=string.charAt(i);
			if(isHankaku(c) && extZenkaku==0) str.append(c); 
			if(!isHankaku(c) && extZenkaku==1) str.append(c); 
		}
		return new Literal(str.toString(),TYPE.STRING);
	}
	public static Literal time(){
		 DateTimeFormat fmt = DateTimeFormat.getFormat("HH:mm:ss");
		return new Literal(fmt.format(new Date()),TYPE.STRING);
	}
	public static Literal string(int num,char c){
		StringBuilder str=new StringBuilder();
		for(int i=0;i<num;i++) str.append(c);
		return new Literal(str.toString(),TYPE.STRING);
	}
	public Literal str(){
		check(type!=TYPE.STRING );
		String prefix="";
		if(number>0) prefix=" ";
		if(type==TYPE.INTEGER){
			return new Literal(prefix+Integer.toString(getInteger()),
					TYPE.STRING);
		}
		if(type==TYPE.DOUBLE){
			return new Literal(prefix+Double.toString(getInteger()),
					TYPE.STRING);
		}
		if(type==TYPE.SINGLE){
			return new Literal(prefix+Float.toString(getInteger()),
					TYPE.STRING);
		}
		return null;
	}
	public static Literal space(int num){
		return string(num,' ');
	}
	public Literal sgn(){
		check(type!=TYPE.STRING );
		if(number>0)	return Literal.ONE;
		if(number==0)	return Literal.ZERO;
		return Literal.MINUS_ONE;
	}
	public static void randomize(){
		randomize(new Date().getTime());
	}

	public static void randomize(long seed){
		Literal.seed=seed;
		random=new Random(seed);
		previousRnd=random.nextDouble();
	}
	public static Literal rnd(int func){
		if(func<0) randomize(Literal.seed); 
		if(func>0) previousRnd=random.nextDouble();
		return new Literal(previousRnd,TYPE.SINGLE);
	}
	public Literal left(int num){
		check(type==TYPE.STRING );
		return new Literal(string.substring(0,num),TYPE.STRING);
	}
	public Literal mid(int from,int size){
		from--;
		check(type==TYPE.STRING );
		if(size<0) size=string.length()-from;
		return new Literal(string.substring(from,from+size),TYPE.STRING);
	}
	public Literal mid(Literal orig,Literal from,Literal to,Literal replace){
		String o=orig.getString();
		String r=replace.getString();
		int f=from.getInteger();
		int t=to.getInteger();
		String result=o.substring(0,f)
				+r.substring(0,t-f)+o.substring(t,o.length());
		return new Literal(result,Literal.TYPE.STRING);
	}
	public Literal kmid(int from,int size){
		return mid(from,size);
	}

	public Literal right(int num){
		check(type==TYPE.STRING );
		return mid(string.length()-num+1,num);
	}
	public Literal len(){
		check(type==TYPE.STRING );
		return new Literal(string.length(),TYPE.INTEGER);
	}
	public Literal klen(int func){
		check(type==TYPE.STRING );
		switch(func){
		case 0:
			return len();
		case 1:
		{
			int count=0;
			for(int i=0;i<string.length();i++)
				if(is1ByteHankaku(string.charAt(i))) count++;
			return new Literal(count,TYPE.INTEGER);
		}
		case 2:
		{
			int count=0;
			for(int i=0;i<string.length();i++)
				if(!is1ByteHankaku(string.charAt(i))) count++;
			return new Literal(count,TYPE.INTEGER);
		}
		case 3:
		{
			int count=0;
			for(int i=0;i<string.length();i++)
				if(is2ByteHankaku(string.charAt(i))) count++;
			return new Literal(count,TYPE.INTEGER);
		}
		case 4:
		{
			int count=0;
			for(int i=0;i<string.length();i++){
				char c=string.charAt(i);
				if(!is1ByteHankaku(c) && !is2ByteHankaku(c)) count++;
			}
			return new Literal(count,TYPE.INTEGER);
		}
		case 5:
			return Literal.ZERO;
		default:
			throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
		}
	}
	public Literal knj(){
		check(type==TYPE.STRING );
		int c=Integer.parseInt(string.substring(3),16);
		return new Literal(String.valueOf(c),TYPE.INTEGER);
	}
	public Literal ktype(int loc){
		char c=string.charAt(loc);
		if(isHankaku(c)) return Literal.ZERO;
		if(is2ByteHankaku(c)) return intnum(2);
		return Literal.ONE;
	}

	enum MODE {
		FIRST,DOT,EXP_FIRST,EXP,OTHER,AND
	};
	public Literal val(){
		check(type==TYPE.STRING );
		string.replaceAll(" ","");

		MODE mode=MODE.FIRST;
		TYPE type=TYPE.INTEGER;
		int radix=10;
		int count=0;
		double n=0;
		int exp=0;
		int neg=1;
		int exp_neg=1;
		for(int i=0;i<string.length();i++){
			char c=string.charAt(i);
			boolean ok=false;
			if(radix==10 && c=='!'){
				count=-1;
				type=TYPE.SINGLE;
				break;
			}
			if(radix==10 && c=='#'){
				count=-1;
				type=TYPE.DOUBLE;
				break;
			}
			if(radix==10 && c=='%'){
				count=-1;
				type=TYPE.INTEGER;
				break;
			}
			if(mode==MODE.FIRST && c=='+' ){
				mode=MODE.OTHER;
				ok=true;
			}
			if(mode==MODE.FIRST &&   c=='-' ){
				neg=-1;
				mode=MODE.OTHER;
				ok=true;
			}
			if(mode==MODE.FIRST && c=='&' ){
				mode=MODE.AND;
				ok=true;
			}
			if(mode==MODE.FIRST && c=='.' ){
				mode=MODE.DOT;
				type=TYPE.SINGLE;
				ok=true;
			}
			if(mode==MODE.AND && (c=='H' || c=='h') ){
				mode=MODE.OTHER;
				radix=16;
				ok=true;
			}
			if(mode==MODE.AND && (c=='O' || c=='o') ){
				mode=MODE.OTHER;
				radix=8;
				ok=true;
			}
			if(mode==MODE.AND && ('0'<=c && c<='7') ){
				mode=MODE.OTHER;
				radix=8;
				n=c-'0';
				ok=true;
			}
			if(mode==MODE.DOT && ('0'<=c && c<='9') ){
				if(++count>7) type=TYPE.DOUBLE;
				n+=(c-'0')*0.1*count;
				ok=true;
			}
			if(mode==MODE.OTHER && c=='.' ){
				mode=MODE.DOT;
				type=TYPE.SINGLE;
				ok=true;
			}
			if(mode==MODE.OTHER && radix==10 && ('0'<=c && c<='9') ){
				n=n*10+(c-'0');
				ok=true;
			}
			if((mode==MODE.DOT || mode==MODE.OTHER) && radix==10 
					&& (c=='D' ||  c=='d') ){
				mode=MODE.EXP_FIRST;
				type=TYPE.DOUBLE;
				ok=true;
			}
			if((mode==MODE.DOT || mode==MODE.OTHER) && radix==10 
					&& (c=='E' ||  c=='e') ){
				mode=MODE.EXP_FIRST;
				type=TYPE.SINGLE;
				ok=true;
			}
			if(mode==MODE.EXP_FIRST && c=='+' ){
				mode=MODE.EXP;
				ok=true;
			}
			if(mode==MODE.EXP_FIRST && c=='-'){
				mode=MODE.EXP;
				exp_neg=-1;
				ok=true;
			}
			if((mode==MODE.EXP || mode==MODE.EXP_FIRST) 
					&&  ('0'<=c && c<='9') ){
				mode=MODE.EXP;
				exp=exp*10+c-'0';
				ok=true;
			}
			if(mode==MODE.OTHER && radix==8 && ('0'<=c && c<='7') ){
				n=n*radix+c-'0';
				ok=true;
			}
			if(mode==MODE.FIRST && '0'<=c && c<='9'){
				mode=MODE.OTHER;
				n=n*radix+c-'0';
				ok=true;
			}
			if(mode==MODE.OTHER && radix==16 && '0'<=c && c<='9'){
				n=n*radix+c-'0';
				ok=true;
			}
			if(mode==MODE.OTHER && radix==16 && 'a'<=c && c<='f'){
				n=n*radix+c-'a'+10;
				ok=true;
			}
			if(mode==MODE.OTHER && radix==16 && 'A'<=c && c<='F'){
				n=n*radix+c-'A'+10;
				ok=true;
			}
			if(!ok) break;
		}
		double n1=n*neg*Math.pow(10, exp_neg*exp);
		return new Literal(n1,type);
	}
}
