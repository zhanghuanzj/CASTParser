package com.iseu.Information;

import java.io.Serializable;

public class MethodInformation implements Serializable{
	private boolean isObjChange;     //是否对调用对象进行修改
	private int isParaChange;        //对每一个参数的修改情况，每一个bit为代表对相应index参数的修改情况（0表示不修改，1表示修改）初始全为0
	private int checkTable;          //记录参数列表的引用修改情况（0表示引用进行了修改，1表示没有修改）初始全为1
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
	//返回checkTable（index）是否为1，也就是是否允许更改
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
