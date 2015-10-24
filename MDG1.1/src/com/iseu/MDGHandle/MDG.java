package com.iseu.MDGHandle;


import com.iseu.CASTParser.CASTParser;


public class MDG {
	private CASTParser cAstParser;
	public MDG(String projectPath) {
		System.out.println("MDG constructor");
		cAstParser = new CASTParser(projectPath);
	}
	public void handleAst() {
		System.out.println("Handling AST information");
		cAstParser.parser();
	}
	public static void main(String[] args) {
		System.out.println("Begin parser");
		//���蹹������ͼ��Դ�빤��·������
		/********************************************************************/
		MDG mdg = new MDG("H:\\Projects\\TestCase\\src\\com\\TestCase01");
		/********************************************************************/
		mdg.handleAst();
		System.out.println("finish");
	}
}
