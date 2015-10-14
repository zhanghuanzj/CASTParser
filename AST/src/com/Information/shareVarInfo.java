package com.Information;

public class ShareVarInfo {
	//用于节点定位的路径与行号
	private int lineNumber;
	private String path;
	//用于类型判断的记录
	private String type;
	//属于哪个函数
	private String belongMethod;
	//是否为原始变量
	private boolean isPrimitive;
	//原始变量定义位置
	private String belongClass;
	private int classLineNumber;
	
	
	public ShareVarInfo(int lineNumber, String type,String path,String belongMethod) {
		super();
		this.lineNumber = lineNumber;
		this.type = type;
		this.path = path;
		this.belongMethod = belongMethod;
		this.isPrimitive = false;
		this.belongClass = null;
		this.classLineNumber = 0;
	}
	
	public String getBelongMethod() {
		return belongMethod;
	}

	public void setBelongMethod(String belongMethod) {
		this.belongMethod = belongMethod;
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
	
	public boolean isPrimitive() {
		return isPrimitive;
	}

	public void setPrimitive(boolean isPrimitive) {
		this.isPrimitive = isPrimitive;
	}

	public String getBelongClass() {
		return belongClass;
	}

	public void setBelongClass(String belongClass) {
		this.belongClass = belongClass;
	}

	public int getClassLineNumber() {
		return classLineNumber;
	}

	public void setClassLineNumber(int classLineNumber) {
		this.classLineNumber = classLineNumber;
	}

	@Override
	public String toString() {
		return  "PATH:          "+path+
				"\nLINENUMBER:    "+lineNumber+
				"\nTYPE:          "+type+
				"\nBELONGMETHOD:  "+belongMethod+
				"\nISPRIM:        "+isPrimitive+
				"\nBELONGCLASS:   "+belongClass+
				"\nCLASSLINE:     "+classLineNumber+"\n";
	}
	
}
