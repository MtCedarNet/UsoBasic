package jp.gr.java_conf.utamaro.usobasic.client;

public class Point{
	public int x;
	public int y;
	public Point(int x,int y){
		this.x=x;
		this.y=y;
	}
	public void set(int x,int y){
		this.x=x;
		this.y=y;
	}

	@Override
	public boolean equals(Object o){
		if(o==null || !(o instanceof Point)) return false;
		Point p=(Point)o;
		return isLocatedAt(p.x,p.y);
	}
	public void toOrigin(){
		x=y=0;
	}
	public boolean isOrigin(){
		return (x==0 && y==0);
	}
	public boolean isLocatedAt(int x,int y){
		return (this.x==x && this.y==y);
	}
}