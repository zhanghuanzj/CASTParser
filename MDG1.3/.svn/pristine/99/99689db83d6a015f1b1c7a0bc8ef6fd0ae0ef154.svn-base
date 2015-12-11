package com.iseu.Information;

import java.io.Serializable;

public class MethodInformation implements Serializable{
	private boolean isObjChange;     //是否对调用对象进行修改
	private int paraChange;        //对每一个参数的修改情况，每一个bit为代表对相应index参数的修改情况（0表示不修改，1表示修改）初始全为0
	private int checkTable;          //记录参数列表的引用修改情况（0表示引用进行了修改，1表示没有修改）初始全为1
	public MethodInformation() {
		super();
		this.isObjChange = false;
		this.paraChange = 0;
		this.checkTable = -1;
	}
	//对象改变情况函数
	public boolean isObjChange() {
		return isObjChange;
	}
	
	public void setObjChange(boolean isObjChange) {
		this.isObjChange = isObjChange;
	}
	
	//参数改变情况函数
	/**
	 * index 号参数是否修改
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
	 * 设置参数修改情况，index号参数修改
	 * @param index
	 */
	public void parameterChange(int index) {
		if (index<0||index>31) {
			return ;
		}
		paraChange = (paraChange|(1<<index));
	}
	
	/**
	 * 任意参数是否修改
	 * @return
	 */
	public boolean isAnyParaChange() {
		return (paraChange&checkTable)>0;
	}

	/**
	 * 获取参数记录值
	 * @return
	 */
	public int getParaChange() {
		return paraChange;
	}
	
	//引用重置情况函数
	 /**
	  * 引用重置调整，index号参数引用重置
	  * @param index
	  */
	public void checkTableAdjust(int index) {
		if (index<0||index>31) {
			return ;
		}
		checkTable = checkTable&(~(1<<index));
	}
	
	/**
	 * 返回checkTable（index）是否为1，true为引用没有重置
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
