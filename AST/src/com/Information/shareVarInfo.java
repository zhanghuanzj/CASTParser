package com.Information;

public class ShareVarInfo {
	private int lineNumber;
	private String type;
	private String path;
	
	public ShareVarInfo(int lineNumber, String type,String path) {
		super();
		this.lineNumber = lineNumber;
		this.type = type;
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
	
	@Override
	public String toString() {
		return "PATH: "+path+"\nLINENUMBER: "+lineNumber+"\nTYPE: "+type+"\n";
	}
	
}
