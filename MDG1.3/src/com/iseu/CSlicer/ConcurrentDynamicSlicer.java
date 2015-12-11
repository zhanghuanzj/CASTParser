package com.iseu.CSlicer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import org.neo4j.cypher.ExtendedExecutionResult;
//import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;






//import storage.DGEdge;
import storage.NEO4JAccess;

public class ConcurrentDynamicSlicer extends ConcurrentSlicer{
	//public HashMap<Long,HashMap<String, Long>> currentNodesThreadWitness;//��ǰ�ڵ�
	public LinkedList<Long> traverseNodeListOne;
	public LinkedList<Long> traverseNodeListTwo;
	public String waitThreadRelateEdges;

	public ConcurrentDynamicSlicer(String dataBasePath, String rootFilePath) {
		super(dataBasePath, rootFilePath);
		//currentNodesThreadWitness = new HashMap<>();
		traverseNodeListOne = new LinkedList<>();
		traverseNodeListTwo = new LinkedList<>();
		waitThreadRelateEdges="notify|FutureGet|countDown|interrupt";
	}

	/**
	 * ������̬��Ƭ�㷨ʵ��
	 * @param criterion  ����Ƭ׼��
	 */
	public void sliceHandle(SliceCriterion criterion) {
		if(!slicePrepare(criterion)){
			System.out.println("Prepare Exception");
			return;
		}
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
				if (nodes0!=null) {
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY) || isMethodEntryNode(nodeY) || (isMark(nodeY) && (isNeededToWait(nodeY, getNode(nodeIDX),waitThreadRelateEdges) || isNeededToInclude(nodeY, getNode(nodeIDX))))){
								ArrayList<Node> relateNodes = getRelateNode(nodeY);					
								for (Node node : relateNodes) {
									nodeRecordHandle(nodeIDX, node, currentNodes);
								}
							}
						}
					}
				}
				//(2)����������������
				ArrayList<Node> nodes1 = getNodesToSrc(nodeIDX, withoutParameterOutEdges);
				if (nodes1!=null) {
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY) || isMethodEntryNode(nodeY) || isMark(nodeY)){
								nodeRecordHandle(nodeIDX, nodeY, traverseNodeListOne);
							}
						}
					}
				}	
				//(3)����������
				ArrayList<Node> nodes2 = getNodesToSrc(nodeIDX, parameterOutEdge);
				if (nodes2!=null) {
					for (Node nodeY : nodes2) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY) || isMethodEntryNode(nodeY) || isMark(nodeY)){
								nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
							}
						}
					}
				}
				//IDX���̼߳�֤ȥ��
				//currentNodesThreadWitness.remove(nodeIDX);
			}
			//2.StepTwo
			while(!traverseNodeListTwo.isEmpty()){
				Long nodeIDX = traverseNodeListTwo.getFirst();
				traverseNodeListTwo.removeFirst();
				//(1)ͨ��������ͬ��������CountDown,FutureGet,�ж�
				ArrayList<Node> nodes0 = getNodesToSrc(nodeIDX, threadRelateEdges);
				if (nodes0!=null) {
					for (Node nodeY : nodes0) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY) || isMethodEntryNode(nodeY) || (isMark(nodeY) && (isNeededToWait(nodeY, getNode(nodeIDX),waitThreadRelateEdges) || isNeededToInclude(nodeY, getNode(nodeIDX))))){
								ArrayList<Node> relateNodes = getRelateNode(nodeY);	
								for (Node node : relateNodes) {
									nodeRecordHandle(nodeIDX, node, currentNodes);
								}
							}
						}
					}
				}
				//(2)��������������ߺͺ������ñ�
				ArrayList<Node> nodes1 = getNodesToSrc(nodeIDX, withoutParaInAndMethodInvokeEdges);
				if (nodes1!=null) {
					for (Node nodeY : nodes1) {
						Long nodeIDY = nodeY.getId();
						if (!visitedNodes.contains(nodeIDY)) {
							if(isNotStatementNode(nodeY) || isMethodEntryNode(nodeY) || isMark(nodeY)){
								nodeRecordHandle(nodeIDX, nodeY, traverseNodeListTwo);
							}
						}
					}
				}
				//IDX���̼߳�֤ȥ��
				//currentNodesThreadWitness.remove(nodeIDX);
			}
		}
		slicesStorage();
		System.out.println(slices);
	}

	/**
	 * ��Ƭ�ڵ㼯��ʼ��
	 * @param criterion  �� ��Ƭ׼��
	 * @return 
	 */
	public boolean slicePrepare(SliceCriterion criterion) {
		ArrayList<Node> nodes = getNodes(criterion.filePath, criterion.lineNumber);
		for (Node node : nodes) {
			Long nodeID = node.getId();
			visitedNodes.add(nodeID);
			currentNodes.add(nodeID);
			//currentNodesThreadWitness.put(nodeID, new HashMap<>());
			sliceAdd(node);
		}
		try(FileInputStream fileInput = new FileInputStream(criterion.traceFilePath);BufferedReader bufr = new BufferedReader(new InputStreamReader(fileInput))){
			String value=null;
			String []par=null;
			while ((value=bufr.readLine())!=null){
				par=value.split(",");
				propertyReset(par[1],Integer.parseInt(par[2]),Long.parseLong(par[0]));					
			}
			return true;
		}
		catch(Exception e){
			System.out.println("Trace Exception");
			return false;
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
			if(node.hasProperty("FilePath") && node.hasProperty("Startline")){

				String filePath = (String) node.getProperty("FilePath");
				String startLine = (String) node.getProperty("Startline");
				//			String query = "MATCH n WHERE n.Startline="+startLine+" AND n.FilePath="+filePath+" RETURN n";
				String query = "MATCH n WHERE n.FilePath = \""+
						filePath.replace("\\", "\\\\")+"\" AND n.Startline = \""+
						startLine+"\"  RETURN n";

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
	 * �ж��Ƿ�Ϊ������ڽڵ�
	 * @param node : ��ѯ�ڵ�
	 * @return
	 */
	public boolean isMethodEntryNode(Node node) {
		Transaction transaction = database.beginTx();
		try {
			if (!node.hasProperty(NEO4JAccess.ElementMark) && node.hasLabel(NEO4JAccess.StatementNode)) {
				/*String query = "START n=node("+node.getId()+") "//�ڲ���û��parameter
						+ "MATCH n-[:parameter]->b "
						+ "RETURN b";*/
				String query = "START n=node("+node.getId()+") "
						+ "MATCH b-[:controlDepd]->n "
						+ "RETURN b";
				//��ȡ��ѯ���
				System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				ResourceIterator<Node> nodes = result.javaColumnAs("b");
				while(nodes.hasNext()){
					Node n = nodes.next();	
					if(n.hasLabel(NEO4JAccess.InstanceMethodNode))
						return true;
				}		
				transaction.success();
			}
		} finally {
			transaction.close();
		}
		return false;
	}


	/**
	 * �ж�node�ڵ��Ƿ�Ϊ��ִ�нڵ�
	 * @param node   ��    ���жϽڵ�
	 * @return  
	 */
	public boolean isNotStatementNode(Node node) {	
		Transaction transaction = database.beginTx();
		try{
			if(!node.hasLabel(NEO4JAccess.StatementNode)){
				return true;
			}
			return false;
		}
		finally{
			transaction.success();
			transaction.close();
		}
	}

	/**
	 * �ж�node�ڵ��Ƿ񱻹켣���
	 * @param node   ��    ���жϽڵ�
	 * @return  
	 */
	public boolean isMark(Node node) {	
		Transaction transaction = database.beginTx();
		try{
			if(node.hasProperty(NEO4JAccess.ElementMark)){
				return true;
			}
			return false;
		}
		finally{
			transaction.success();
			transaction.close();
		}
	}

	/**
	 * ��ִ���Ⱥ�˳���ж�Y---->X�ڵ���Y�Ƿ�Ӧ�ñ���������
	 * @param nodeY   ��    Դ
	 * @param nodeX   ��    Ŀ
	 * @return  
	 */
	public boolean isNeededToInclude(Node nodeY,Node nodeX) {		
		Transaction transaction = database.beginTx();
		try{
			Long nodeIDX = (Long)nodeX.getProperty(NEO4JAccess.ElementEndID);
			Long nodeIDY = (Long)nodeY.getProperty(NEO4JAccess.ElementStartID);
			return nodeIDY < nodeIDX;
		}
		catch(Exception e){
			return true;//����ڵ�
		}
		finally{
			transaction.success();
			transaction.close();
		}
	}
	
	/**
	 * �ж�Y---->X�ڵ���Ƿ���ָ�����͵ı�
	 * @param nodeY   ��    Դ
	 * @param nodeX   ��    Ŀ
	 * @return  
	 */
	public boolean isNeededToWait(Node nodeY,Node nodeX,String edges){
		Transaction transaction = database.beginTx();
		try{
			String query = "START a=node("+nodeY.getId()+"), b=node("+nodeX.getId()+") MATCH a-[r:"+edges+"]->b return a";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (!result.isEmpty()) {	
				return true;
			}
			return false;
		}
		finally {
			transaction.success();
			transaction.close();
		}
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
				/*********************�̼߳�֤��¼*********************//*

				//currentNodesThreadWitness.put(nodeID,(HashMap<String, Long>)currentNodesThreadWitness.get(src).clone());	

				 *//********************
				 * 1.�ڵ������߳���
				 * 2.witness����
				 * ******************//*
				HashSet<Node> threads = new HashSet<>();
				threadName(node, threads);
				//HashMap<String, Long> threadWitness = currentNodesThreadWitness.get(nodeID);
				for (Node threadNode : threads) {
					String threadName = (String) threadNode.getProperty("Name");
					threadWitness.put(threadName, nodeID);
				}
				System.out.println("ID:"+nodeID+threadWitness);*/
			}
			finally{
				transaction.success();
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
			transaction.success();
			transaction.close();
		}
	}

	/**
	 * ��ǹ켣
	 * @param filePath ����·��
	 * @param lineNumber �ڵ��к�
	 * @param traceId �켣���
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public long propertyReset(String filePath,int lineNumber, long traceId) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH (node:StatementNode) WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//��ȡ��ѯ���
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the statenode");
				return -1;
			}

			//			System.out.println(result.dumpToString());        //ֱ�ӽ��������ˣ�result�����
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();						
				if(node.hasProperty(NEO4JAccess.ElementMark)){
					node.setProperty(NEO4JAccess.ElementEndID, traceId);
				}
				else{
					node.setProperty(NEO4JAccess.ElementMark, 1);
					node.setProperty(NEO4JAccess.ElementStartID, traceId);
					node.setProperty(NEO4JAccess.ElementEndID, traceId);					
				}
				transaction.success();
				return node.getId();
			}
		}	
		finally {			
			transaction.close();
		}
		return -1;
	}

	/*	*//**
	 * ���ش�Դ�ڵ㾭���ض��ߵ���Ľڵ㼯(��ʼ��ʱ�������)
	 * @param src       ��Դ�ڵ�
	 * @param edgeType  ��������
	 *//*
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
	}*/


	/**
	 * ��̬��Ƭ���ĺ���1����(node)
	 * ��¼һ��������ڵ��̣߳����ܱ�����߳�ִ��
	 * @param node     �����ڵ�
	 * @param threads  ���̼߳�(���ڴ洢�߳���)
	 *//*
	public void threadName(Node node,HashSet<Node> threads) {
		if (node==null) {
			return ;
		}
		//��������
		Transaction transaction = database.beginTx();
		try{
			if (node.hasProperty("MethodID")) {
				//��ȡ������ں����ڵ�
				Node methodNode = getNode(Long.valueOf((String) node.getProperty("MethodID")));
				String methodName = (String) methodNode.getProperty("Name");
				//��Ϊ�̺߳���
				if (methodName.equals("run")||methodName.equals("compute")||methodName.equals("call")||methodName.equals("main")) {
					Node classNode = getNode(Long.valueOf((String) methodNode.getProperty("ClassID")));
					threads.add(classNode);
					return;
				}
				else {
					ArrayList<Node> nodes = getNodesToSrcByEdgeType(Long.valueOf((String) node.getProperty("MethodID")), DGEdge.methodInvocation);
					for (Node n : nodes) {
						threadName(n, threads);
					}
				}
			}
			else{
				return;
			}
		}	
		finally {
			transaction.success();
			transaction.close();
		}	
	}*/

	/*	*//**
	 * ��̬��Ƭ���ĺ���2��Reachable(src,des)
	 * @return
	 *//*
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
						//(4)<���ڵ���ͬ��������>�Ƚ�NodeID��С
						if (srcNode.getId()<desNode.getId()||isParentLoop) {
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
					}
				}
			}
		}
		return true;
	}*/

	public static void main(String[] args) {
		String dataBasePath = "E:\\graph.db";
		ConcurrentDynamicSlicer concurrentSlicer = new ConcurrentDynamicSlicer(dataBasePath,"D");
		//Criterion
		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase05\\ThreadInterrupt02.java";
		int lineNumber = 24;
		ArrayList<String> vars = new ArrayList<>();
		vars.add("counter");
		String traceFile = "D:/traceFile.txt";
		SliceCriterion criterion = new SliceCriterion(filePath, lineNumber, vars, traceFile);
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
