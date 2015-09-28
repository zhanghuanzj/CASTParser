package com.Information;

public class ThreadInformation {
	private String name;
	private ThreadType threadType;
	private String filePath;
	private int startLineNumber;
	
	//setters and getters
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
	}
	@Override
	public String toString() {
		return "ThreadName : "+name+"\nThreadFile : "+filePath+"\nThreadType : "
				+threadType+"\nThreadEntrance : "+startLineNumber;
	}
}
