package com.iseu.CASTStorage;


import java.io.File;

import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExtendedExecutionResult;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.neo4j.kernel.impl.util.StringLogger;

import storage.DGEdge;
import storage.NEO4JAccess;
import storage.StDGNode;
import storage.StNodeID;


public class CASTStorage {
	public static GraphDatabaseService database = null;
	public static ExecutionEngine engine;
	public static String DBpath ;

	/**
	 * 读取数据库
	 * @param dataBasePath 数据库的路径
	 * @return 
	 */
	public static boolean CreateDataBase(String dataBasePath) {
		DBpath = dataBasePath;                                            //路径设置
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//数据库创建或读取
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("D:/log"))); //查询引擎创建
		return true;
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}


	/**
	 * 获取函数的数据库ID
	 * @param filePath
	 * @param methodName
	 * @param lineNumber
	 * @return methodID
	 */
	public static long getMethodID(String filePath,String methodName,int lineNumber) {
		//开启事务
		Transaction transaction = database.beginTx();
		try {
			//查询语句
			String query = "MATCH (node:InstanceMethodNode) WHERE node.Name = \""+
					methodName+"\" AND node.FilePath = \""+
					filePath.replace("\\", "\\\\")+
					"\" RETURN node";

			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();
				transaction.success();				
				return node.getId();				
			}	
			/*while(nodes.hasNext()){
				Node node = nodes.next();			
				if (node.hasProperty("Startline")&&node.hasProperty("StopLine")&&
						Integer.parseInt(node.getProperty("Startline").toString())<lineNumber&&
						Integer.parseInt(node.getProperty("StopLine").toString())>lineNumber) {
					transaction.success();
					return node.getId();
				}
			}	*/
		}
		finally{
			transaction.close();
		}
		return -1;		
	}

	/**
	 * 获取触发节点ID
	 * @param filePath 绝对路径
	 * @param methodName 函数名，没有包名的函数名如main
	 * @param lineNumber 节点行号
	 * @return 触发节点的数据库ID号，类型为long
	 */
	public static long getStatementNodeID(String filePath,int lineNumber) {
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
				System.out.println("Can not find the statnode");
				return -1;
			}

			//			System.out.println(result.dumpToString());        //直接将结果输出了，result被清空
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();					
				transaction.success();
				return node.getId();
			}
		}	
		finally {
			transaction.close();
		}
		return -1;
	}

	/**
	 * 更新节点的标签
	 * @param src 源节点
	 * @param des 目标节点
	 * @param edgeType 边的类型
	 */
	public static void labelReset(Node src,Node des,DGEdge edgeType) {
		switch (edgeType) {
		case threadStart:
//			src.removeLabel(NEO4JAccess.StatementNode);
			src.addLabel(NEO4JAccess.TriggerNode);
			des.addLabel(NEO4JAccess.ThreadEntryNode);
			break;
		case notify:
//			src.removeLabel(NEO4JAccess.StatementNode);
			src.addLabel(NEO4JAccess.WakeNode);
//			des.removeLabel(NEO4JAccess.StatementNode);
			des.removeLabel(NEO4JAccess.BlockNode);
			break;
		case interrupt:
			src.addLabel(NEO4JAccess.InterruptNotify);
			des.addLabel(NEO4JAccess.InterruptAccept);
			break;
		case authorityAcquire:
			des.addLabel(NEO4JAccess.CriticalEntryNode);
			break;
		case authorityRelease:
			src.addLabel(NEO4JAccess.CriticalExitNode);
			break;
		default:
			break;
		}
	}
	/**
	 * 通过起始行号获取函数的ID
	 * @param filePath 绝对路径
	 * @param Startline 起始行号
	 * @return 函数节点的数据库ID
	 */
	public static long getMethodID(String filePath,int Startline) {
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH (node:InstanceMethodNode) WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					Startline+"\" RETURN node";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();
				transaction.success();
				return node.getId();
			}	
		}
		finally{	
			transaction.close();
		}	
		return -1;	
	}

	/**
	 * 获取类节点ID
	 * @param qulifiedName 绝对路径
	 * @return 触发节点的数据库ID号，类型为long
	 */
	public static long getClassNodeID(String qulifiedName) {
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH (node:ClassNode) WHERE node.Name = \""+
					qulifiedName+"\"  RETURN node";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("result empty");
				return -1;
			}
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();
				transaction.success();
				return node.getId();
			}
		}
		finally{	
			transaction.close();
		}
		return -1;
	}

	/**
	 * 获取触发节点ID
	 * @param qulifiedName 绝对路径
	 * @param memberName 成员变量名
	 * @return 触发节点的数据库ID号，类型为long
	 */
	public static long getMemberNodeID(String qulifiedName,String memberName) {
		long classID = getClassNodeID(qulifiedName);
		System.out.println("ClassID:"+classID);
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH (node:MemberVariableNode) WHERE node.ClassID = \""+
					classID+"\" AND node.Name = \""+
					memberName+"\"  RETURN node";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("result empty");
				return -1;
			}
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();
				transaction.success();
				return node.getId();
			}
		}
		finally{	
			transaction.close();
		}
		return -1;
	}

	/**
	 * 将边的信息存入
	 * @param src 源节点ID
	 * @param des 目标点ID
	 * @param edgeType 边的类型
	 * @return
	 */
	public static boolean store(long src, long des, DGEdge edgeType) {
		System.out.println("SRC:"+src);
		System.out.println("DES:"+des);
		if (src==-1||des==-1) {
			return false;
		}
		Transaction ts = database.beginTx();
		try {
			String query = "START a=node("+src+"), b=node("+des+") MATCH a-["+edgeType+"]->b return b";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			//获取node列
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if (!nodes.hasNext()) {

				Node srcNode = database.getNodeById(src);
				Node desNode = database.getNodeById(des);
				if ((srcNode == null) || (desNode == null)) {
					return false;
				}
				labelReset(srcNode, desNode, edgeType);
				srcNode.createRelationshipTo(desNode, edgeType);
				System.out.println(src+" TO "+des+"store finished\n");
/*				desNode.createRelationshipTo(srcNode, edgeType.getReverse());*/
				ts.success();
			}		
		}
		finally{
			ts.close();
		}
		return true;
	}
	
	/**
	 * 用于线程退出虚拟节点的判断处理
	 * @param methodID :函数在数据库中的ID
	 * @return 如果存在线程退出虚拟节点，则返回相应的ID，否则返回-1
	 */
	public static long threadExitNodeID(long methodID) {
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH (node:ThreadExitNode) WHERE node.MethodID = \""+methodID+"\" RETURN node";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			//获取node列
		    ResourceIterator<Node> nodes = result.javaColumnAs("node");
			if(nodes.hasNext()){
				Node node = nodes.next();
				transaction.success();
				return node.getId();
			}	
		}
		finally{	
			transaction.close();
		}	
		return -1;
	}


	/**
	 * 节点存入
	 * @param node ： node 自己创建的类型节点
	 * @return 新节点的ID
	 */
	public static long store(StDGNode node) {
		long ret = -1;
		Transaction ts = database.beginTx();
		try{
			Node newNode = database.createNode();
			ret =  newNode.getId();
			if (node instanceof StNodeID) {
				((StNodeID) node).setID(ret);
			}
			node.setDGNode(newNode);  //将node节点的信息存入newNode
			ts.success();
		}
		finally {
			ts.close();
		}
		return ret;
	}

}
