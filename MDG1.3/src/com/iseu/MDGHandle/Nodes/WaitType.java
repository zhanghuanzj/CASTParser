package com.iseu.MDGHandle.Nodes;

public enum WaitType {
	AWAIT("AWAIT"),
	WAIT("WAIT");
	private String waitType;

	private WaitType(String waitType) {
		this.waitType = waitType;
	}

	public String getWaitType() {
		return waitType;
	}

	public void setWaitType(String waitType) {
		this.waitType = waitType;
	}
}
