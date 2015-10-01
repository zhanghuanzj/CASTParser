package com.MDGHandle.Edges;

public enum ThreadEdgeType {
	THREADTRIGGER("THREADTRIGGER"),
	THREADSYND("THREADSYND");
	private String threadEdgeType;

	private ThreadEdgeType(String threadEdgeType) {
		this.threadEdgeType = threadEdgeType;
	}

	public String getThreadEdgeType() {
		return threadEdgeType;
	}

	public void setThreadEdgeType(String threadEdgeType) {
		this.threadEdgeType = threadEdgeType;
	}
}
