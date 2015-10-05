package com.Information;

import java.util.HashMap;

public class MethodInformation {
	private boolean isObjChange;     //是否对调用对象进行修改
	private int isParaChange;        //对每一个参数的修改情况，每一个bit为代表对相应index参数的修改情况（0表示不修改，1表示修改）初始全为0
	private int checkTable;          //记录参数列表的引用修改情况（0表示引用进行了修改，1表示没有修改）初始全为1
	public MethodInformation() {
		super();
		this.isObjChange = false;
		this.isParaChange = 0;
		this.checkTable = -1;
	}
//	public static void main(String[] args) {
//		HashMap<String, MethodInformation> hashMap = new HashMap<>();
//		hashMap.put("1", new MethodInformation());
//		MethodInformation methodInformation = hashMap.get("1");
//		methodInformation.setObjChange(true);
//		MethodInformation methodinfo = hashMap.get("1");
//		System.out.println(methodinfo);
//	}
	
	public boolean isObjChange() {
		return isObjChange;
	}
	public void setObjChange(boolean isObjChange) {
		this.isObjChange = isObjChange;
	}
	public int getIsParaChange() {
		return isParaChange;
	}
	public void setIsParaChange(int isParaChange) {
		this.isParaChange = isParaChange;
	}
	public int getCheckTable() {
		return checkTable;
	}
	public void setCheckTable(int checkTable) {
		this.checkTable = checkTable;
	}
	
	@Override
	public String toString() {
		return "IsObjChange: "+isObjChange+"\nIsParaChange: "+Integer.toBinaryString(isParaChange)+"\nCheckTable:"+Integer.toBinaryString(checkTable);
	}
}
