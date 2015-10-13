package com.CASTParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.CASTVistitors.CASTVisitorTrigger;
import com.CASTVistitors.CASTVisitorCheck;
import com.CASTVistitors.CASTVisitorCommunication;
import com.CASTVistitors.CASTVisitorJavaMethodsCheck;
import com.CASTVistitors.CASTVisitorJavaMethodsPrepare;
import com.CASTVistitors.CASTVisitorSyn;
import com.CASTVistitors.CASTVisitorPrepare;
import com.Information.MethodInformation;
import com.Information.ShareVarInfo;
import com.Information.ThreadInformation;
import com.Information.ThreadVar;
import com.MDGHandle.Edges.Edge;
import com.MDGHandle.Edges.ThreadEdgeType;
import com.MDGHandle.Nodes.Node;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadTriggerNode;
import com.MDGHandle.Nodes.ThreadWaitNode;

public class CASTParser {
	private String projectPath;
	private ArrayList<String> filePathList;
	private HashMap<String, ThreadInformation> threadsInfo;
	private HashMap<String, ThreadVar> threadVarHashMap;
	/*
	 * 用于记录线程运行中包含的函数调用
	 * KEY:methodKey <包_类_函数_参数>
	 * VALUE:线程信息key <包_类> BinaryName
	 */
	private HashMap<String, String> threadMethodMapTable; 
	private Set<String> threadRelate;
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;
	//构造函数
	public CASTParser(String projectPath) {
		this.projectPath = projectPath;
		filePathList = new ArrayList<>();
		threadsInfo = new HashMap<>();	
		threadVarHashMap = new HashMap<>();
		threadTriggerNodes = new ArrayList<>();
		threadMethodMapTable = new HashMap<>();
		threadRelate = new HashSet<>();
	}

	public void  parser() {
		System.out.println("parsering");
		//创建AST访问单元组
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		
	
		
		
		
		triggerParser(compileUnits);
		bindThreadRel();	
//		synchronizeParser(compileUnits);	
		communicationParserPre(compileUnits);
		System.out.println("FIRST FINISH");
		communicatinoParserPost(compileUnits);
//		javaSrcMethod(compileUnits);
		
	}
	
	//线程启动边解析
	public void triggerParser(ArrayList<CompileUnit> compileUnits) {
		//用于获取线程相关类的信息，线程变量集，线程启动点
		CASTVisitorTrigger castVisitor = new CASTVisitorTrigger(threadsInfo,threadVarHashMap,threadTriggerNodes,threadMethodMapTable);
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
	//线程同步依赖解析
	public void synchronizeParser(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorSyn castVisitorSyn = new CASTVisitorSyn();
		castVisitorSyn.traverse(compileUnits);
		ArrayList<ThreadNotifyNode> threadNotifyNodes = castVisitorSyn.getThreadNotifyNodes();
		ArrayList<ThreadWaitNode> threadWaitNodes = castVisitorSyn.getThreadWaitNodes();
		for (ThreadNotifyNode threadNotifyNode : threadNotifyNodes) {
			System.out.println(threadNotifyNode);
		}
		for (ThreadWaitNode threadWaitNode : threadWaitNodes) {
			System.out.println(threadWaitNode);
		}
		ArrayList<Edge> edges = new ArrayList<>();
		for (ThreadNotifyNode threadNotifyNode : threadNotifyNodes) {
			for (ThreadWaitNode threadWaitNode : threadWaitNodes) {
				//属于同一个类中
				if(threadNotifyNode.getFileName().equals(threadWaitNode.getFileName())){
					if (threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())) {
						Node from = new Node(threadNotifyNode.getFileName(), threadNotifyNode.getLineNumber());
						Node to = new Node(threadWaitNode.getFileName(), threadWaitNode.getLineNumber());
						Edge edge = new Edge(from, to, ThreadEdgeType.THREADSYND);
						edges.add(edge);
					}
				}
				else if(threadRelate.contains(threadNotifyNode.getClassName()+threadWaitNode.getClassName())) {
					if (threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())) {
						Node from = new Node(threadNotifyNode.getFileName(), threadNotifyNode.getLineNumber());
						Node to = new Node(threadWaitNode.getFileName(), threadWaitNode.getLineNumber());
						Edge edge = new Edge(from, to, ThreadEdgeType.THREADSYND);
						edges.add(edge);
					}
				}
			}
		}
		for (Edge edge : edges) {
			System.out.println("______________________________________SYND_______________________________________");
			System.out.println(edge);
			System.out.println("______________________________________SYND_______________________________________");
		}
	}
	//线程通信依赖解析--函数信息获取
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.先将赋值语句，前缀表达式，后缀表达式会引起变量变化的函数记录下来
		CASTVisitorPrepare castVisitorPrepare = new CASTVisitorPrepare();
		castVisitorPrepare.traverse(compileUnits);
		//函数修改变量的信息
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.将函数调用引起变量改变的函数添加进记录表
		
		
		int times = 1;     //迭代次数
		CASTVisitorCheck castVisitorCheck = new CASTVisitorCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //处理函数信息
	
//			Set<Map.Entry<String, MethodInformation>> methodInformationxx = changeMethods.entrySet();
//			for (Entry<String, MethodInformation> entry : methodInformationxx) {
//				System.out.println("KEY:"+entry.getKey());
//				System.out.println("VALUE:"+entry.getValue());
//			}
			System.out.println("The "+(times++)+"  time");
//			System.out.println("The size is"+methodInformationxx.size());
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //清除那些对调用对象&&参数没有修改的函数
		ArrayList<String> keys = new ArrayList<>();
		Set<Map.Entry<String, MethodInformation>> methodInformations = changeMethods.entrySet();
		for (Entry<String, MethodInformation> entry : methodInformations) {
			if (!entry.getValue().isObjChange()&&entry.getValue().getIsParaChange()==0) {
				keys.add(entry.getKey());
			}
		}
		for (String  key : keys) {
			changeMethods.remove(key);
		}
		
		//函数信息处理与保存
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //映射表<包名，编号>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //将包名用编号替换的函数信息表
		int keyNumber = 0;
		try {
			PrintWriter methodsInfoOutToFile = new PrintWriter("Methods.txt");
			PrintWriter methodsMapTableOutToFile = new PrintWriter("methodMapTable.txt");
			File file = new File("srcMethodMapTable.obj");
			File file2 = new File("srcMethodInfo.obj");
			FileOutputStream fileOut = new FileOutputStream(file);
			FileOutputStream fileOut2 = new FileOutputStream(file2);
			
			//将函数信息写入文件————并建立映射表
			for (Entry<String, MethodInformation> entry : methodInformationsNew) {
				//找到包名
				String mapKey = entry.getKey().substring(0, entry.getKey().indexOf('_'));
				if (!methodMapTable.containsKey(mapKey)) {
					methodMapTable.put(mapKey, keyNumber++);
				}
				methodsInfoOutToFile.println(entry.getKey());
				methodsInfoOutToFile.print(entry.getValue());
				//键值为<包编号_类名_函数名及参数列表>
				javaMethodInfo.put(methodMapTable.get(mapKey)+entry.getKey().substring(entry.getKey().indexOf('_')), entry.getValue());
			}
			methodsInfoOutToFile.flush();
			methodsInfoOutToFile.close();
			
			//将映射表写入文件
			Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
			for (Entry<String, Integer> entry : mapTableSet) {
				methodsMapTableOutToFile.println(entry.getKey());
				methodsMapTableOutToFile.println(entry.getValue());
			}
			methodsMapTableOutToFile.flush();
			methodsMapTableOutToFile.close();
			System.out.println("The total mapTable size is: "+mapTableSet.size());
			
			//将映射表以对象的形式写入文件srcMethodMapTable.obj
			try {
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				objectOut.writeObject(methodMapTable);
				objectOut.flush();
				objectOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//将java函数信息以对象的形式写入文件srcMethodInfo.obj
			try {
				ObjectOutputStream objectOut2 = new ObjectOutputStream(fileOut2);
				objectOut2.writeObject(javaMethodInfo);
				objectOut2.flush();
				objectOut2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("After reduce the methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
	//线程通信依赖解析--Def与Use
	public void communicatinoParserPost(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorCommunication castVisitorCommunication = new CASTVisitorCommunication(threadMethodMapTable,threadsInfo);
		do {
			castVisitorCommunication.traverse(compileUnits);
		} while (castVisitorCommunication.isUpdate());
		
//		Set<Map.Entry<String, ThreadInformation>> set = threadsInfo.entrySet();
//		for (Entry<String, ThreadInformation> entry : set) {
//			System.out.println("KEY:"+entry.getKey());
//			System.out.println("VALUE:"+entry.getValue());
//		}
		System.out.println(threadsInfo.size());
//		System.out.println(threadMethodMapTable);
		System.out.println(threadMethodMapTable.size());
		
//		Set<Map.Entry<String, String>> set = threadMethodMapTable.entrySet();
//		for (Entry<String, String> entry : set) {
//			System.out.println("KEY:"+entry.getKey());
//			System.out.println("VALUE:"+entry.getValue());
//		}
		Set<Map.Entry<String, ThreadInformation>> threadInfomations = threadsInfo.entrySet();
		for (Entry<String, ThreadInformation> entry : threadInfomations) {
			System.out.println("< Thread: "+entry.getKey()+" >");
			System.out.println("_________________________DEF________________________________");
			HashMap<String, ShareVarInfo> defHM = entry.getValue().getDefList();
			Set<Map.Entry<String, ShareVarInfo>> defs = defHM.entrySet();
			if (defs.isEmpty()) {
				System.out.println("------------------------EMPTY--------------------------");
			}
			for (Entry<String, ShareVarInfo> def : defs) {
				System.out.println("KEY: "+def.getKey());
				System.out.println(def.getValue());
			}
			System.out.println("_________________________USE________________________________");
			HashMap<String, ShareVarInfo> usesHM = entry.getValue().getUseList();
			Set<Map.Entry<String, ShareVarInfo>> uses = usesHM.entrySet();
			if (uses.isEmpty()) {
				System.out.println("------------------------EMPTY--------------------------");
			}
			for (Entry<String, ShareVarInfo> use : uses) {
				System.out.println("KEY: "+use.getKey());
				System.out.println(use.getValue());
			}
		}
		System.out.println("DONE!");
		System.out.flush();
	}
	//java源码函数解析
	public void javaSrcMethod(ArrayList<CompileUnit> compileUnits) {
		// 1.先将赋值语句，前缀表达式，后缀表达式会引起变量变化的函数记录下来
		CASTVisitorJavaMethodsPrepare castVisitorTest = new CASTVisitorJavaMethodsPrepare();
		castVisitorTest.traverse(compileUnits);
		//函数修改变量的信息
		HashMap<String, MethodInformation> changeMethods = castVisitorTest.getChangeMethods();
		// 2.将函数调用引起变量改变的函数添加进记录表
		int times = 1;     //迭代次数
		CASTVisitorJavaMethodsCheck castVisitorCheck = new CASTVisitorJavaMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //处理函数信息
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //清除那些对调用对象及参数没有修改的函数
		ArrayList<String> keys = new ArrayList<>();
		Set<Map.Entry<String, MethodInformation>> methodInformations = changeMethods.entrySet();
		for (Entry<String, MethodInformation> entry : methodInformations) {
			if (!entry.getValue().isObjChange()&&entry.getValue().getIsParaChange()==0) {
				keys.add(entry.getKey());
			}
		}
		for (String  key : keys) {
			changeMethods.remove(key);
		}
		
		//函数信息处理与保存
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //映射表<包名，编号>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //将包名用编号替换的函数信息表
		int keyNumber = 0;
		try {
			PrintWriter methodsInfoOutToFile = new PrintWriter("javaMethods.txt");
			PrintWriter methodsMapTableOutToFile = new PrintWriter("javaMethodMapTable.txt");
			File file = new File("javaMethodMapTable.obj");
			File file2 = new File("javaMethodInfo.obj");
			FileOutputStream fileOut = new FileOutputStream(file);
			FileOutputStream fileOut2 = new FileOutputStream(file2);
			
			//将函数信息写入文件————并建立映射表
			for (Entry<String, MethodInformation> entry : methodInformationsNew) {
				//找到包名
				String mapKey = entry.getKey().substring(0, entry.getKey().indexOf('_'));
				if (!methodMapTable.containsKey(mapKey)) {
					methodMapTable.put(mapKey, keyNumber++);
				}
				methodsInfoOutToFile.println(entry.getKey());
				methodsInfoOutToFile.print(entry.getValue());
				javaMethodInfo.put(methodMapTable.get(mapKey)+entry.getKey().substring(entry.getKey().indexOf('_')), entry.getValue());
			}
			methodsInfoOutToFile.flush();
			methodsInfoOutToFile.close();
			
			//将映射表写入文件
			Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
			for (Entry<String, Integer> entry : mapTableSet) {
				methodsMapTableOutToFile.println(entry.getKey());
				methodsMapTableOutToFile.println(entry.getValue());
			}
			methodsMapTableOutToFile.flush();
			methodsMapTableOutToFile.close();
			System.out.println("The total mapTable size is: "+mapTableSet.size());
			
			//将映射表以对象的形式写入文件javaMethodMapTable.obj
			try {
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				objectOut.writeObject(methodMapTable);
				objectOut.flush();
				objectOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//将java函数信息以对象的形式写入文件javaMethodInfo.obj
			try {
				ObjectOutputStream objectOut2 = new ObjectOutputStream(fileOut2);
				objectOut2.writeObject(javaMethodInfo);
				objectOut2.flush();
				objectOut2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("The methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
	//打印文件列表
	public void  printFiles() {
		for (String fileName : filePathList) {
			System.out.println(fileName);
		}
	}
	//将相关的线程进行绑定
	public void bindThreadRel() {
		ThreadVar[] threadVarArray = new ThreadVar[threadVarHashMap.size()];
		threadVarHashMap.values().toArray(threadVarArray);
		System.out.println("ThreadVarArray :"+threadVarArray.length);
		for (ThreadVar threadVar1 : threadVarArray) {
			for (ThreadVar threadVar2 : threadVarArray) {
				if (threadVar1.getFilePath().equals(threadVar2.getFilePath())) {
					System.out.println("Tn the same file");
					threadRelate.add(threadVar1.getBindingTypeName()+threadVar2.getBindingTypeName());
					threadRelate.add(threadVar2.getBindingTypeName()+threadVar1.getBindingTypeName());					
				}
			}
		}
		System.out.println("Total ThreadRelate :"+threadRelate.size());
		for (String threadRel : threadRelate) {
			System.out.println("ThreadRelate:::"+threadRel);
		}
	}

}

