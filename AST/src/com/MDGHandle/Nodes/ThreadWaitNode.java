package com.MDGHandle.Nodes;

public class ThreadWaitNode extends Node{
	private String ClassName;
	private WaitType waitType;
	private boolean lockIsThis;

	public ThreadWaitNode(String fileName, int lineNumber, String className, WaitType waitType, boolean lockIsThis) {
		super(fileName, lineNumber);
		ClassName = className;
		this.waitType = waitType;
		this.lockIsThis = lockIsThis;
	}


	@Override
	public String toString() {
		return super.toString()+"\nClassName :"+ClassName+"\nWaitType :"+waitType+"\nLockIsThis :"+lockIsThis;
	}
}
