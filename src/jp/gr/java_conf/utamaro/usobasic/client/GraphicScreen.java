package jp.gr.java_conf.utamaro.usobasic.client;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;

public class GraphicScreen {

	private static class Color{
		public int r;
		public int g;
		public int b;

		public String toString(){
			return "rgb("+r+","+g+","+b+")";
		}
		public Color(int c){
			g=((c&0xf00)>>8)*17;
			r=((c&0x0f0)>>4)*17;
			b=((c&0x00f))*17;
		}
		public boolean equals(Object obj){
			if(!(obj instanceof Color)) return false;
			Color c=(Color)obj;
			return c.r==r && c.g==g && c.b==b;
		}
//		public Color(ImageData idata,int x,int y){
//			r=idata.getRedAt(x, y);
//			g=idata.getGreenAt(x, y);
//			b=idata.getBlueAt(x, y);
//		}
//		public void adaptTo(ImageData idata,int x,int y){
//			idata.setRedAt(r, x, y);
//			idata.setGreenAt(g, x, y);
//			idata.setBlueAt(b, x, y);
//		}
	}


	public final static int CANVAS_NUMBER=2;
	public final static int COLOR_NUMBER=16;

	private Canvas canvas;
	private Context2d ctx;
	public final static int  WIDTH=640;
	public final static int HEIGHT=400;
	private Point lp=new Point(0,0);
	private static Color[] colors=new Color[COLOR_NUMBER];
	private static int bgcolor=0;
	private static  int fgcolor=7;
	private static HashMap<VariableParam,ImageData> capture=new HashMap<>();
	private Logger logger = Logger.getLogger("gscreen");
	static{
		initColor();
	}


	public enum PUT_MODE{
		PSET,PRESET,OR,AND,XOR
	}

	public static void initColor(){
		colors[0]=new Color(0x0);
		colors[1]=new Color(0xf);
		colors[2]=new Color(0xf0);
		colors[3]=new Color(0xff);
		colors[4]=new Color(0xf00);
		colors[5]=new Color(0xf0f);
		colors[6]=new Color(0xff0);
		colors[7]=new Color(0xfff);
		colors[8]=new Color(0x777);
		colors[9]=new Color(0x00a);
		colors[10]=new Color(0x0a0);
		colors[11]=new Color(0x0aa);
		colors[12]=new Color(0xa00);
		colors[13]=new Color(0xa0a);
		colors[14]=new Color(0xaa0);
		colors[15]=new Color(0xaaa);
	}


	public GraphicScreen(){
		canvas=Canvas.createIfSupported();
		canvas.setWidth(WIDTH+"px");
		canvas.setHeight(HEIGHT+"px");
		canvas.setCoordinateSpaceWidth(WIDTH);
		canvas.setCoordinateSpaceHeight(HEIGHT);
		ctx=canvas.getContext2d();
		cls();
	}

	public void hide(){
		canvas.setVisible(false);
	}
	public void show(){
		canvas.setVisible(true);
	}

	public void putString(int x,int y,String str,int fg,int bg,PUT_MODE mode){
		if(fg<0) fg=fgcolor;
		if(bg<0) bg=bgcolor;
		BasicError.check(fg<colors.length && bg<colors.length, BasicError.ILLEGAL_FUNCTION_CALL);
		Context2d c=new GraphicScreen().ctx;
		c.beginPath();
		c.setFillStyle(colors[bg].toString());
		c.fillRect(0,0,WIDTH,HEIGHT);
		c.setFont("16px 'ricty'");
		c.setFillStyle(colors[fg].toString());
		c.fillText(str, 0,16);

		ImageData from=c.getImageData(0,0,16*str.length(),16);
		ImageData to= ctx.getImageData(x,y,16*str.length(),16);
		ImageData result=null;
		if(mode==PUT_MODE.PSET) result=from;
		else{
			if(mode==PUT_MODE.PRESET) result=preset(from);
			else result=bitOperate(from,to,mode);
		}
		ctx.putImageData(result, x, y);
		lp.set(x,y);
	}

	public void getImageData(int x1,int y1,int x2,int y2,VariableParam name	){
		capture.put(name, ctx.getImageData(x1,y1,x2-x1,y2-y1));
	}
	public void putImageData(int x,int y,VariableParam name	,PUT_MODE mode){
		ImageData idata=capture.get(name);
		BasicError.check(idata!=null, BasicError.ILLEGAL_OPERATION);
		ImageData to= ctx.getImageData(x,y,idata.getWidth(),idata.getHeight());
		ImageData result=null;
		if(mode==PUT_MODE.PSET) result=idata;
		else{
			if(mode==PUT_MODE.PRESET) result=preset(idata);
			else result=bitOperate(idata,to,mode);
		}
		ctx.putImageData(result, x, y);
		lp.set(x, y);
	}
	private int ope(int a,int b,PUT_MODE mode){
		if(mode==PUT_MODE.OR) return a|b;
		if(mode==PUT_MODE.AND) return a&b;
		return a^b;
	}


	private ImageData bitOperate(ImageData idata,ImageData to,PUT_MODE mode){
		ImageData result=ctx.createImageData(idata.getWidth(),
				idata.getHeight());
		for(int x=0;x<idata.getWidth();x++){
			for(int y=0;y<idata.getHeight();y++){
				int r=ope(idata.getRedAt(x, y),to.getRedAt(x, y),mode);
				int g=ope(idata.getGreenAt(x, y),to.getGreenAt(x, y),mode);
				int b=ope(idata.getBlueAt(x, y),to.getBlueAt(x, y),mode);
				result.setAlphaAt(idata.getAlphaAt(x,y), x, y);
				result.setRedAt(r, x, y);
				result.setGreenAt(g, x, y);
				result.setBlueAt(b, x, y);
			}
		}
		return result;
	}
	private ImageData preset(ImageData idata){
		ImageData result=ctx.createImageData(idata.getWidth(),
				idata.getHeight());
		for(int x=0;x<idata.getWidth();x++){
			for(int y=0;y<idata.getHeight();y++){
				int r=255-idata.getRedAt(x, y);
				int g=255-idata.getGreenAt(x, y);
				int b=255-idata.getBlueAt(x, y);
				result.setAlphaAt(idata.getAlphaAt(x,y), x, y);
				result.setRedAt(r, x, y);
				result.setGreenAt(g, x, y);
				result.setBlueAt(b, x, y);
			}
		}
		return result;
	}

	public void roll(int x,int y,boolean paintBGColor){
		ImageData result=ctx.getImageData(0,0,WIDTH, HEIGHT);
		ctx.putImageData(result, x, y);
		if(paintBGColor){
			ctx.setFillStyle(colors[bgcolor].toString());
		}else{
			ctx.setFillStyle(colors[0].toString());
		}
		ctx.fillRect(0,0,x,HEIGHT);
		ctx.fillRect(0,0,WIDTH,y);
	}


	public Canvas getCanvas(){
		return canvas;
	}

	public Literal getLP(int func){
		switch(func){
		case 0 :
		case 2:
			return new Literal(lp.x,Literal.TYPE.INTEGER);
		case 1:
		case 3:
			return new Literal(lp.y,Literal.TYPE.INTEGER);
		default:
			throw new BasicError(BasicError.ILLEGAL_FUNCTION_CALL);
		}
	}

	public void cls(){
		ctx.setFillStyle(colors[bgcolor].toString());
		ctx.fillRect(0,0,WIDTH,HEIGHT);
	}
	public static void setFGColor(int c){
		fgcolor=c;
	}

	public static void setBGColor(int c){
		bgcolor=c;
	}
	public void  setLP(int x,int y){
		lp.set(x,y);
	}

	public void circle(boolean isStep,int x,int y,int r,
			int color1,double start_angle,double end_angle,
			double ratio,boolean isF,int color2){
		BasicError.check(r>0 && color1<colors.length && color2<colors.length,
				BasicError.ILLEGAL_FUNCTION_CALL);

		if(color1<0) color1=fgcolor;
		if(isStep){
			x+=lp.x;
			y+=lp.y;
		}
		if(color2==-1) color2=color1;

		ctx.save();
		ctx.beginPath();
		if(ratio>1)	ctx.scale(1,ratio);
		else ctx.scale(ratio,1);
		ctx.setStrokeStyle(colors[color1].toString());

		ctx.arc(x,y,r,2*Math.PI-Math.abs(start_angle),
				2*Math.PI-Math.abs(end_angle),true);
		if(start_angle<0 || end_angle<0) ctx.lineTo(x,y);
		ctx.closePath();
		if(isF){
			ctx.setFillStyle(colors[color2].toString()); 
			ctx.fill();
		}
		ctx.stroke();
		ctx.restore();
		lp.set(x,y);
	}
	public void preset(boolean isStep,int x,int y){
		if(isStep){
			x+=lp.x;
			y+=lp.y;
		}
		rect(x,y,1,1,true,bgcolor,bgcolor);
		lp.set(x,y);
	}
	public void pset(boolean isStep,int x,int y,int color){
		if(color<0) color=fgcolor;
		if(isStep){
			x+=lp.x;
			y+=lp.y;
		}
		rect(x,y,1,1,true,color,color);
		lp.set(x,y);
	}

	public void  setColor(int p,int c){
		BasicError.check(p<COLOR_NUMBER, BasicError.ILLEGAL_FUNCTION_CALL);
		colors[p]=new Color(c);
	}
	public void line(int fromX,int fromY,boolean isFromStep,int toX,
			int toY,boolean isToStep,int color) {
		BasicError.check(color<colors.length,BasicError.ILLEGAL_FUNCTION_CALL);

		if(color<0) color=fgcolor;
		if(isFromStep){
			fromX+=lp.x;
			fromY+=lp.y;
		}
		if(isToStep){
			toX+=lp.x;
			toY+=lp.y;
		}
		ctx.beginPath();
		ctx.setStrokeStyle(colors[color].toString()); 
		ctx.moveTo(fromX, fromY);
		ctx.lineTo(toX,toY);
		ctx.stroke();
		lp.set(toX,toY);
	}
	public void rect(int x,int y,int width,int height,boolean fill,
			int color_stroke,int color_fill) {
		BasicError.check(color_stroke<colors.length && color_fill<colors.length,
				BasicError.ILLEGAL_FUNCTION_CALL);

		if(color_stroke<0) color_stroke=fgcolor;
		if(color_fill<0) color_fill=color_stroke;
		ctx.beginPath();
		ctx.setStrokeStyle(colors[color_stroke].toString()); 
		ctx.setFillStyle(colors[color_fill].toString()); 
		if(fill) ctx.fillRect(x,y,width,height);
		else ctx.strokeRect(x,y,width,height);
		lp.set(x+width,y+width);
	}


	//	private void scanH(ImageData idata,Color surface,Color boundary,
	//			Stack<Point>stack,boolean isUP){
	//		while(stack.size()>0){
	//			Point p=stack.pop();
	//			int x=p.x;
	//			int y=p.y;
	//			int right=idata.getWidth();
	//			int left=0;
	//			for(int x0=x;x0<idata.getWidth()-1;x0++){
	//				surface.adaptTo(idata, x0, y);
	//				Color pt=new Color(idata,x0+1,y);
	//				if(pt.equals(boundary)){
	//					right=x0;
	//					break;
	//				}
	//			}
	//			for(int x0=x;x0>0;x0--){
	//				surface.adaptTo(idata, x0, y);
	//				Color pt=new Color(idata,x0-1,y);
	//				if(pt.equals(boundary)){
	//					left=x0;
	//					break;
	//				}
	//			}
	//			if(y<HEIGHT-1 && !isUP) scanLine(idata,right,left,y+1,boundary,stack);
	//			if(y>0 && isUP) scanLine(idata,right,left,y-1,boundary,stack);
	//		}
	//	}

	/*not work correctly due to antialiasing*/
	//	public void paint(int x,int y,boolean isStep,int surface,int boundary) {
	//		Stack<Point> stack=new Stack<>();
	//		if(surface<0) surface=fgcolor;
	//		if(boundary<0) boundary=surface;
	//		if(isStep){
	//			x+=lp.x;
	//			y+=lp.y;
	//		}
	//		ImageData idata= ctx.getImageData(0,0,WIDTH,HEIGHT);
	//		Color pt=new Color(idata,x,y);
	//		if(!pt.equals(boundary)){
	//			stack.push(new Point(x,y));
	//			scanH(idata,colors[surface],colors[boundary],stack,true);
	//			stack.push(new Point(x,y+1));
	//			scanH(idata,colors[surface],colors[boundary],stack,false);
	//		}
	//		ctx.putImageData(idata, 0, 0);
	//		lp.set(x,y);
	//	}

	/*not work correctly due to antialiasing*/
	//	public int colorAt(int x,int y){
	//		ImageData idata= ctx.getImageData(x,y,1,1);
	//		Color c=new Color(idata,0,0);
	//		for(int i=0;i<colors.length;i++){
	//			if(colors[i].equals(c)) return i;
	//		}
	//		return -1;
	//	}
}
