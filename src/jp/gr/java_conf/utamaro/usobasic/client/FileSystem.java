package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.HashMap;

import com.google.gwt.storage.client.Storage;

public class FileSystem {
	public enum TYPE{
		INPUT,OUTPUT,APPEND,RANDOM
	}

	public class File{
		private StringBuilder content=new StringBuilder();
		private int loc=0;
		private TYPE type=null;
		private String name;

		private void setName(String name){
			this.name=name;
		}

		public String getName(){
			return name;
		}
		private  File(String name){
			this.name=name;
		}
		private File(String name,String content){
			this(name);
			this.content=new StringBuilder(content);
		}
		public void open(TYPE type){
			this.type=type;
		}
		public int size(){
			return content.length();
		}

		public String getContent(){
			return content.toString();
		}

		public String getContent(int size){
			BasicError.check(type==TYPE.INPUT && type==TYPE.RANDOM, 
					BasicError.ILLEGAL_OPERATION);
			BasicError.check(loc<content.length(), BasicError.INPUT_PAST_END);
			String str=content.substring(loc, loc+size);
			loc+=size;
			return str;
		}
		public boolean appendRecord(String s){
			BasicError.check(type==TYPE.RANDOM,BasicError.ILLEGAL_OPERATION);
			StringBuilder t=new StringBuilder();
			for(int i=s.length();i<255;i++) t.append(" ");
			append(s+t.toString());
			return true;
		}
		public boolean append(String s){
			BasicError.check(type==TYPE.OUTPUT || type==TYPE.APPEND  
					&& type==TYPE.RANDOM, BasicError.ILLEGAL_OPERATION);
			content.insert(loc,s);
			loc+=s.length();
			return true;
		}
		public void setLoc(int loc){
			this.loc=loc;
		}
		public int getLoc(){
			if(loc==0) return 0;
			return (loc>>8)+1;
		}
		public boolean eof(){
			BasicError.check(type==TYPE.INPUT,BasicError.ILLEGAL_OPERATION);
			return loc==content.length();
		}
		public boolean close(){
			Storage stockStore = Storage.getLocalStorageIfSupported();
			if (stockStore != null) {
				stockStore.setItem(name,content.toString());
				return true;
			}
			return false;
		}
		public Literal input(Literal.TYPE type){
			BasicError.check(loc<content.length(), BasicError.INPUT_PAST_END);
			int end;
			if(type==Literal.TYPE.STRING){
				end=Math.min(content.indexOf(",",loc),
						content.indexOf("\r",loc));
			}else{
				end=Math.min(content.indexOf(",",loc),
						content.indexOf("\r",loc));
				end=Math.min(content.indexOf(" ",loc),end);
				end=Math.min(content.indexOf("\t",loc),end);
			}
			Literal l=new Literal(content.substring(loc,end),type);
			loc=end;
			return l;
		}
	}



	public final static int MAX_SIZE=2_000_000;

	private HashMap<String ,File> files=new HashMap<>();
	private static FileSystem system=null;

	private FileSystem(){};
	public static FileSystem getFileSystem(){
		if(system==null){
			system=new FileSystem();
			system.loadAll();
		}
		return system;
	}

	public void remove(File f){
		files.remove(f.getName());
		Storage stockStore = Storage.getLocalStorageIfSupported();
		if(stockStore!=null)	stockStore.removeItem(f.getName());
	}
	public void rename(File f,String name){
		f.setName(name);
		files.remove(f.getName());
		files.put(name,f);
	}

	private boolean loadAll(){
		Storage stockStore = Storage.getLocalStorageIfSupported();
		if (stockStore != null){
			for (int i = 0; i < stockStore.getLength(); i++){
				String name = stockStore.key(i);
				files.put(name,new File(stockStore.getItem(name)));
			}
			return true;
		}
		return false;
	}

	public void save(File f,String name){
		String cont=files.get(name).getContent();
		BasicError.check(cont.length()>remain(),BasicError.DISK_FULL);
		files.put(name,f);
	}
	public boolean saveAll(){
		Storage stockStore = Storage.getLocalStorageIfSupported();
		if (stockStore != null) {
			for(String name:files.keySet()){
				String cont=files.get(name).getContent();
				stockStore.setItem(name, cont);
			}
			return true;
		}
		return false;
	}

	public File searchFile(String name){
		return files.get(name);
	}


	public File getFile(String name){
		File f=files.get(name);
		if(f==null){
			f=new File(name);
			files.put(name,f);
		}
		return f;
	}
	public File newFile(String name,String cont){
		File f=new File(cont);
		files.put(name,f);
		return f;
	}

	public HashMap<String,File> flist(){
		return files;
	}

	public int size(){
		int size=0;
		for(String name:files.keySet())
			size+=files.get(name).size();
		return size;
	}

	public int remain(){
		return MAX_SIZE-size();
	}

}
