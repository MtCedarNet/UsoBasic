package jp.gr.java_conf.utamaro.usobasic.client;

public class ListNode<T>{
	public T value;
	public ListNode<T> next;
	public ListNode<T> previous;

	public ListNode (T value){
		this.value=value;
		next=null;
		previous=null;
	}
	public ListNode<T> addToNext(T value){
		ListNode<T> n=new ListNode<>(value);
		ListNode<T> tmp=next;
		next=n;
		n.next=tmp;
		n.previous=this;
		return n;
	}
	public void remove(){
		previous.next=next;
		next.previous=previous;
	}
}
