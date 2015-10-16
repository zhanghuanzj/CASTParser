package com.Information;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadInformation {
	private String name;
	private ThreadType threadType;
	private String filePath;
	private int startLineNumber;
	//Def”ÎUseºØ£¨Key£∫methodKey+––∫≈
	private HashMap<String, ShareVarInfo> defHashMap;
	private HashMap<String, ShareVarInfo> useHashMap;
	
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
	
	public ThreadInformation(String name,ThreadType tType) {
		this.name = name;
		threadType = tType;
		defHashMap = new HashMap<>();
		useHashMap = new HashMap<>();
	}
	@Override
	public String toString() {
		return "ThreadName : "+name+"\nThreadFile : "+filePath+"\nThreadType : "
				+threadType+"\nThreadEntrance : "+startLineNumber;
	}
}
