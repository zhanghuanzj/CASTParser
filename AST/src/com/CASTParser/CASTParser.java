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

import javax.swing.text.StyledEditorKit.ForegroundAction;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SynchronizedStatement;

import com.CASTVistitors.CASTVisitorTrigger;
import com.CASTVistitors.CASTVisitorSrcMethodsCheck;
import com.CASTVistitors.CASTVisitorCommunication;
import com.CASTVistitors.CASTVisitorInterrupt;
import com.CASTVistitors.CASTVisitorJavaMethodsCheck;
import com.CASTVistitors.CASTVisitorJavaMethodsPrepare;
import com.CASTVistitors.CASTVisitorSynchronize;
import com.CASTVistitors.CASTVisitorSrcMethodsPrepare;
import com.Information.MethodInformation;
import com.Information.ShareVarInfo;
import com.Information.ThreadInformation;
import com.Information.ThreadVar;
import com.MDGHandle.Edges.Edge;
import com.MDGHandle.Edges.ThreadEdgeType;
import com.MDGHandle.Nodes.Node;
import com.MDGHandle.Nodes.ThreadInterruptNode;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadTriggerNode;
import com.MDGHandle.Nodes.ThreadWaitNode;

public class CASTParser {
	private String projectPath;
	private ArrayList<String> filePathList;
	private HashMap<String, ThreadInformation> threadsInfo;   //线程信息表
	private HashMap<String, ThreadVar> threadVarHashMap;      //线程变量
	/*
	 * 用于记录每个线程运行中包含的函数调用
	 * KEY:methodKey <包_类_函数_参数>
	 * VALUE:线程key集   <包_类> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable; 
	private Set<String> threadRelate;                 		  //线程相关记录
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;  //线程触发节点列表
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
	
	/**
	 * AST树解析，获取并发相关的节点与边的信息
	 */
	public void  parser() {
		System.out.println("parsering");
		//AST树的创建与编译单元组的获取
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		
	
		//MDG依赖边的解析与获取
		triggerParser(compileUnits);                 //1.线程触发边解析
		bindThreadRel();							 //相关线程绑定
//		synchronizeParser(compileUnits);			 //2.同步依赖解析
		communicationParserPre(compileUnits);
		System.out.println("FIRST FINISH");
		communicatinoParserPost(compileUnits);       //3.通信依赖解析
		interruptParser(compileUnits);
//		javaSrcMethod(compileUnits);                 //java源码函数解析
		
	}
	
	/**
	 * 线程启动边解析
	 * @param compileUnits ：编译单元列表
	 */
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
			String threadInfoKey;
			if (!threadVarHashMap.containsKey(threadVarKey)) {
				if (!threadsInfo.containsKey(threadVarKey)) {
					continue;
				}
				//触发节点中直接存储着线程key
				else{
					threadInfoKey = threadVarKey;
				}
			}
			//属于线程变量调用开始
			else{
				threadInfoKey = threadVarHashMap.get(threadVarKey).getThreadInfoKey();
				if (!threadsInfo.containsKey(threadInfoKey)) {
					continue;
				}
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

	/**
	 * 线程同步依赖解析
	 * @param compileUnits ：编译单元列表
	 */
	public void synchronizeParser(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorSynchronize castVisitorSyn = new CASTVisitorSynchronize();
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
				//1.属于同一个类中且对象类型相同
				if(threadNotifyNode.getFileName().equals(threadWaitNode.getFileName())&&
				   threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())){
					Node from = new Node(threadNotifyNode.getFileName(), threadNotifyNode.getLineNumber());
					Node to = new Node(threadWaitNode.getFileName(), threadWaitNode.getLineNumber());
					Edge edge = new Edge(from, to, ThreadEdgeType.THREADSYND);
					edges.add(edge);
				}
				//2.线程相关且对象类型相等
				else if(threadRelate.contains(threadNotifyNode.getClassName()+threadWaitNode.getClassName())&&
						threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())) {
					Node from = new Node(threadNotifyNode.getFileName(), threadNotifyNode.getLineNumber());
					Node to = new Node(threadWaitNode.getFileName(), threadWaitNode.getLineNumber());
					Edge edge = new Edge(from, to, ThreadEdgeType.THREADSYND);
					edges.add(edge);
				}
			}
		}
		for (Edge edge : edges) {
			System.out.println("______________________________________SYND_______________________________________");
			System.out.println(edge);
			System.out.println("______________________________________SYND_______________________________________");
		}
	}
	
	/**
	 * 线程通信依赖解析--函数信息获取(对于每个函数都建立一个线程集)
	 * @param compileUnits ：编译单元列表
	 */
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.先将赋值语句，前缀表达式，后缀表达式会引起变量变化的函数记录下来
		CASTVisitorSrcMethodsPrepare castVisitorPrepare = new CASTVisitorSrcMethodsPrepare();
		castVisitorPrepare.traverse(compileUnits);
		//函数修改变量的信息
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.将函数调用引起变量改变的函数添加进记录表		
		int times = 1;     //迭代次数
		CASTVisitorSrcMethodsCheck castVisitorCheck = new CASTVisitorSrcMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //处理函数信息
			System.out.println("The "+(times++)+"  time");
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
	
	/**
	 * 线程通信依赖解析--Def与Use
	 * @param compileUnits ：编译单元列表
	 */
	public void communicatinoParserPost(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorCommunication castVisitorCommunication = new CASTVisitorCommunication(threadMethodMapTable,threadsInfo);
		do {
			castVisitorCommunication.traverse(compileUnits);
		} while (castVisitorCommunication.isUpdate());

		System.out.println(threadsInfo.size());
		System.out.println(threadMethodMapTable.size());
		
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
		Set<Map.Entry<String, HashSet<String>>> methodSet = threadMethodMapTable.entrySet();
		for (Entry<String, HashSet<String>> entry : methodSet) {
			System.out.println("Method:"+entry.getKey());
			System.out.println("Thread:"+entry.getValue());
		}
		System.out.println(threadRelate);
		for (Entry<String, ThreadInformation> thread1 : threadInfomations) {
			for (Entry<String, ThreadInformation> thread2 : threadInfomations) {
				if (isThreadRelated(thread1.getKey(), thread2.getKey())) {
					System.out.println("THREAD:"+thread1.getKey()+"--TO--"+"THREAD:"+thread2.getKey());
					System.out.println("___________________________COMMUNICATION_____________________________");
					ThreadInformation threadInformation1 = thread1.getValue();
					ThreadInformation threadInformation2 = thread2.getValue();
					ArrayList<ShareVarInfo> threadDef = new ArrayList<>(threadInformation1.getDefList().values());
					ArrayList<ShareVarInfo> threadUse = new ArrayList<>(threadInformation2.getUseList().values());
					for (ShareVarInfo defVar : threadDef) {
						for (ShareVarInfo useVar : threadUse) {
							//类型相同且不属于同一函数
							if (defVar.getType().equals(useVar.getType())&&!defVar.getBelongMethod().equals(useVar.getBelongMethod())) {
								//原始函数做特殊处理(声明所属类，及声明行号相等)
								if (defVar.isPrimitive()&&
									defVar.getBelongClass().equals(useVar.getBelongClass())&&
									defVar.getClassLineNumber()==useVar.getClassLineNumber()) {
									System.out.println("FROM:");
									System.out.println("PATH:"+defVar.getPath());
									System.out.println("LINE:"+defVar.getLineNumber());
									System.out.println(" TO :");
									System.out.println("PATH:"+useVar.getPath());
									System.out.println("LINE:"+useVar.getLineNumber());
									System.out.println();
								}
								//不是原始类型
								else if(!defVar.isPrimitive()){
									System.out.println("FROM:");
									System.out.println("PATH:"+defVar.getPath());
									System.out.println("LINE:"+defVar.getLineNumber());
									System.out.println(" TO :");
									System.out.println("PATH:"+useVar.getPath());
									System.out.println("LINE:"+useVar.getLineNumber());
									System.out.println();
								}
							}
						}
					}
				}
			}
		}
		System.out.println("DONE!");
		System.out.flush();
	}
	
	public void interruptParser(ArrayList<CompileUnit> compileUnits) {
		System.out.println(threadMethodMapTable);
		CASTVisitorInterrupt castVisitorInterrupt = new CASTVisitorInterrupt(threadMethodMapTable, threadsInfo, threadVarHashMap);
		castVisitorInterrupt.traverse(compileUnits);
		
		Set<ThreadInterruptNode> threadInterruptNodes = castVisitorInterrupt.getThreadInterruptNodes();
		//中断通知节点
		for (ThreadInterruptNode threadInterruptNode : threadInterruptNodes) {
			ArrayList<String> threads = threadInterruptNode.getThreadKeyList();
			for (String thread : threads) {
				System.out.println("THREAD: "+thread);
				Set<Node> nodes = threadsInfo.get(thread).getInterruptNodes();
				//中断接受节点
				for (Node node : nodes) {
					System.out.println("___________________________THREADINTERRUPT______________________________");
					System.out.println("FROM:");
					System.out.println("FILE:"+threadInterruptNode.getFileName());
					System.out.println("LINE:"+threadInterruptNode.getLineNumber());
					System.out.println("TO:");
					System.out.println("FILE:"+node.getFileName());
					System.out.println("LINE:"+node.getLineNumber());
					System.out.println("___________________________THREADINTERRUPT______________________________");
				}
			}
		}
	}
	/**
	 * java源码函数解析，用于提取会改变调用对象或参数的函数
	 * @param compileUnits
	 */
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
	
	/**
	 * 打印文件列表
	 */
	public void  printFiles() {
		for (String fileName : filePathList) {
			System.out.println(fileName);
		}
	}
	
	/**
	 * 将相关的线程进行绑定
	 * 并存入线程相关集
	 */
	public void bindThreadRel() {
		ThreadVar[] threadVarArray = new ThreadVar[threadVarHashMap.size()];
		threadVarHashMap.values().toArray(threadVarArray);
		System.out.println("ThreadVarArray :"+threadVarArray.length);
		for (ThreadVar threadVar1 : threadVarArray) {
			for (ThreadVar threadVar2 : threadVarArray) {
				if (threadVar1.getFilePath().equals(threadVar2.getFilePath())&&
					!threadVar1.getBindingTypeName().equals(threadVar2.getBindingTypeName())) {
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
	
	/**
	 * 线程是否相关
	 * @param key1 ：线程1
	 * @param key2: 线程2
	 * @return 是否相关
	 */
	public boolean isThreadRelated(String key1,String key2) {
		return threadRelate.contains(key1+key2)||threadRelate.contains(key2+key1);
	}

}

