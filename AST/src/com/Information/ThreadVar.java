package com.Information;

//���ڼ�¼�̱߳�������Ϣ����Ҫ���ڽ�����������������Ӧ�߳���Ϣ�ν�����
public class ThreadVar {
	//ǰ�����������̱߳�������KEY
	private String typeName;   //����������
	private String filePath;   //�ļ�·��
	private String varName;    //������
	private String bindingTypeName;   //��������
	private String threadInfoKey;     //��ȡ�߳���Ϣ��KEY
	public ThreadVar(String typeName, String filePath, String varName, String bindingTypeName, String threadInfoKey) {
		super();
		this.typeName = typeName;
		this.filePath = filePath;
		this.varName = varName;
		this.bindingTypeName = bindingTypeName;
		this.threadInfoKey = threadInfoKey;
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
	public String getThreadInfoKey() {
		return threadInfoKey;
	}
	public void setThreadInfoKey(String threadInfoKey) {
		this.threadInfoKey = threadInfoKey;
	}
	
	@Override
	public String toString() {
		return "FilePaht : "+filePath+"\nTypeName : "+typeName+"\nVarName : "
				+varName+"\nBindingTypeName : "+bindingTypeName+"\nThreadInfoKey : "+threadInfoKey;
	}
	
}
