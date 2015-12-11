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
	//public HashMap<Long,HashMap<String, Long>> currentNodesThreadWitness;//当前节点
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
	 * 并发静态切片算法实现
	 * @param criterion  ：切片准则
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
				//(1)通信依赖，同步依赖，CountDown,FutureGet,中断
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
				//(2)不包括参数传出边
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
				//(3)参数传出边
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
				//IDX的线程见证去除
				//currentNodesThreadWitness.remove(nodeIDX);
			}
			//2.StepTwo
			while(!traverseNodeListTwo.isEmpty()){
				Long nodeIDX = traverseNodeListTwo.getFirst();
				traverseNodeListTwo.removeFirst();
				//(1)通信依赖，同步依赖，CountDown,FutureGet,中断
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
				//(2)不包括参数传入边和函数调用边
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
				//IDX的线程见证去除
				//currentNodesThreadWitness.remove(nodeIDX);
			}
		}
		slicesStorage();
		System.out.println(slices);
	}

	/**
	 * 切片节点集初始化
	 * @param criterion  ： 切片准则
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
	 * 将语句节点的所有虚拟节点包含进来	
	 * @param node  ：  语句节点
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
				//获取node列
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
	 * 判断是否为函数入口节点
	 * @param node : 查询节点
	 * @return
	 */
	public boolean isMethodEntryNode(Node node) {
		Transaction transaction = database.beginTx();
		try {
			if (!node.hasProperty(NEO4JAccess.ElementMark) && node.hasLabel(NEO4JAccess.StatementNode)) {
				/*String query = "START n=node("+node.getId()+") "//内部类没有parameter
						+ "MATCH n-[:parameter]->b "
						+ "RETURN b";*/
				String query = "START n=node("+node.getId()+") "
						+ "MATCH b-[:controlDepd]->n "
						+ "RETURN b";
				//获取查询结果
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
	 * 判断node节点是否为可执行节点
	 * @param node   ：    被判断节点
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
	 * 判断node节点是否被轨迹标记
	 * @param node   ：    被判断节点
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
	 * 按执行先后顺序判断Y---->X节点中Y是否应该被包含进来
	 * @param nodeY   ：    源
	 * @param nodeX   ：    目
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
			return true;//虚拟节点
		}
		finally{
			transaction.success();
			transaction.close();
		}
	}
	
	/**
	 * 判断Y---->X节点间是否是指定类型的边
	 * @param nodeY   ：    源
	 * @param nodeX   ：    目
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
	 * 节点记录处理
	 * 1.访问过则不用处理
	 * 2.存入切片(路径：行号)
	 * 3.更新线程见证
	 * @param src  :节点的前驱
	 * @param node ：遍历到的节点
	 */
	public void nodeRecordHandle(Long src,Node node,LinkedList<Long> nodeList){
		Long nodeID = node.getId();
		if (visitedNodes.contains(nodeID)) {
			return;
		}
		else{
			visitedNodes.add(nodeID);
			nodeList.add(nodeID);
			//开启事务
			Transaction transaction = database.beginTx();
			try{
				sliceAdd(node);
				/*********************线程见证记录*********************//*

				//currentNodesThreadWitness.put(nodeID,(HashMap<String, Long>)currentNodesThreadWitness.get(src).clone());	

				 *//********************
				 * 1.节点所在线程名
				 * 2.witness更新
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
	 * 将节点关联的语句加入到切片集中
	 * @param node
	 */
	public void sliceAdd(Node node) {
		if (node==null) {
			return ;
		}
		//开启事务
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
	 * 标记轨迹
	 * @param filePath 绝对路径
	 * @param lineNumber 节点行号
	 * @param traceId 轨迹序号
	 * @return 触发节点的数据库ID号，类型为long
	 */
	public long propertyReset(String filePath,int lineNumber, long traceId) {
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH (node:StatementNode) WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//获取查询结果
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the statenode");
				return -1;
			}

			//			System.out.println(result.dumpToString());        //直接将结果输出了，result被清空
			//获取node列
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
	 * 返回从源节点经过特定边到达的节点集(初始化时正向遍历)
	 * @param src       ：源节点
	 * @param edgeType  ：边类型
	 *//*
	public void getNodesFromSrcByEdgeType(Long src,DGEdge edgeType){
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "START a=node("+src+") MATCH a-[r:"+edgeType+"]->b return b";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return ;
			}
			//获取node列
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
	 * 静态切片核心函数1：Θ(node)
	 * 记录一条语句所在的线程，可能被多个线程执行
	 * @param node     ：语句节点
	 * @param threads  ：线程集(用于存储线程名)
	 *//*
	public void threadName(Node node,HashSet<Node> threads) {
		if (node==null) {
			return ;
		}
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			if (node.hasProperty("MethodID")) {
				//获取语句所在函数节点
				Node methodNode = getNode(Long.valueOf((String) node.getProperty("MethodID")));
				String methodName = (String) methodNode.getProperty("Name");
				//若为线程函数
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
	 * 静态切片核心函数2：Reachable(src,des)
	 * @return
	 *//*
	public boolean reachable(Node src,Node des,Node threadNode) {
		//1.ID合理性判断
		Long srcID = src.getId();
		Long desID = des.getId();
		Long thrID = threadNode.getId();
		if (srcID==-1||desID==-1||thrID==-1) {
			return false;
		}
		//2.分别获取线程节点到两节点的路径
		ArrayList<PathImpl> srcPaths = getPaths(thrID, srcID);
		ArrayList<PathImpl> desPaths = getPaths(thrID, desID);
		//3.两路径的比较
		for (PathImpl srcPath : srcPaths) {
			//(1)ThreadNode-------->srcNode:PATH
			Iterator<Node> srcNodes = srcPath.nodes().iterator();
			for (PathImpl desPath : desPaths) {
				//(2)ThreadNode-------->desNode:PATH
				Iterator<Node> desNodes = desPath.nodes().iterator();
				//记录父节点是否可以循环
				boolean isParentLoop = false;
				while (desNodes.hasNext()&&srcNodes.hasNext()) {
					Node srcNode = (Node) srcNodes.next();
					Node desNode = (Node) desNodes.next();
					//(3)路径节点ID是否相同
					if (srcNode.getId()!=desNode.getId()) {
						//(4)<父节点相同的情形下>比较NodeID大小
						if (srcNode.getId()<desNode.getId()||isParentLoop) {
							return true;
						}
						else{
							break;
						}
					}
					else{
						//(5)父节点循环标记处理
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
