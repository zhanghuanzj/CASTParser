package com.MDGHandle.Nodes;

public class Node {
	private String fileName;
	private int lineNumber;

	public Node(String fileName, int lineNumber) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	@Override
	public String toString() {
		return "FilePath: "+fileName+"\nLineNumber: "+lineNumber;
	}
}
