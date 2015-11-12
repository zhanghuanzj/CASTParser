package com.iseu.MDGHandle.Nodes;

public enum NotifyType {
	NOTIFY("NOTIFY"),
	SIGNAL("SIGNAL");
	private String notifyType;

	private NotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	
	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	
}
