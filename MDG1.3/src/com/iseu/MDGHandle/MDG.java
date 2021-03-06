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
		//所需构建依赖图的源码工程路径,以及数据库路径设置
		/********************************************************************/
		MDG mdg = new MDG("H:\\Projects\\TestCase\\src\\com\\TestCase05","E:/graph.db");
//		MDG mdg = new MDG("H:\\Projects\\hadoop-2.7.1-src","E:/graph.db");
		/********************************************************************/
		mdg.handleAst();
		System.out.println("finish");
	}
}
