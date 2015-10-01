package com.MDGHandle.Nodes;

public class ThreadNotifyNode extends Node{
	private String className;
	private NotifyType notifyType;
	private String objectTypeName;
	private String objectName;


	public ThreadNotifyNode(String fileName, int lineNumber, String className, NotifyType notifyType,
			 String objectTypeName, String objectName) {
		super(fileName, lineNumber);
		this.className = className;
		this.notifyType = notifyType;
		this.objectTypeName = objectTypeName;
		this.objectName = objectName;
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




	public String getObjectName() {
		return objectName;
	}




	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}




	@Override
	public String toString() {
		return super.toString()+"\nClassName :"+className+"\nNotifyType :"+notifyType
				+"\nObjectTypeName :"+objectTypeName+"\nObjectName :"+objectName;
	}
}
