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
	private HashMap<String, ThreadInformation> threadsInfo;   //�߳���Ϣ��
	private HashMap<String, ThreadVar> threadVarHashMap;      //�̱߳���
	/*
	 * ���ڼ�¼ÿ���߳������а����ĺ�������
	 * KEY:methodKey <��_��_����_����>
	 * VALUE:�߳�key��   <��_��> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable; 
	private Set<String> threadRelate;                 		  //�߳���ؼ�¼
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;  //�̴߳����ڵ��б�
	//���캯��
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
	 * AST����������ȡ������صĽڵ���ߵ���Ϣ
	 */
	public void  parser() {
		System.out.println("parsering");
		//AST���Ĵ�������뵥Ԫ��Ļ�ȡ
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		
	
		//MDG�����ߵĽ������ȡ
		triggerParser(compileUnits);                 //1.�̴߳����߽���
		bindThreadRel();							 //����̰߳�
//		synchronizeParser(compileUnits);			 //2.ͬ����������
		communicationParserPre(compileUnits);
		System.out.println("FIRST FINISH");
		communicatinoParserPost(compileUnits);       //3.ͨ����������
		interruptParser(compileUnits);
//		javaSrcMethod(compileUnits);                 //javaԴ�뺯������
		
	}
	
	/**
	 * �߳������߽���
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void triggerParser(ArrayList<CompileUnit> compileUnits) {
		//���ڻ�ȡ�߳���������Ϣ���̱߳��������߳�������
		CASTVisitorTrigger castVisitor = new CASTVisitorTrigger(threadsInfo,threadVarHashMap,threadTriggerNodes,threadMethodMapTable);
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
		for (ThreadTriggerNode threadTiggerNode : threadTriggerNodes) {
			ThreadTriggerNode from = threadTiggerNode;
			String threadVarKey = from.getThreadVarKey();
			String threadInfoKey;
			if (!threadVarHashMap.containsKey(threadVarKey)) {
				if (!threadsInfo.containsKey(threadVarKey)) {
					continue;
				}
				//�����ڵ���ֱ�Ӵ洢���߳�key
				else{
					threadInfoKey = threadVarKey;
				}
			}
			//�����̱߳������ÿ�ʼ
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
		//��ӡ��
		for (Edge edge : edgesList) {
			System.out.println("______________________________________TRIGGER_______________________________________");
			System.out.println(edge);
			System.out.println("______________________________________TRIGGER_______________________________________");
		}
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
				if(threadNotifyNode.getFileName().equals(threadWaitNode.getFileName())&&
				   threadNotifyNode.getObjectTypeName().equals(threadWaitNode.getObjectTypeName())){
					Node from = new Node(threadNotifyNode.getFileName(), threadNotifyNode.getLineNumber());
					Node to = new Node(threadWaitNode.getFileName(), threadWaitNode.getLineNumber());
					Edge edge = new Edge(from, to, ThreadEdgeType.THREADSYND);
					edges.add(edge);
				}
				//2.�߳�����Ҷ����������
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
	 * �߳�ͨ����������--������Ϣ��ȡ(����ÿ������������һ���̼߳�)
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.�Ƚ���ֵ��䣬ǰ׺���ʽ����׺���ʽ����������仯�ĺ�����¼����
		CASTVisitorSrcMethodsPrepare castVisitorPrepare = new CASTVisitorSrcMethodsPrepare();
		castVisitorPrepare.traverse(compileUnits);
		//�����޸ı�������Ϣ
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.������������������ı�ĺ�����ӽ���¼��		
		int times = 1;     //��������
		CASTVisitorSrcMethodsCheck castVisitorCheck = new CASTVisitorSrcMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //��������Ϣ
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //�����Щ�Ե��ö���&&����û���޸ĵĺ���
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
		
		//������Ϣ�����뱣��
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //ӳ���<���������>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //�������ñ���滻�ĺ�����Ϣ��
		int keyNumber = 0;
		try {
			PrintWriter methodsInfoOutToFile = new PrintWriter("Methods.txt");
			PrintWriter methodsMapTableOutToFile = new PrintWriter("methodMapTable.txt");
			File file = new File("srcMethodMapTable.obj");
			File file2 = new File("srcMethodInfo.obj");
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("After reduce the methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
	
	/**
	 * �߳�ͨ����������--Def��Use
	 * @param compileUnits �����뵥Ԫ�б�
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
							//������ͬ�Ҳ�����ͬһ����
							if (defVar.getType().equals(useVar.getType())&&!defVar.getBelongMethod().equals(useVar.getBelongMethod())) {
								//ԭʼ���������⴦��(���������࣬�������к����)
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
								//����ԭʼ����
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
				}
			}
		}
	}
	/**
	 * javaԴ�뺯��������������ȡ��ı���ö��������ĺ���
	 * @param compileUnits
	 */
	public void javaSrcMethod(ArrayList<CompileUnit> compileUnits) {
		// 1.�Ƚ���ֵ��䣬ǰ׺���ʽ����׺���ʽ����������仯�ĺ�����¼����
		CASTVisitorJavaMethodsPrepare castVisitorTest = new CASTVisitorJavaMethodsPrepare();
		castVisitorTest.traverse(compileUnits);
		//�����޸ı�������Ϣ
		HashMap<String, MethodInformation> changeMethods = castVisitorTest.getChangeMethods();
		// 2.������������������ı�ĺ�����ӽ���¼��
		int times = 1;     //��������
		CASTVisitorJavaMethodsCheck castVisitorCheck = new CASTVisitorJavaMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //��������Ϣ
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //�����Щ�Ե��ö��󼰲���û���޸ĵĺ���
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
		
		//������Ϣ�����뱣��
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //ӳ���<���������>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //�������ñ���滻�ĺ�����Ϣ��
		int keyNumber = 0;
		try {
			PrintWriter methodsInfoOutToFile = new PrintWriter("javaMethods.txt");
			PrintWriter methodsMapTableOutToFile = new PrintWriter("javaMethodMapTable.txt");
			File file = new File("javaMethodMapTable.obj");
			File file2 = new File("javaMethodInfo.obj");
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
	 * �߳��Ƿ����
	 * @param key1 ���߳�1
	 * @param key2: �߳�2
	 * @return �Ƿ����
	 */
	public boolean isThreadRelated(String key1,String key2) {
		return threadRelate.contains(key1+key2)||threadRelate.contains(key2+key1);
	}

}

