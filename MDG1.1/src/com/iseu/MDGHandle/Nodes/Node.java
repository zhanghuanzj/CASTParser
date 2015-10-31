package com.iseu.MDGHandle.Nodes;

public class Node {
	private String methodName;
	private String fileName;
	private int lineNumber;

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Node(String methodName, String fileName, int lineNumber) {
		super();
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	@Override
	public String toString() {
		return "FilePath: "+fileName+"\nLineNumber: "+lineNumber;
	}
}
