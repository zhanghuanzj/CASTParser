package com.iseu.CASTParser;

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


import com.iseu.CASTStorage.CASTStorage;
import com.iseu.CASTVistitors.CASTVisitorMethodPre;
import com.iseu.CASTVistitors.CASTVisitorMethodPost;
import com.iseu.CASTVistitors.CASTVisitorCommunication;
import com.iseu.CASTVistitors.CASTVisitorInterrupt;
import com.iseu.CASTVistitors.CASTVisitorJavaMethodsCheck;
import com.iseu.CASTVistitors.CASTVisitorJavaMethodsPrepare;
import com.iseu.CASTVistitors.CASTVisitorSrcMethodsCheck;
import com.iseu.CASTVistitors.CASTVisitorSrcMethodsPrepare;
import com.iseu.CASTVistitors.CASTVisitorSynchronize;
import com.iseu.CASTVistitors.CASTVisitorTest;
import com.iseu.CASTVistitors.CASTVisitorTrigger;
import com.iseu.CASTVistitors.CASTVisitorOther;
import com.iseu.Information.MethodInformation;
import com.iseu.Information.ShareVarInfo;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Edges.Edge;
import com.iseu.MDGHandle.Edges.ThreadEdgeType;
import com.iseu.MDGHandle.Nodes.Node;
import com.iseu.MDGHandle.Nodes.ThreadInterruptNode;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;

import storage.DGEdge;
import storage.NEO4JAccess;
import storage.StThreadExitNode;



public class CASTParser {
	String DBpath;
	private String projectPath;
	private ArrayList<String> filePathList;
	private HashMap<String, ThreadInformation> threadsInfo;   //�߳���Ϣ��
	private HashMap<String, ThreadVar> threadVarHashMap;      //�̱߳���
	private HashMap<String, Integer> threadToIntegerMap;      //�̵߳��߳�IDӳ���
	private ArrayList<Edge> edges;
	/*
	 * ���ڼ�¼ÿ���߳������а����ĺ�������
	 * KEY:methodKey <��_��_����_����>
	 * VALUE:�߳�key��   <��_��> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable; 
	private Set<String> threadRelate;                 		  //�߳���ؼ�¼
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;  //�̴߳����ڵ��б�
	//���캯��
	public CASTParser(String projectPath,String DBpath) {
		this.projectPath = projectPath;
		this.DBpath = DBpath; 
		filePathList = new ArrayList<>();
		threadsInfo = new HashMap<>();	
		threadVarHashMap = new HashMap<>();
		threadTriggerNodes = new ArrayList<>();
		threadMethodMapTable = new HashMap<>();
		threadRelate = new HashSet<>();
		threadToIntegerMap = new HashMap<>();
		edges = new ArrayList<>();
	}
	
	/**
	 * AST����������ȡ������صĽڵ���ߵ���Ϣ
	 */
	public void  parser() {
		System.out.println("parsering");
		CASTStorage.CreateDataBase(DBpath);
		
		//AST���Ĵ�������뵥Ԫ��Ļ�ȡ
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		

	
		//MDG�����ߵĽ������ȡ
		triggerParser(compileUnits);                 //1.�̴߳����߽���
		System.out.println("Total Thread Number:"+threadsInfo.size());
		System.out.println(threadToIntegerMap);
		communicationParserPre(compileUnits);        //2.1ͨ�������������ı����������ȡ
		System.out.println("FIRST FINISH");
		communicatinoParserPost(compileUnits);       //2.2ͨ�����������������̱߳�def&use
		synchronizeParser(compileUnits);			 //3.ͬ����������

		interruptParser(compileUnits);               //4.�ж�����
		otherDependenceAnalyse(compileUnits);        //5.������������
//		javaSrcMethod(compileUnits);                 //javaԴ�뺯������
//		testParser(compileUnits);
		
	}
	
	public void testParser(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorTest castVisitorTest = new CASTVisitorTest();
		castVisitorTest.traverse(compileUnits);
	}
	/**
	 * �߳������߽���
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void triggerParser(ArrayList<CompileUnit> compileUnits) {
		//���ڻ�ȡ�߳���������Ϣ���̱߳��������߳�������
		CASTVisitorTrigger castVisitor = new CASTVisitorTrigger(threadsInfo,threadVarHashMap,threadTriggerNodes,threadMethodMapTable,threadToIntegerMap);
		castVisitor.traverse(compileUnits);
		//��ӡ�߳�����Ϣ
		Set<Map.Entry<String, ThreadInformation>> threadInfomations = threadsInfo.entrySet();
		for (Entry<String, ThreadInformation> entry : threadInfomations) {
			System.out.println("______________________________THREAD_INFO_HASHMAP____________________________________");
			System.out.println("KEY: "+entry.getKey());
			System.out.println(entry.getValue());
			System.out.println("______________________________THREAD_INFO_HASHMAP____________________________________");
		}
		
		//��ӡ�̱߳�����Ϣ
		Set<Map.Entry<String, ThreadVar>> threadVars = threadVarHashMap.entrySet();
		for (Entry<String, ThreadVar> entry : threadVars) {
			System.out.println("______________________________THREAD_VAR_HASHMAP____________________________________");
			System.out.println("KEY: "+entry.getKey());
			System.out.println(entry.getValue());
			System.out.println("______________________________THREAD_VAR_HASHMAP____________________________________");
		}
		
		//��ӡ�̴߳����ڵ�
		for (ThreadTriggerNode threadTiggerNode : threadTriggerNodes) {
			System.out.println("_____________________________TRIGGER_TRIGGER_NODE___________________________________");
			System.out.println(threadTiggerNode);
			System.out.println("_____________________________TRIGGER_TRIGGER_NODE___________________________________");
		}
		
		ArrayList<Edge> edgesList = new ArrayList<>();
		for (ThreadTriggerNode from : threadTriggerNodes) {
			String threadVarKey = from.getThreadVarKey();
			String threadInfoKey;
			if (!threadVarHashMap.containsKey(threadVarKey)) {
				if (!threadsInfo.containsKey(threadVarKey)) {
					continue;
				}
				//1.�����ڵ���ֱ�Ӵ洢���߳�key
				else{
					threadInfoKey = threadVarKey;
				}
			}
			//2.�����̱߳���
			else{
				threadInfoKey = threadVarHashMap.get(threadVarKey).getBindingTypeName();
				if (!threadsInfo.containsKey(threadInfoKey)) {
					continue;
				}
			}
			
			ThreadInformation threadInformation = threadsInfo.get(threadInfoKey);
			if (threadInformation!=null) {
				String filePath = threadInformation.getFilePath();
				int lineNumber = threadInformation.getStartLineNumber();
				//��ȡ���ڵ�����ݿ�ID
				long fromNodeID = CASTStorage.getStatementNodeID(from.getFileName(), from.getLineNumber());
				long toNodeID = CASTStorage.getMethodID(filePath, lineNumber);
//				System.out.println("FROM:"+fromNodeID);
//				System.out.println("TO:"+toNodeID);
				//���ߵ���Ϣ����
				CASTStorage.store(fromNodeID, toNodeID, DGEdge.threadStart);
			}
			System.out.println("Thread Start edge store finished!");
		}
		
		
		/*ArrayList<Edge> edgesList = new ArrayList<>();
		for (ThreadTriggerNode threadTiggerNode : threadTriggerNodes) {
			ThreadTriggerNode from = threadTiggerNode;
			String threadVarKey = from.getThreadVarKey();
			String threadInfoKey;
			if (!threadVarHashMap.containsKey(threadVarKey)) {
				if (!threadsInfo.containsKey(threadVarKey)) {
					continue;
				}
				else{
					threadInfoKey = threadVarKey;
				}
			}
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
				Node to = new Node(null,filePath, lineNumber);
				Edge threadStartEdge = new Edge(from, to, ThreadEdgeType.THREADTRIGGER);
				edgesList.add(threadStartEdge);
			}
		}
		System.out.println("Total trigger edge is:"+edgesList.size());
		for (Edge edge : edgesList) {
			System.out.println("______________________________________TRIGGER_______________________________________");
			System.out.println(edge);
			System.out.println("______________________________________TRIGGER_______________________________________");
		}*/
	}

	/**
	 * �߳�ͬ����������
	 * @param compileUnits �����뵥Ԫ�б�
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
				//1.����ͬһ�������Ҷ���������ͬ
				System.out.println("F1:"+threadNotifyNode.getFileName());
				System.out.println("F2:"+threadWaitNode.getFileName());
				if(threadNotifyNode.getFileName().equals(threadWaitNode.getFileName())&&
				   threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())){
					long from = CASTStorage.getStatementNodeID(threadNotifyNode.getFileName(),  threadNotifyNode.getLineNumber());
					long to = CASTStorage.getStatementNodeID(threadWaitNode.getFileName(),  threadWaitNode.getLineNumber());
					CASTStorage.store(from, to, DGEdge.notify);		
				}
				//2.�߳�����Ҷ����������
				else if(isThreadRelated(threadNotifyNode.getClassName(), threadWaitNode.getClassName())&&
						threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())) {
						long from = CASTStorage.getStatementNodeID(threadNotifyNode.getFileName(),  threadNotifyNode.getLineNumber());
						long to = CASTStorage.getStatementNodeID(threadWaitNode.getFileName(),  threadWaitNode.getLineNumber());
						CASTStorage.store(from, to, DGEdge.notify);	
				}
			}
		}
		//�����ڵ�Ŀ�����������
		HashMap<Node, Node> blockNodes = castVisitorSyn.getBlockControlDependence();
		Set<Entry<Node, Node>> blockNodesSet = blockNodes.entrySet();
		for (Entry<Node, Node> entry : blockNodesSet) {
			System.out.println("BLOCKFROM:\n"+entry.getKey());
			System.out.println("BLOCKTO:\n"+entry.getValue());
			long from = CASTStorage.getStatementNodeID(entry.getKey().getFileName(), entry.getKey().getLineNumber());
			long to = CASTStorage.getStatementNodeID(entry.getValue().getFileName(), entry.getValue().getLineNumber());
			CASTStorage.store(from,to,DGEdge.controlDepd);
		}
		System.out.println("Synchronize dependence store finished!");
	}
	
	/**
	 * �߳�ͨ����������--������Ϣ��ȡ(�������Ƿ��޸ı���)
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.�Ƚ���ֵ��䣬ǰ׺���ʽ����׺���ʽ����������仯�ĺ�����¼����
		CASTVisitorMethodPre castVisitorPrepare = new CASTVisitorMethodPre();
		castVisitorPrepare.traverse(compileUnits);
		//�����޸ı�������Ϣ
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.������������������ı�ĺ�����ӽ���¼��		
		int times = 1;     //��������
		CASTVisitorMethodPost castVisitorCheck = new CASTVisitorMethodPost(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //��������Ϣ
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //�����Щ�Ե��ö���&&����û���޸ĵĺ���
		ArrayList<String> keys = new ArrayList<>();
		Set<Map.Entry<String, MethodInformation>> methodInformations = changeMethods.entrySet();
		for (Entry<String, MethodInformation> entry : methodInformations) {
			if (!entry.getValue().isObjChange()&&!entry.getValue().isAnyParaChange()) {
				keys.add(entry.getKey());
			}
		}
		for (String  key : keys) {
			changeMethods.remove(key);
		}
		
		//������Ϣ�����뱣��
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //ӳ���<���������>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //�������ñ���滻�ĺ�����Ϣ��
		int keyNumber = 0;
		try {
			File dir = new File("srcMethodsInfo\\");
			if (dir.exists()) {
				if (!deleteDirFiles(dir)) {
					System.err.println("File delete error!");
				}
				else{
					System.out.println("File delete sucess!");
				}
			}
			if (dir.mkdirs()) {
				PrintWriter methodsInfoOutToFile = new PrintWriter("srcMethodsInfo\\Methods.txt");
				PrintWriter methodsMapTableOutToFile = new PrintWriter("srcMethodsInfo\\methodMapTable.txt");
				File file = new File("srcMethodsInfo\\srcMethodMapTable.obj");
				File file2 = new File("srcMethodsInfo\\srcMethodInfo.obj");
				FileOutputStream fileOut = new FileOutputStream(file);
				FileOutputStream fileOut2 = new FileOutputStream(file2);
				
				//��������Ϣд���ļ���������������ӳ���
				for (Entry<String, MethodInformation> entry : methodInformationsNew) {
					//�ҵ�����
					String mapKey = entry.getKey().substring(0, entry.getKey().indexOf('_'));
					if (!methodMapTable.containsKey(mapKey)) {
						methodMapTable.put(mapKey, keyNumber++);
					}
					methodsInfoOutToFile.println(entry.getKey());
					methodsInfoOutToFile.print(entry.getValue());
					//��ֵΪ<�����_����_�������������б�>
					javaMethodInfo.put(methodMapTable.get(mapKey)+entry.getKey().substring(entry.getKey().indexOf('_')), entry.getValue());
				}
				methodsInfoOutToFile.flush();
				methodsInfoOutToFile.close();
				
				//��ӳ���д���ļ�
				Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
				for (Entry<String, Integer> entry : mapTableSet) {
					methodsMapTableOutToFile.println(entry.getKey());
					methodsMapTableOutToFile.println(entry.getValue());
				}
				methodsMapTableOutToFile.flush();
				methodsMapTableOutToFile.close();
				System.out.println("The total mapTable size is: "+mapTableSet.size());
				
				//��ӳ����Զ������ʽд���ļ�srcMethodMapTable.obj
				try {
					ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
					objectOut.writeObject(methodMapTable);
					objectOut.flush();
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//��java������Ϣ�Զ������ʽд���ļ�srcMethodInfo.obj
				try {
					ObjectOutputStream objectOut2 = new ObjectOutputStream(fileOut2);
					objectOut2.writeObject(javaMethodInfo);
					objectOut2.flush();
					objectOut2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("After reduce the methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
	
	/**
	 * �߳�ͨ����������--Def��Use(����ÿ������������һ���̼߳�)
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void communicatinoParserPost(ArrayList<CompileUnit> compileUnits) {
		CASTVisitorCommunication castVisitorCommunication = new CASTVisitorCommunication(threadMethodMapTable,threadsInfo,threadVarHashMap);
		do {
			castVisitorCommunication.traverse(compileUnits);
		} while (castVisitorCommunication.isUpdate());

		System.out.println(threadsInfo.size());
		System.out.println(threadMethodMapTable.size());
		
		bindThreadRel();			//����̰߳�
		
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
							//������ͬ�Ҳ�����ͬһ����
							if (defVar.getType().equals(useVar.getType())&&!defVar.getBelongMethod().equals(useVar.getBelongMethod())) {
								if (defVar.getBelongClass().equals(useVar.getBelongClass())&&
									defVar.getVariableID()==useVar.getVariableID()) {
									System.out.println("FROM:");
									System.out.println("PATH:"+defVar.getPath());
									System.out.println("LINE:"+defVar.getLineNumber());
									System.out.println(" TO :");
									System.out.println("PATH:"+useVar.getPath());
									System.out.println("LINE:"+useVar.getLineNumber());
									System.out.println();
									long from = CASTStorage.getStatementNodeID(defVar.getPath()	, defVar.getLineNumber());
									long to = CASTStorage.getStatementNodeID(useVar.getPath(),  useVar.getLineNumber());
									CASTStorage.store(from, to, DGEdge.s_communicate);
								}
								else if(defVar.getBelongClass().equals("METHODVAR")||useVar.getBelongClass().equals("METHODVAR")) {
									System.out.println("FROM:");
									System.out.println("PATH:"+defVar.getPath());
									System.out.println("LINE:"+defVar.getLineNumber());
									System.out.println(" TO :");
									System.out.println("PATH:"+useVar.getPath());
									System.out.println("LINE:"+useVar.getLineNumber());
									System.out.println();
									long from = CASTStorage.getStatementNodeID(defVar.getPath()	, defVar.getLineNumber());
									long to = CASTStorage.getStatementNodeID(useVar.getPath(),  useVar.getLineNumber());
									CASTStorage.store(from, to, DGEdge.s_communicate);
								}
							}
						}
					}
				}
			}
		}
		System.out.println("Communication Dependence store finished DONE!");
		System.out.flush();
	}
	
	public void interruptParser(ArrayList<CompileUnit> compileUnits) {
		System.out.println(threadMethodMapTable);
		CASTVisitorInterrupt castVisitorInterrupt = new CASTVisitorInterrupt(threadMethodMapTable, threadsInfo, threadVarHashMap);
		castVisitorInterrupt.traverse(compileUnits);
		
		Set<ThreadInterruptNode> threadInterruptNodes = castVisitorInterrupt.getThreadInterruptNodes();
		//�ж�֪ͨ�ڵ�
		for (ThreadInterruptNode threadInterruptNode : threadInterruptNodes) {
			ArrayList<String> threads = threadInterruptNode.getThreadKeyList();
			for (String thread : threads) {
				System.out.println("THREAD: "+thread);
				Set<Node> nodes = threadsInfo.get(thread).getInterruptNodes();
				//�жϽ��ܽڵ�
				for (Node node : nodes) {
					System.out.println("___________________________THREADINTERRUPT______________________________");
					System.out.println("FROM:");
					System.out.println("FILE:"+threadInterruptNode.getFileName());
					System.out.println("LINE:"+threadInterruptNode.getLineNumber());
					System.out.println("TO:");
					System.out.println("FILE:"+node.getFileName());
					System.out.println("LINE:"+node.getLineNumber());
					System.out.println("___________________________THREADINTERRUPT______________________________");
					long from = CASTStorage.getStatementNodeID(threadInterruptNode.getFileName(), threadInterruptNode.getLineNumber());
					long to = CASTStorage.getStatementNodeID(node.getFileName(), node.getLineNumber());
					CASTStorage.store(from, to, DGEdge.interrupt);
				}
			}
		}
		System.out.println("Thread Interrupt edge store finished!");
	}
	
	public void otherDependenceAnalyse(ArrayList<CompileUnit> compileUnits) {
		String MD="MethodDeclaration";
		String TD="TypeDeclaration";
		String MEMBERVARIABLE="MemberVariable";
		System.out.println("��ʼ����...");
		HashMap<String,String> hm=new HashMap<String,String>();
		CASTVisitorOther visitor = new CASTVisitorOther(hm,true,edges);
		visitor.traverse(compileUnits);
		visitor = new CASTVisitorOther(hm,false,edges);
		visitor.traverse(compileUnits);
		System.out.println("�������!");
		System.out.println("��ʼ�洢...");
		for(Edge edge : edges){
			Node from=edge.getFrom();
			Node to=edge.getTo();
			System.out.println(edge.getEdgeType()+"From:");
			System.out.println(from.getFileName()+"+++++++++"+from.getMethodName()+"+++++++++"+from.getLine());
			System.out.println("To:");
			System.out.println(to.getFileName()+"+++++++++"+to.getMethodName()+"+++++++++"+to.getLine());

			long fromNodeID=-1;
			String fromLine=from.getLine();
			String toLine=to.getLine();
			String name=from.getMethodName();
			if(name.equals(TD)){
				fromNodeID=CASTStorage.getClassNodeID(from.getFileName());
			}
			else if(fromLine.equals(MEMBERVARIABLE)){
				fromNodeID=CASTStorage.getMemberNodeID(from.getFileName(),from.getMethodName());
			}
			else if(name.equals(MD)){
				fromNodeID=CASTStorage.getMethodID(from.getFileName(),Integer.parseInt(from.getLine()));
			}
			else{
				fromNodeID = CASTStorage.getStatementNodeID(from.getFileName(),  Integer.parseInt(from.getLine()));
			}
			if(fromNodeID != -1){
				long toNodeID=-1;
				if(toLine.equals(MEMBERVARIABLE)){//Ȩ���ͷű�ʱ,toNode����Ϊ��Ա�����ڵ�
					toNodeID=CASTStorage.getMemberNodeID(to.getFileName(),to.getMethodName());
				}
				else{
					toNodeID = CASTStorage.getStatementNodeID(to.getFileName(),  Integer.parseInt(to.getLine()));
				}
				if(toNodeID != -1){
					System.out.println("FROM:"+fromNodeID);
					System.out.println("TO:"+toNodeID);

					//���ߵ���Ϣ����
					if(edge.getEdgeType().equals(DGEdge.FutureGet)){//�贴������ڵ�
						long methodID=CASTStorage.getMethodID(from.getFileName(), from.getMethodName(), Integer.parseInt(from.getLine()));
						long exitNodeID=-1;
						if(methodID != -1){
							exitNodeID=(int)CASTStorage.threadExitNodeID(methodID);
						}
						if(exitNodeID == -1){//����ڵ㲻����
							StThreadExitNode exitNode=new StThreadExitNode(0,"",methodID);
							exitNodeID=CASTStorage.store(exitNode);
						}
						CASTStorage.store(fromNodeID, exitNodeID, edge.getEdgeType());
						CASTStorage.store(exitNodeID, toNodeID, edge.getEdgeType());
					}
					else{
						CASTStorage.store(fromNodeID, toNodeID, edge.getEdgeType());
					}
				}
				else{
					System.out.println("toNode����");
				}
			}
			else{
				System.out.println("fromNode����");
			}
			System.out.println();
		}
		System.out.println("�洢���!");
	}
	/**
	 * javaԴ�뺯��������������ȡ��ı���ö��������ĺ���
	 * @param compileUnits
	 */
	public void javaSrcMethod(ArrayList<CompileUnit> compileUnits) {
		// 1.�Ƚ���ֵ��䣬ǰ׺���ʽ����׺���ʽ����������仯�ĺ�����¼����
		CASTVisitorMethodPre castVisitorTest = new CASTVisitorMethodPre();
		castVisitorTest.traverse(compileUnits);
		//�����޸ı�������Ϣ
		HashMap<String, MethodInformation> changeMethods = castVisitorTest.getChangeMethods();
		// 2.������������������ı�ĺ�����ӽ���¼��
		int times = 1;     //��������
		CASTVisitorMethodPost castVisitorCheck = new CASTVisitorMethodPost(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //��������Ϣ
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //�����Щ�Ե��ö��󼰲���û���޸ĵĺ���
		ArrayList<String> keys = new ArrayList<>();
		Set<Map.Entry<String, MethodInformation>> methodInformations = changeMethods.entrySet();
		for (Entry<String, MethodInformation> entry : methodInformations) {
			if (!entry.getValue().isObjChange()&&!entry.getValue().isAnyParaChange()) {
				keys.add(entry.getKey());
			}
		}
		for (String  key : keys) {
			changeMethods.remove(key);
		}
		
		//������Ϣ�����뱣��
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //ӳ���<���������>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //�������ñ���滻�ĺ�����Ϣ��
		int keyNumber = 0;
		File dir = new File("javaMethodsInfo\\");
		if (dir.exists()) {
			if (!deleteDirFiles(dir)) {
				System.err.println("File delete error!");
			}
			else{
				System.out.println("File delete sucess!");
			}
		}
		if (dir.mkdirs()) {
			try {
				PrintWriter methodsInfoOutToFile = new PrintWriter("javaMethodsInfo\\javaMethods.txt");
				PrintWriter methodsMapTableOutToFile = new PrintWriter("javaMethodsInfo\\javaMethodMapTable.txt");
				File file = new File("javaMethodsInfo\\javaMethodMapTable.obj");
				File file2 = new File("javaMethodsInfo\\javaMethodInfo.obj");
				FileOutputStream fileOut = new FileOutputStream(file);
				FileOutputStream fileOut2 = new FileOutputStream(file2);
				
				//��������Ϣд���ļ���������������ӳ���
				for (Entry<String, MethodInformation> entry : methodInformationsNew) {
					//�ҵ�����
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
				
				//��ӳ���д���ļ�
				Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
				for (Entry<String, Integer> entry : mapTableSet) {
					methodsMapTableOutToFile.println(entry.getKey());
					methodsMapTableOutToFile.println(entry.getValue());
				}
				methodsMapTableOutToFile.flush();
				methodsMapTableOutToFile.close();
				System.out.println("The total mapTable size is: "+mapTableSet.size());
				
				//��ӳ����Զ������ʽд���ļ�javaMethodMapTable.obj
				try {
					ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
					objectOut.writeObject(methodMapTable);
					objectOut.flush();
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//��java������Ϣ�Զ������ʽд���ļ�javaMethodInfo.obj
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
		}
		
		System.out.println("The methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
	
	/**
	 * ��ӡ�ļ��б�
	 */
	public void  printFiles() {
		for (String fileName : filePathList) {
			System.out.println(fileName);
		}
	}
	
	/**
	 * ����ص��߳̽��а�
	 * �������߳���ؼ�
	 */
	public void bindThreadRel() {
		ThreadVar[] threadVarArray = new ThreadVar[threadVarHashMap.size()];
		threadVarHashMap.values().toArray(threadVarArray);
		System.out.println("ThreadVarArray :"+threadVarArray.length);
		//�߳�A�����߳�B����A��B���
		for (ThreadVar threadVar1 : threadVarArray) {
			Set<String> thread1s = threadMethodMapTable.get(threadVar1.getThreadMethodKey());  //��ȡ����1���ں������̼߳�
			int  thread1ID = threadID(threadVar1.getBindingTypeName());
			for (String threadName : thread1s) {
				int threadID = threadID(threadName);
				if (thread1ID!=threadID&&thread1ID!=-1&&threadID!=-1) {
					threadRelate.add(thread1ID+"_"+threadID);
					threadRelate.add(threadID+"_"+thread1ID);
				}
			}
			//�̱߳��������ں���������õ�������߳����
			for (ThreadVar threadVar2 : threadVarArray) {
				Set<String> thread2s = threadMethodMapTable.get(threadVar2.getThreadMethodKey());  //��ȡ����2���ں������̼߳�
				int thread2ID = threadID(threadVar2.getBindingTypeName());
				//��������̼߳����������̲߳���ͬһ�߳�
				if (threadMethodMapTable.containsKey(threadVar1.getThreadMethodKey())&&
					threadMethodMapTable.containsKey(threadVar2.getThreadMethodKey())&&
					!threadVar1.getBindingTypeName().equals(threadVar2.getBindingTypeName())) {
					Set<String> set = new HashSet<>();
					System.out.println(thread1s);
					System.out.println(thread2s);
					set.addAll(thread1s);
					set.retainAll(thread2s);
					//���̼߳��н���
					if (!set.isEmpty()) {
						threadRelate.add(thread1ID+"_"+thread2ID);
						threadRelate.add(thread2ID+"_"+thread1ID);
					}				
				}
			}
		}
		System.out.println("Total ThreadRelate :"+threadRelate.size());
		for (String threadRel : threadRelate) {
			System.out.println("ThreadRelate:::"+threadRel);
		}
	}
	
	/**
	 * �߳��Ƿ����
	 * @param key1 ���߳�1
	 * @param key2: �߳�2
	 * @return �Ƿ����
	 */
	public boolean isThreadRelated(String key1,String key2) {
		System.out.println(threadID(key1)+"_"+threadID(key2));
		return threadRelate.contains(threadID(key1)+"_"+threadID(key2))||threadRelate.contains(threadID(key2)+"_"+threadID(key1));
	}
	
	public int threadID(String threadKey) {
		if (threadKey==null||!threadToIntegerMap.containsKey(threadKey)) {
			return -1;
		}
		else{
			return threadToIntegerMap.get(threadKey);
		}
	}

	/**
	 * �ݹ�ɾ��Ŀ¼��Ŀ¼�µ��ļ�
	 * @param file ���ļ�
	 * @return ɾ���Ƿ�ɹ�
	 */
	public boolean deleteDirFiles(File file) {
		if (file.isDirectory()) {
			String[] fileNames = file.list();
			for(int i=0;i<fileNames.length;++i){
				if (!deleteDirFiles(new File(file, fileNames[i]))) {
					return false;
				}
			}
		}
		return file.delete();
	}
}

