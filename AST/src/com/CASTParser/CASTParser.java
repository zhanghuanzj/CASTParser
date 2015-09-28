package com.CASTParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.CASTVistitors.CASTVisitor;
import com.Information.ThreadInformation;
import com.Information.ThreadVar;
import com.MDGHandle.Edges.Edge;
import com.MDGHandle.Edges.ThreadEdgeType;
import com.MDGHandle.Nodes.Node;
import com.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTParser {
	private String projectPath;
	private ArrayList<String> filePathList;
	private HashMap<String, ThreadInformation> threadsInfo;
	private HashMap<String, ThreadVar> threadVarHashMap;
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;
	//构造函数
	public CASTParser(String projectPath) {
		this.projectPath = projectPath;
		filePathList = new ArrayList<>();
		threadsInfo = new HashMap<>();	
		threadVarHashMap = new HashMap<>();
		threadTriggerNodes = new ArrayList<>();
	}
	
	
	
	public void  parser() {
		System.out.println("parsering");
		//创建AST访问单元组
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		//用于获取线程相关类的信息，线程变量集，线程启动点
		CASTVisitor castVisitor = new CASTVisitor(threadsInfo,threadVarHashMap,threadTriggerNodes);
		castVisitor.traverse(compileUnits);
		
		//打印线程类信息
		Set<Map.Entry<String, ThreadInformation>> threadInfomations = threadsInfo.entrySet();
		for (Entry<String, ThreadInformation> entry : threadInfomations) {
			System.out.println("______________________________THREAD_INFO_HASHMAP____________________________________");
			System.out.println("KEY: "+entry.getKey());
			System.out.println(entry.getValue());
			System.out.println("______________________________THREAD_INFO_HASHMAP____________________________________");
		}
		
		//打印线程变量信息
		Set<Map.Entry<String, ThreadVar>> threadVars = threadVarHashMap.entrySet();
		for (Entry<String, ThreadVar> entry : threadVars) {
			System.out.println("______________________________THREAD_VAR_HASHMAP____________________________________");
			System.out.println("KEY: "+entry.getKey());
			System.out.println(entry.getValue());
			System.out.println("______________________________THREAD_VAR_HASHMAP____________________________________");
		}
		
		//打印线程触发节点
		for (ThreadTriggerNode threadTiggerNode : threadTriggerNodes) {
			System.out.println("_____________________________TRIGGER_TRIGGER_NODE___________________________________");
			System.out.println(threadTiggerNode);
			System.out.println("_____________________________TRIGGER_TRIGGER_NODE___________________________________");
		}
		
		ArrayList<Edge> edgesList = new ArrayList<>();
		for (ThreadTriggerNode threadTiggerNode : threadTriggerNodes) {
			ThreadTriggerNode from = threadTiggerNode;
			String threadVarKey = from.getThreadVarKey();
			if (!threadVarHashMap.containsKey(threadVarKey)) {
				continue;
			}
			String threadInfoKey = threadVarHashMap.get(threadVarKey).getThreadInfoKey();
			if (!threadsInfo.containsKey(threadInfoKey)) {
				continue;
			}
			ThreadInformation threadInformation = threadsInfo.get(threadInfoKey);
			if (threadInformation!=null) {
				String filePath = threadInformation.getFilePath();
				int lineNumber = threadInformation.getStartLineNumber();
				Node to = new Node(filePath, lineNumber);
				Edge threadStartEdge = new Edge(from, to, ThreadEdgeType.THREADTRIGGER);
				edgesList.add(threadStartEdge);
			}
		}
		System.out.println("Total trigger edge is:"+edgesList.size());
		//打印边
		for (Edge edge : edgesList) {
			System.out.println("______________________________________TRIGGER_______________________________________");
			System.out.println(edge);
			System.out.println("______________________________________TRIGGER_______________________________________");
		}
	}
	
	public void  printFiles() {
		for (String fileName : filePathList) {
			System.out.println(fileName);
		}
	}
	
	//用于解析的参数
	public String  getBinPath(String packageName)
	{
		return projectPath+"\\bin\\"+packageName;
	}
	
	public String  getSrcPath(String packageName)
	{
		return projectPath+"\\src\\"+packageName;
	}
}

