package com.Information;

import java.util.HashMap;

public class MethodInformation {
	private boolean isObjChange;     //�Ƿ�Ե��ö�������޸�
	private int isParaChange;        //��ÿһ���������޸������ÿһ��bitΪ�������Ӧindex�������޸������0��ʾ���޸ģ�1��ʾ�޸ģ���ʼȫΪ0
	private int checkTable;          //��¼�����б�������޸������0��ʾ���ý������޸ģ�1��ʾû���޸ģ���ʼȫΪ1
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
