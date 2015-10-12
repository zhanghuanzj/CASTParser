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
	private HashMap<String, ShareVarInfo> defList;
	private HashMap<String, ShareVarInfo> useList;
	
	//setters and getters
	public void addDefVar(String key,ShareVarInfo varInfo) {
		if (defList.containsKey(key)) {
			return;
		}
		defList.put(key,varInfo);
	}
	public void addUseVar(String key,ShareVarInfo varInfo) {
		if (defList.containsKey(key)) {
			return;
		}
		useList.put(key, varInfo);
	}
	public HashMap<String, ShareVarInfo> getDefList() {
		return defList;
	}
	public void setDefList(HashMap<String, ShareVarInfo> defList) {
		this.defList = defList;
	}
	public HashMap<String, ShareVarInfo> getUseList() {
		return useList;
	}
	public void setUseList(HashMap<String, ShareVarInfo> useList) {
		this.useList = useList;
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
		defList = new HashMap<>();
		useList = new HashMap<>();
	}
	@Override
	public String toString() {
		return "ThreadName : "+name+"\nThreadFile : "+filePath+"\nThreadType : "
				+threadType+"\nThreadEntrance : "+startLineNumber;
	}
}
