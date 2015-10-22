package com.iseu.MDGHandle.Nodes;

public class ThreadWaitNode extends Node{
	private String className;
	private WaitType waitType;
	private String objectTypeName;


	public ThreadWaitNode(String fileName, int lineNumber, String className, WaitType waitType,
		   String objectTypeName) {
		super(fileName, lineNumber);
		this.className = className;
		this.waitType = waitType;
		this.objectTypeName = objectTypeName;

	}




	@Override
	public String toString() {
		return super.toString()+"\nClassName :"+className+"\nNotifyType :"+waitType+
				"\nObjectTypeName :"+objectTypeName;
	}




	public String getClassName() {
		return className;
	}




	public void setClassName(String className) {
		this.className = className;
	}




	public WaitType getWaitType() {
		return waitType;
	}




	public void setWaitType(WaitType waitType) {
		this.waitType = waitType;
	}





	public String getObjectTypeName() {
		return objectTypeName;
	}




	public void setObjectTypeName(String objectTypeName) {
		this.objectTypeName = objectTypeName;
	}



	
	
}
