package com.iseu.Information;

//用于记录线程变量的信息，主要用于将变量函数调用与相应线程信息衔接起来
public class ThreadVar {
	//前三变量用作线程变量集的KEY
	private String typeName;   //定义类型名
	private String filePath;   //文件路径
	private String varName;    //变量名
	private String bindingTypeName;   //绑定类型名,获取线程信息的KEY
	private String threadMethodKey;   //所在函数的methodKey
	public ThreadVar(String typeName, String filePath, String varName, String bindingTypeName,String threadMethodKey) {
		super();
		this.typeName = typeName;
		this.filePath = filePath;
		this.varName = varName;
		this.bindingTypeName = bindingTypeName;
		this.threadMethodKey = threadMethodKey;
	}
	
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getBindingTypeName() {
		return bindingTypeName;
	}
	public void setBindingTypeName(String bindingTypeName) {
		this.bindingTypeName = bindingTypeName;
	}
	
	public String getThreadMethodKey() {
		return threadMethodKey;
	}

	public void setThreadMethodKey(String threadMethodKey) {
		this.threadMethodKey = threadMethodKey;
	}

	@Override
	public String toString() {
		return "FilePaht : "+filePath+"\nTypeName : "+typeName+"\nVarName : "
				+varName+"\nBindingTypeName : "+bindingTypeName;
	}
	
}
