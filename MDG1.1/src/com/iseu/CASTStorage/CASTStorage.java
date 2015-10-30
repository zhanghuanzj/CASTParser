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


public class CASTStorage {
	public static GraphDatabaseService database = null;
	public static ExecutionEngine engine;
	public static String DBpath = "D:/graph.db";
	
	public static boolean CreateDataBase(String s) {
		DBpath = s.toString();                                            //路径设置
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
	public static int getMethodID(String filePath,String methodName,int lineNumber) {
		//开启事务
		Transaction transaction = database.beginTx();
		//查询语句
		String query = "MATCH (node:n23) WHERE node.n = \""+
						methodName+"\" AND node.p = \""+
						filePath.replace("\\", "\\\\")+"\" AND node.sl < \""+
						lineNumber+"\" AND node.pl > \""+
						lineNumber+
						"\" RETURN node";
		
		System.out.println(query);
		//获取查询结果
		ExtendedExecutionResult result = engine.execute(query);
		//获取node列
	    ResourceIterator<Node> nodes = result.javaColumnAs("node");
		while(nodes.hasNext()){
			Node node = nodes.next();
			System.out.println(node.getId());
			System.out.println(node.getProperty("n"));
			System.out.println(node.getProperty("p"));
			transaction.success();
			transaction.close();
			return (int) node.getId();
		}	
		return -1;		
	}
	
	public static int getTriggerNodeID(String filePath,String methodName,int lineNumber) {
		int methodID = getMethodID(filePath, methodName, lineNumber);
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
			System.out.println(node.getId());
			return (int) node.getId();
		}
		return -1;
	}
	
	public static void main(String[] args) {
		CASTStorage castStorage = new CASTStorage();
		castStorage.CreateDataBase("D:/graph.db");
		String filePath = "H:\\Projects\\TestCase\\src\\com\\TestCase02\\ForkJoinTest.java";
		String methodName = "accept";
		int lineNumber = 29;
		System.out.println(castStorage.getTriggerNodeID(filePath, methodName, lineNumber));
	}
}
