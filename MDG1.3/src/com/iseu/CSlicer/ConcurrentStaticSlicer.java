package com.iseu.CSlicer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.iseu.MDGHandle.Edges.Edge;

import storage.DGEdge;
import storage.NEO4JAccess;

public class ConcurrentStaticSlicer extends ConcurrentSlicer{
	public HashMap<Long,HashMap<String, Long>> currentNodesThreadWitness;//��ǰ�ڵ�
	public LinkedList<Long> traverseNodeListOne;
	public LinkedList<Long> traverseNodeListTwo;
	
	public ConcurrentStaticSlicer(String dataBasePath, String rootFilePath) {
		super(dataBasePath, rootFilePath);
		currentNodesThreadWitness = new HashMap<>();
		traverseNodeListOne = new LinkedList<>();
		traverseNodeListTwo = new LinkedList<>();
	}

	/**
	 * ������̬��Ƭ�㷨ʵ��
	 * @param criterion  ����Ƭ׼��
	 */
	public void sliceHandle(SliceCriterion criterion) {
		slicePrepare(criterion);
		System.out.println(currentNodes);
		System.out.println(visitedNodes);	
		while(!currentNodes.isEmpty()){
			Long nodeID = currentNodes.getFirst();
			currentNodes.removeFirst();
			traverseNodeListOne.add(nodeID);
			//1.StepOne
			while(!traverseNodeListOne.isEmpty()){
				Long nodeIDX = traverseNodeListOne.getFirst();
				traverseNodeListOne.removeFirst();
				//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�
				ArrayList<Node> nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges);
				for (Node nodeY : nodes0) {
					Long nodeIDY = nodeY.getId();
					if (!visitedNodes.contains(nodeIDY)) {
						if (isNeededToInclude(nodeY, getNode(nodeIDX))) {
							ArrayList<Node> relateNodes = getRelateNode(nodeY);
							for (Node node : relateNodes) {
								nodeRecordHandle(nodeIDX, node, currentNodes);
							}
						}
					}
				}
				//(2)����������������
				ArrayList<Node> nodes1 = getNodesToSrc(nodeIDX, withoutParameterOutEdges);
				for (Node nodeY : nodes1) {
					Long nodeIDY = nodeY.getId();
					if (!visitedNodes.contains(nodeIDY)) {
						nodeRecordHandle(nodeIDX, nodeY, traverseNodeListOne);
					}
				}	
				//(3)����������
				ArrayList<Node> nodes2 = getNodesToSrc(nodeIDX, parameterOutEdge);
				for (Node nodeY : nodes2) {
					Long nodeIDY = nodeY.getId();
					if (!visitedNodes.contains(nodeIDY)) {
						nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
					}
				}

				//IDX���̼߳�֤ȥ��
				currentNodesThreadWitness.remove(nodeIDX);
			}
			//2.StepTwo
			while(!traverseNodeListTwo.isEmpty()){
				Long nodeIDX = traverseNodeListTwo.getFirst();
				traverseNodeListTwo.removeFirst();
				//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�
				ArrayList<Node> nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges);
				for (Node nodeY : nodes0) {
					Long nodeIDY = nodeY.getId();
					if (!visitedNodes.contains(nodeIDY)) {
						if (isNeededToInclude(nodeY, getNode(nodeIDX))) {
							ArrayList<Node> relateNodes = getRelateNode(nodeY);
							for (Node node : relateNodes) {
								nodeRecordHandle(nodeIDX, node, currentNodes);
							}
						}
					}
				}

				//(2)��������������ߺͺ������ñ�
				ArrayList<Node> nodes1 = getNodesToSrc(nodeIDX, withoutParaInAndMethodInvokeEdges);
				for (Node nodeY : nodes1) {
					Long nodeIDY = nodeY.getId();
					if (!visitedNodes.contains(nodeIDY)) {
						nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
					}
				}
				//IDX���̼߳�֤ȥ��
				currentNodesThreadWitness.remove(nodeIDX);
			}
		}
		slicesStorage();
		System.out.println(slices);
	}
	
	/**
	 * ��Ƭ�ڵ㼯��ʼ��
	 * @param criterion  �� ��Ƭ׼��
	 */
	public void slicePrepare(SliceCriterion criterion) {
		ArrayList<Node> nodes = getNodes(criterion.filePath, criterion.lineNumber);
		if (nodes==null) {
			return;
		}
		for (Node node : nodes) {
			Long nodeID = node.getId();
			visitedNodes.add(nodeID);
			currentNodes.add(nodeID);
			//�̼߳�֤��ʼ��
			HashMap<String, Long> hashMap = new HashMap<>();
			HashSet<Node> hashSet = new HashSet<>();
			threadName(node, hashSet);
			Transaction transaction = database.beginTx();	
			if (!hashSet.isEmpty()) {
				for (Node n : hashSet) {
					try {
						hashMap.put((String) n.getProperty("Name"), nodeID);
					} finally {
//						transaction.success();
						transaction.close();
					}
				}
			}	
			currentNodesThreadWitness.put(nodeID, hashMap);
			System.out.println(currentNodesThreadWitness);
			sliceAdd(node);
		}
	}
	
	/**
	 * �����ڵ����������ڵ��������	
	 * @param node  ��  ���ڵ�
	 * @return
	 */
	public ArrayList<Node> getRelateNode(Node node) {
		ArrayList<Node> resultNodes = new ArrayList<>();
		if (node==null) {
			return resultNodes;
		}
		Transaction transaction = database.beginTx();
		try {
			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
				String filePath = (String) node.getProperty("FilePath");
				String startLine = (String) node.getProperty("Startline");
				String query = "MATCH n WHERE n.Startline=\""+startLine+"\" AND n.FilePath=\""+filePath.replace("\\", "\\\\")+"\" RETURN n";
				System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				if (result.isEmpty()) {
					System.out.println("Relate Nodes not find!");
					return resultNodes;
				}
				//��ȡnode��
				ResourceIterator<Node> nodes = result.javaColumnAs("n");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					resultNodes.add(n);
				}
			}
			else{
				resultNodes.add(node);
			}
			transaction.success();
			return resultNodes;
		} finally {
			transaction.close();
		}
	}
	
	/**
	 * �ж�Y---->X�ڵ���Y�Ƿ�Ӧ�ñ���������(�̼߳�)
	 * @param nodeY   ��    Դ
	 * @param nodeX   ��    Ŀ
	 * @return  
	 */
	public boolean isNeededToInclude(Node nodeY,Node nodeX) {
		Long nodeIDX = nodeX.getId();
		Long nodeIDY = nodeY.getId();
		HashMap<String, Long> witness = currentNodesThreadWitness.get(nodeIDX);
		HashSet<Node> threads = new HashSet<>();
		threadName(nodeY, threads);
		System.out.println("ThreadNames:"+threads);
		System.out.println("WITNESS:"+witness);
		//��������
		Transaction transaction = database.beginTx();
		try{
			System.out.println("Size:"+threads.size());
			for (Node node : threads) {
				String threadName = (String) node.getProperty("Name");
				//T.has(��(s)&&Reachable(y,T[��(s)]))|| !T.has(��(s)) 
				if (!witness.containsKey(threadName)||
					(witness.containsKey(threadName)&&reachable(nodeY,getNode(witness.get(threadName)), node))) {
						return true;
					}
			}
		}
		finally{
//			transaction.success();
			transaction.close();
		}
		return false;
	}
	
	/**
	 * �ڵ��¼����
	 * 1.���ʹ����ô���
	 * 2.������Ƭ(·�����к�)
	 * 3.�����̼߳�֤
	 * @param src  :�ڵ��ǰ��
	 * @param node ���������Ľڵ�
	 */
	public void nodeRecordHandle(Long src,Node node,LinkedList<Long> nodeList){
		Long nodeID = node.getId();
		if (visitedNodes.contains(nodeID)) {
			return;
		}
		else{
			visitedNodes.add(nodeID);
			nodeList.add(nodeID);
			//��������
			Transaction transaction = database.beginTx();
			try{
				sliceAdd(node);
				/*********************�̼߳�֤��¼*********************/
				
				currentNodesThreadWitness.put(nodeID,(HashMap<String, Long>)currentNodesThreadWitness.get(src).clone());	
				
				/********************
				 * 1.�ڵ������߳���
				 * 2.witness����
				 * ******************/
				HashSet<Node> threads = new HashSet<>();
				threadName(node, threads);
				HashMap<String, Long> threadWitness = currentNodesThreadWitness.get(nodeID);
				for (Node threadNode : threads) {
					String threadName = (String) threadNode.getProperty("Name");
					threadWitness.put(threadName, nodeID);
				}
//				transaction.success();
				System.out.println("ID:"+nodeID+threadWitness);
			}
			finally{
				transaction.close();
			}
		}
	}
	
	/**
	 * ���ڵ�����������뵽��Ƭ����
	 * @param node
	 */
	public void sliceAdd(Node node) {
		if (node==null) {
			return ;
		}
		//��������
		Transaction transaction = database.beginTx();
		try{
			if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
				String filePath = (String)node.getProperty("FilePath");
				Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
				if (startLine<=0) {
					return;
				}
				if (slices.containsKey(filePath)) {
					slices.get(filePath).add(startLine);
				}
				else{
					TreeSet<Integer> fileLineSet = new TreeSet<>();
					fileLineSet.add(startLine);
					slices.put(filePath, fileLineSet);
				}
			}
		}
		finally{
//			transaction.success();
			transaction.close();
		}
	}
	
	/**
	  * ���ش�Դ�ڵ㾭���ض��ߵ���Ľڵ㼯(��ʼ��ʱ�������)
	  * @param src       ��Դ�ڵ�
	  * @param edgeType  ��������
	  */
	public void getNodesFromSrcByEdgeType(Long src,DGEdge edgeType){
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "START a=node("+src+") MATCH a-[r:"+edgeType+"]->b return b";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return ;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("b");
			while(nodes.hasNext()){
				Node node = nodes.next();	
				nodeRecordHandle(src,node,currentNodes);
				System.out.println(node.getId());
			}
			transaction.success();
		}	
		finally {
			transaction.close();
		}
	}
	
	
	/**
	 * ��̬��Ƭ���ĺ���1����(node)
	 * ��¼һ��������ڵ��̣߳����ܱ�����߳�ִ��
	 * @param node     �����ڵ�
	 * @param threads  ���̼߳�(���ڴ洢�߳���)
	 */
	public void threadName(Node node,HashSet<Node> threads) {
		if (node==null) {
			return ;
		}
		//��������
		Transaction transaction = database.beginTx();
		try{
			if (node.hasProperty("MethodID")) {
				long methodID = Long.valueOf((String) node.getProperty("MethodID"));
				if (methodID<0) {
					return ;
				}
				//��ȡ������ں����ڵ�
				Node methodNode = getNode(methodID);
				String methodName = (String) methodNode.getProperty("Name");
				System.out.println("MethodNode:"+methodNode);
				System.out.println("MethodName:"+methodName);
				//��Ϊ�̺߳���
				if (methodName.equals("run")||methodName.equals("compute")||methodName.equals("call")) {
					Node classNode = getNode(Long.valueOf((String) methodNode.getProperty("ClassID")));
					threads.add(classNode);			
					return;
				}
				else if (methodName.equals("main")) {
					threads.add(methodNode);			
					return;
				}
				else {
					ArrayList<Node> nodes = getNodesToSrcByEdgeType(Long.valueOf((String) node.getProperty("MethodID")), DGEdge.methodInvocation);
					for (Node n : nodes) {
						threadName(n, threads);
					}
				}
//				transaction.success();
			}
			else{
				Node reNode = getObjectNode(node.getId(), DGEdge.typeAnalysis);
				threadName(reNode, threads);
				return;
			}
		}	
		finally {
			
			transaction.close();
		}	
	}
	
	public Node getObjectNode(Long src,DGEdge edgeType) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH a-[r:"+edgeType+"]->b return a";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return null;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			if(nodes.hasNext()){
				Node node = nodes.next();
				return node;
			}			
		}	
		finally {
			transaction.success();
			transaction.close();
		}
		return null;
	}
	/**
	 * ��̬��Ƭ���ĺ���2��Reachable(src,des)
	 * @return
	 */
	public boolean reachable(Node src,Node des,Node threadNode) {
		//1.ID�������ж�
		Long srcID = src.getId();
		Long desID = des.getId();
		Long thrID = threadNode.getId();
		if (srcID==-1||desID==-1||thrID==-1) {
			return false;
		}
		//2.�ֱ��ȡ�߳̽ڵ㵽���ڵ��·��
		ArrayList<PathImpl> srcPaths = getPaths(thrID, srcID);
		ArrayList<PathImpl> desPaths = getPaths(thrID, desID);
		//3.��·���ıȽ�
		if (desPaths==null||srcPaths==null) {
			return false;
		}
		for (PathImpl srcPath : srcPaths) {
			//(1)ThreadNode-------->srcNode:PATH
			Iterator<Node> srcNodes = srcPath.nodes().iterator();
			for (PathImpl desPath : desPaths) {
				//(2)ThreadNode-------->desNode:PATH
				Iterator<Node> desNodes = desPath.nodes().iterator();
				//��¼���ڵ��Ƿ����ѭ��
				boolean isParentLoop = false;
				while (desNodes.hasNext()&&srcNodes.hasNext()) {
					Node srcNode = (Node) srcNodes.next();
					Node desNode = (Node) desNodes.next();
					//(3)·���ڵ�ID�Ƿ���ͬ
					if (srcNode.getId()!=desNode.getId()) {
						System.out.println("SRC:"+srcNode.getId());
						System.out.println("DES:"+desNode.getId());
						//(4)<���ڵ���ͬ��������>�Ƚ�NodeID��С
						if (srcNode.getId()<desNode.getId()||isParentLoop) {
							System.out.println(srcID+" can reach to "+desID);
							return true;
						}
						else{
							break;
						}
					}
					else{
						//(5)���ڵ�ѭ����Ǵ���
						if (srcNode.hasLabel(NEO4JAccess.LoopNode)) {
							isParentLoop = true;
						}
						else{
							isParentLoop = false;
						}
					}
				}
			}
		}
		System.out.println(srcID+" can not reach to "+desID);
		return false;
	}

	public static void main(String[] args) {
		String dataBasePath = "E:\\graph.db";
		ConcurrentStaticSlicer concurrentSlicer = new ConcurrentStaticSlicer(dataBasePath,"E");
		//Criterion
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase01\\Storage.java";
//		int lineNumber = 41;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("list");

		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase02\\ForkJoinTest.java";
//		int lineNumber = 21;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("counter");
	
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase03\\SemaphoreTest.java";
//		int lineNumber = 27;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase04\\ThreadInterrupt01.java";
//		int lineNumber = 9;
//		ArrayList<String> vars = new ArrayList<>();
//		vars.add("semp");
		
		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase05\\ThreadInterrupt02.java";
		int lineNumber = 37;
		ArrayList<String> vars = new ArrayList<>();
		vars.add("semp");
		
		SliceCriterion criterion = new SliceCriterion(filePath, lineNumber, vars);
		//SliceHandle
		concurrentSlicer.sliceHandle(criterion);
//		ArrayList<PathImpl> result = concurrentSlicer.getPaths((long)1, (long)97);
//		for (PathImpl pathImpl : result) {
//			Iterator<Node> nodes = pathImpl.nodes().iterator();
//			while (nodes.hasNext()) {
//				Node node = (Node) nodes.next();
//				System.out.println(node.getId());
//			}
//		}
	}
	
}
