package com.MDGHandle.Nodes;

public class ThreadNotifyNode extends Node{
	private String ClassName;
	private NotifyType notifyType;
	private boolean lockIsThis;

	public ThreadNotifyNode(String fileName, int lineNumber, String className, NotifyType notifyType,
			boolean lockIsThis) {
		super(fileName, lineNumber);
		ClassName = className;
		this.notifyType = notifyType;
		this.lockIsThis = lockIsThis;
	}


	@Override
	public String toString() {
		return super.toString()+"\nClassName :"+ClassName+"\nNotifyType :"+notifyType+"\nLockIsThis :"+lockIsThis;
	}
}
