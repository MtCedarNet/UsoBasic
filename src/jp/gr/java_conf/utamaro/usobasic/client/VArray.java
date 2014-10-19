package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.ArrayList;
import java.util.logging.Logger;

public class VArray {
	private ArrayList<Integer> max_index;
	private Literal[] literals;
	private Logger logger = Logger.getLogger("varray");

	public VArray(ArrayList<Integer> max_index){
		this.max_index=max_index;
		literals=new Literal[getIndex(max_index)];
	}
	public VArray(){
		super();
		this.max_index=new ArrayList<>();
		max_index.add(10);
		literals=new Literal[getIndex(max_index)];
	}
	private int getIndex(ArrayList<Integer> index){
		int sum=0;
		for(int i=0;i<index.size();i++){
			BasicError.check(index.get(i)<=max_index.get(i),
					BasicError.SUBSCRIPT_OUT_OF_RANGE);
			sum+=max_index.get(i)*index.get(i);
		}
		return sum;
	}
	public Literal get(int... v){
		ArrayList<Integer> in=new ArrayList<>();
		for(int i:v){
			in.add(i);
		}
		return get(in);
	}
	public Literal get(ArrayList<Integer> index){
		return literals[getIndex(index)];
	}
	public void set(Literal l,int... v){
		ArrayList<Integer> in=new ArrayList<>();
		for(int i:v){
			in.add(i);
		}
		set(in,l);
	}
	public void set(ArrayList<Integer> index,Literal l){
		literals[getIndex(index)]=l;
	}
	public ArrayList<Integer> getMaxIndex(){
		return max_index;
	}
}
