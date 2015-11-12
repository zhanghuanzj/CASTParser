package com.iseu.Information;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.iseu.MDGHandle.Nodes.Node;
import com.iseu.MDGHandle.Nodes.ThreadInterruptNode;

public class ThreadInformation {
	//key
	private String name;
	private ThreadType threadType;
	private String filePath;
	private int startLineNumber;
	//Def与Use集，Key：methodKey+行号
	private HashMap<String, ShareVarInfo> defHashMap;
	private HashMap<String, ShareVarInfo> useHashMap;
	//中断接受节点
	private Set<Node> interruptNodes;
	//含有的变量类型(BinaryName)
	private Set<String> variableTypeSet;
	
	public ThreadInformation(String name,ThreadType tType) {
		this.name = name;
		threadType = tType;
		defHashMap = new HashMap<>();
		useHashMap = new HashMap<>();
		interruptNodes = new HashSet<>();
		variableTypeSet = new HashSet<>();
	}
	
	//setters and getters
	public void addDefVar(String key,ShareVarInfo varInfo) {
		if (defHashMap.containsKey(key)) {
			return;
		}
		defHashMap.put(key,varInfo);
	}
	public void addUseVar(String key,ShareVarInfo varInfo) {
		if (defHashMap.containsKey(key)) {
			return;
		}
		useHashMap.put(key, varInfo);
	}
	public HashMap<String, ShareVarInfo> getDefList() {
		return defHashMap;
	}
	public void setDefList(HashMap<String, ShareVarInfo> defList) {
		this.defHashMap = defList;
	}
	public HashMap<String, ShareVarInfo> getUseList() {
		return useHashMap;
	}
	public void setUseList(HashMap<String, ShareVarInfo> useList) {
		this.useHashMap = useList;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ThreadType getThreadType() {
		return threadType;
	}
	public void setThreadType(ThreadType threadType) {
		this.threadType = threadType;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public int getStartLineNumber() {
		return startLineNumber;
	}
	public void setStartLineNumber(int startLineNumber) {
		this.startLineNumber = startLineNumber;
	}
	
	public HashMap<String, ShareVarInfo> getDefHashMap() {
		return defHashMap;
	}
	public void setDefHashMap(HashMap<String, ShareVarInfo> defHashMap) {
		this.defHashMap = defHashMap;
	}
	public HashMap<String, ShareVarInfo> getUseHashMap() {
		return useHashMap;
	}
	public void setUseHashMap(HashMap<String, ShareVarInfo> useHashMap) {
		this.useHashMap = useHashMap;
	}
	public Set<Node> getInterruptNodes() {
		return interruptNodes;
	}
	public void setInterruptNodes(Set<Node> interruptNodes) {
		this.interruptNodes = interruptNodes;
	}

	public Set<String> getVariableTypeSet() {
		return variableTypeSet;
	}
	public void setVariableTypeSet(Set<String> variableTypeSet) {
		this.variableTypeSet = variableTypeSet;
	}
	@Override
	public String toString() {
		return "ThreadName : "+name+"\nThreadFile : "+filePath+"\nThreadType : "
				+threadType+"\nThreadEntrance : "+startLineNumber+"\nVarSet:"+variableTypeSet;
	}
}
