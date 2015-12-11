package com.iseu.MDGHandle.Nodes;

public class Node {
	private String methodName;
	private String fileName;
	private int lineNumber;
	private String line;


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

	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public Node(String methodName, String fileName, int lineNumber) {
		super();
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	public Node(String fileName, String methodName, String line) {
		super();
		this.methodName = methodName;
		this.fileName = fileName;
		this.line = line;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + lineNumber;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "FilePath: "+fileName+"\nLineNumber: "+lineNumber;
	}
}
