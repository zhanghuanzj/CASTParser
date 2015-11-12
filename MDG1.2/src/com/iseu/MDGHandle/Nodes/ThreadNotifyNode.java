package com.iseu.MDGHandle.Nodes;

public class ThreadNotifyNode extends Node{
	private String belongClass;
	private NotifyType notifyType;
	private String objectTypeName;


	public ThreadNotifyNode(String methodName,String fileName, int lineNumber, String className, NotifyType notifyType,
			 String objectTypeName) {
		super(methodName,fileName, lineNumber);
		this.belongClass = className;
		this.notifyType = notifyType;
		this.objectTypeName = objectTypeName;
	}




	public String getClassName() {
		return belongClass;
	}




	public void setClassName(String className) {
		this.belongClass = className;
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
		return super.toString()+"\nClassName :"+belongClass+"\nNotifyType :"+notifyType
				+"\nObjectTypeName :"+objectTypeName;
	}
}
