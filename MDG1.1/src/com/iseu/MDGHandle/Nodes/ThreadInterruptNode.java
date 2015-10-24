package com.iseu.MDGHandle.Nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ThreadInterruptNode extends Node{
	ArrayList<String> threadKeySet ;
	
	public ThreadInterruptNode(String fileName, int lineNumber) {
		super(fileName, lineNumber);
		this.threadKeySet = new ArrayList<>();
	}

	public ArrayList<String> getThreadKeyList() {
		return threadKeySet;
	}

	public void setThreadKeyList(ArrayList<String> threadKeySet) {
		this.threadKeySet = threadKeySet;
	}
	
}
