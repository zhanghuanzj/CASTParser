package com.iseu.Information;

import java.io.Serializable;

public class MethodInformation implements Serializable{
	private boolean isObjChange;     //�Ƿ�Ե��ö�������޸�
	private int paraChange;        //��ÿһ���������޸������ÿһ��bitΪ�������Ӧindex�������޸������0��ʾ���޸ģ�1��ʾ�޸ģ���ʼȫΪ0
	private int checkTable;          //��¼�����б�������޸������0��ʾ���ý������޸ģ�1��ʾû���޸ģ���ʼȫΪ1
	public MethodInformation() {
		super();
		this.isObjChange = false;
		this.paraChange = 0;
		this.checkTable = -1;
	}
	//����ı��������
	public boolean isObjChange() {
		return isObjChange;
	}
	
	public void setObjChange(boolean isObjChange) {
		this.isObjChange = isObjChange;
	}
	
	//�����ı��������
	/**
	 * index �Ų����Ƿ��޸�
	 * @param index
	 * @return
	 */
	public boolean isParameterChange(int index) {
		if (index<0||index>31) {
			return false;
		}
		return ((paraChange&(1<<index))&(checkTable)&(1<<index))>0;
	}

	/**
	 * ���ò����޸������index�Ų����޸�
	 * @param index
	 */
	public void parameterChange(int index) {
		if (index<0||index>31) {
			return ;
		}
		paraChange = (paraChange|(1<<index));
	}
	
	/**
	 * ��������Ƿ��޸�
	 * @return
	 */
	public boolean isAnyParaChange() {
		return (paraChange&checkTable)>0;
	}

	/**
	 * ��ȡ������¼ֵ
	 * @return
	 */
	public int getParaChange() {
		return paraChange;
	}
	
	//���������������
	 /**
	  * �������õ�����index�Ų�����������
	  * @param index
	  */
	public void checkTableAdjust(int index) {
		if (index<0||index>31) {
			return ;
		}
		checkTable = checkTable&(~(1<<index));
	}
	
	/**
	 * ����checkTable��index���Ƿ�Ϊ1��trueΪ����û������
	 * @param index
	 * @return
	 */
	public boolean isCheckTableOk(int index) {
		if (index<0||index>31) {
			return false;
		}
		return (checkTable&(1<<index))>0;
	}
	

	public int getCheckTable() {
		return checkTable;
	}

	public void setCheckTable(int checkTable) {
		this.checkTable = checkTable;
	}

	
	
	
	@Override
	public String toString() {
		return isObjChange+"\n"+paraChange+"\n"+checkTable+"\n";
	}
}
