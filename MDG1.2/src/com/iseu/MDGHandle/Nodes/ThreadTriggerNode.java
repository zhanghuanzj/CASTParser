package com.iseu.MDGHandle.Nodes;

public class ThreadTriggerNode extends Node{
//	private String methodName;      //触发节点所在的函数
	private String threadVarKey;    //为了从线程变量集中获取更多信息
	
	public ThreadTriggerNode(String methodName,String fileName, int lineNumber,String threadVarKey) {
		super(methodName,fileName, lineNumber);
		this.threadVarKey = threadVarKey;
	}

	public String getThreadVarKey() {
		return threadVarKey;
	}

	public void setThreadVarKey(String threadVarKey) {
		this.threadVarKey = threadVarKey;
	}
	
//	@Override
//	public String toString() {
//		return super.toString()+"\nThreadKey: "+threadVarKey;
//	}
}
