package com.Information;

public class shareVarInfo {
	private int lineNumber;
	private String type;
	
	public shareVarInfo(int lineNumber, String type) {
		super();
		this.lineNumber = lineNumber;
		this.type = type;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
