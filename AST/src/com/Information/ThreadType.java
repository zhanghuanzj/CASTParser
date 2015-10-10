package com.Information;

public enum ThreadType {
	THREAD("THREAD"),
	RUNNABLE("RUNNABLE"),
	CALLABLE("CALLABLE"),
	RECURSIVETASK("RECURSIVETASK"),
	RECURSIVEACTION("RECURSIVEACTION"),
	MAIN("MAIN");
	private String type;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	private ThreadType(String type) {
		this.type = type;
	}
}
