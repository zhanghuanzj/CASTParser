package com.iseu.Information;

import java.io.Serializable;

public class MethodInformation implements Serializable{
	private boolean isObjChange;     //�Ƿ�Ե��ö�������޸�
	private int isParaChange;        //��ÿһ���������޸������ÿһ��bitΪ�������Ӧindex�������޸������0��ʾ���޸ģ�1��ʾ�޸ģ���ʼȫΪ0
	private int checkTable;          //��¼�����б�������޸������0��ʾ���ý������޸ģ�1��ʾû���޸ģ���ʼȫΪ1
	public MethodInformation() {
		super();
		this.isObjChange = false;
		this.isParaChange = 0;
		this.checkTable = -1;
	}
	
	public boolean isParameterChange(int index) {
		if (index<0||index>31) {
			return false;
		}
		return (isParaChange&(1<<index))>0;
	}
	//����checkTable��index���Ƿ�Ϊ1��Ҳ�����Ƿ��������
	public boolean isCheckTableOk(int index) {
		if (index<0||index>31) {
			return false;
		}
		return (checkTable&(1<<index))>0;
	}
	
	public void parameterChange(int index) {
		if (index<0||index>31) {
			return ;
		}
		isParaChange = (isParaChange|(1<<index))&checkTable;
	}
	
	public void checkTableAdjust(int index) {
		if (index<0||index>31) {
			return ;
		}
		checkTable = checkTable&(~(1<<index));
	}
	
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
	
//	@Override
//	public String toString() {
//		return "IsObjChange: "+isObjChange+"\nIsParaChange: "+Integer.toBinaryString(isParaChange)+"\nCheckTable:"+Integer.toBinaryString(checkTable);
//	}
	
	@Override
	public String toString() {
		return isObjChange+"\n"+isParaChange+"\n"+checkTable+"\n";
	}
}
