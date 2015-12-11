package com.iseu.MDGHandle.Nodes;

public class ThreadTriggerNode extends Node{
//	private String methodName;      //�����ڵ����ڵĺ���
	private String threadVarKey;    //Ϊ�˴��̱߳������л�ȡ������Ϣ
	
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
