package com.MDGHandle.Nodes;

public class ThreadNotifyNode extends Node{
	private String className;
	private NotifyType notifyType;
	private String objectTypeName;


	public ThreadNotifyNode(String fileName, int lineNumber, String className, NotifyType notifyType,
			 String objectTypeName) {
		super(fileName, lineNumber);
		this.className = className;
		this.notifyType = notifyType;
		this.objectTypeName = objectTypeName;
	}




	public String getClassName() {
		return className;
	}




	public void setClassName(String className) {
		this.className = className;
	}




	public NotifyType getNotifyType() {
		return notifyType;
	}




	public void setNotifyType(NotifyType notifyType) {
		this.notifyType = notifyType;
	}





	public String getObjectTypeName() {
		return objectTypeName;
	}




	public void setObjectTypeName(String objectTypeName) {
		this.objectTypeName = objectTypeName;
	}








	@Override
	public String toString() {
		return super.toString()+"\nClassName :"+className+"\nNotifyType :"+notifyType
				+"\nObjectTypeName :"+objectTypeName;
	}
}
