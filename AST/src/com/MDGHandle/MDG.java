package com.MDGHandle;

import java.util.ArrayList;
import java.util.HashMap;

import com.CASTParser.CASTParser;
import com.MDGHandle.Edges.Edge;
import com.MDGHandle.Nodes.Node;

public class MDG {
	private ArrayList<Node> nodeList;
	private ArrayList<Edge> edgeList;
	private HashMap<Node, String> crossEdge;
	private CASTParser cAstParser;
	public MDG(String projectPath) {
		System.out.println("MDG constructor");
		nodeList = new ArrayList<>();
		edgeList = new ArrayList<>();
		crossEdge = new HashMap<>();
		cAstParser = new CASTParser(projectPath);
	}
	public void handleAst() {
		System.out.println("Handling AST information");
		cAstParser.parser();
	}
	public static void main(String[] args) {
		System.out.println("Begin parser");
		//所需构建依赖图的源码工程路径设置
		/********************************************************************/
		MDG mdg = new MDG("H:\\Projects\\TestCase\\src\\com\\TestCaseX");
		/********************************************************************/
		mdg.handleAst();
		System.out.println("finish");
	}
}
