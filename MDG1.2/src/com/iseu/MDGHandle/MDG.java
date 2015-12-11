package com.iseu.MDGHandle;


import com.iseu.CASTParser.CASTParser;


public class MDG {
	private CASTParser cAstParser;
	public MDG(String projectPath,String DBpath) {
		System.out.println("MDG constructor");
		cAstParser = new CASTParser(projectPath,DBpath);
	}
	public void handleAst() {
		System.out.println("Handling AST information");
		cAstParser.parser();
	}
	public static void main(String[] args) {
		System.out.println("Begin parser");
		//���蹹������ͼ��Դ�빤��·��,�Լ����ݿ�·������
		/********************************************************************/
		MDG mdg = new MDG("H:\\Projects\\Concurrent\\src\\com\\other","D:/graph.db");
		/********************************************************************/
		mdg.handleAst();
		System.out.println("finish");
	}
}