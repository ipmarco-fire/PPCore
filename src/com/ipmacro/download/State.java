package com.ipmacro.download;

public class State {
	int id;
	int progress;
	int rate;
	String source;
	
	public State(){
		
	}
	
	public State(int id,int progress,int rate,String source){
		this.id = id;
		this.progress = progress;
		this.rate = rate;
		this.source = source;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	
}
