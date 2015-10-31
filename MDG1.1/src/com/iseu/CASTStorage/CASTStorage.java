package com.iseu.CASTStorage;


import java.io.File;


import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExtendedExecutionResult;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.neo4j.kernel.impl.util.StringLogger;

import storage.DGEdge;


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
		//查询语句
		String query = "MATCH (node:n23) WHERE node.n = \""+
						methodName+"\" AND node.p = \""+
						filePath.replace("\\", "\\\\")+
						"\" RETURN node";
		
		System.out.println(query);
		//获取查询结果
		ExtendedExecutionResult result = engine.execute(query);
		//获取node列
	    ResourceIterator<Node> nodes = result.javaColumnAs("node");
	 
		while(nodes.hasNext()){
			Node node = nodes.next();			
			if (node.hasProperty("sl")&&node.hasProperty("pl")&&
				Integer.parseInt(node.getProperty("sl").toString())<lineNumber&&
				Integer.parseInt(node.getProperty("pl").toString())>lineNumber) {
				transaction.success();
				transaction.close();
				return node.getId();
			}
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
	public static long getTriggerNodeID(String filePath,String methodName,int lineNumber) {
		long methodID = getMethodID(filePath, methodName, lineNumber);
		System.out.println("METHODID:"+methodID);
		//开启事务
		Transaction transaction = database.beginTx();
		//查询语句
		String query = "MATCH (node:n38) WHERE node.mi = \""+
					   methodID+"\" AND node.sl = \""+
					   lineNumber+"\"  RETURN node";
		//获取查询结果
		ExtendedExecutionResult result = engine.execute(query);
		if (result.isEmpty()) {
			System.out.println("result empty");
			return -1;
		}
//		System.out.println(result.dumpToString());        //直接将结果输出了，result被清空
		//获取node列
	    ResourceIterator<Node> nodes = result.javaColumnAs("node");
		while(nodes.hasNext()){
			Node node = nodes.next();
			transaction.success();
			transaction.close();
			return node.getId();
		}
		return -1;
	}
	/**
	 * 通过起始行号获取函数的ID
	 * @param filePath 绝对路径
	 * @param startLine 起始行号
	 * @return 函数节点的数据库ID
	 */
	public static long getMethodID(String filePath,int startLine) {
		//开启事务
		Transaction transaction = database.beginTx();
		//查询语句
		String query = "MATCH (node:n23) WHERE node.p = \""+
						filePath.replace("\\", "\\\\")+"\" AND node.sl = \""+
						startLine+"\" RETURN node";
		System.out.println(query);
		//获取查询结果
		ExtendedExecutionResult result = engine.execute(query);
		//获取node列
	    ResourceIterator<Node> nodes = result.javaColumnAs("node");
		while(nodes.hasNext()){
			Node node = nodes.next();
			transaction.success();
			transaction.close();
			return node.getId();
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
		try (Transaction ts = database.beginTx()) {
			String query = "START a=node("+src+"), b=node("+des+") MATCH a-[threadStart]->b return b";
			System.out.println(query);
			//获取查询结果
			ExtendedExecutionResult result = engine.execute(query);
			//获取node列
		    ResourceIterator<Node> nodes = result.javaColumnAs("node");
		    if (!nodes.hasNext()) {
		    	System.out.println("OK");
		    	Node srcNode = database.getNodeById(src);
				Node tagNode = database.getNodeById(des);
				if ((srcNode == null) || (tagNode == null)) {
					return false;
				}
				srcNode.createRelationshipTo(tagNode, edgeType);
				tagNode.createRelationshipTo(srcNode, edgeType.getReverse());
			}		
			ts.success();
			ts.close();
		}
		return true;
	}
//	public static void main(String[] args) {
//		CASTStorage castStorage = new CASTStorage();
//		castStorage.CreateDataBase("D:/graph.db");
//		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase02\\ForkJoinTest.java";
//		String methodName = "accept";
//		int lineNumber = 29;
//		System.out.println(castStorage.getTriggerNodeID(filePath, methodName, lineNumber));
//	}
}
