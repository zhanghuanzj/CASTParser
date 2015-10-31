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
	 * ��ȡ���ݿ�
	 * @param dataBasePath ���ݿ��·��
	 * @return 
	 */
	public static boolean CreateDataBase(String dataBasePath) {
		DBpath = dataBasePath;                                            //·������
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//���ݿⴴ�����ȡ
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("D:/log"))); //��ѯ���洴��

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
	 * ��ȡ���������ݿ�ID
	 * @param filePath
	 * @param methodName
	 * @param lineNumber
	 * @return methodID
	 */
	public static long getMethodID(String filePath,String methodName,int lineNumber) {
		//��������
		Transaction transaction = database.beginTx();
		//��ѯ���
		String query = "MATCH (node:n23) WHERE node.n = \""+
						methodName+"\" AND node.p = \""+
						filePath.replace("\\", "\\\\")+
						"\" RETURN node";
		
		System.out.println(query);
		//��ȡ��ѯ���
		ExtendedExecutionResult result = engine.execute(query);
		//��ȡnode��
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
	 * ��ȡ�����ڵ�ID
	 * @param filePath ����·��
	 * @param methodName ��������û�а����ĺ�������main
	 * @param lineNumber �ڵ��к�
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public static long getTriggerNodeID(String filePath,String methodName,int lineNumber) {
		long methodID = getMethodID(filePath, methodName, lineNumber);
		System.out.println("METHODID:"+methodID);
		//��������
		Transaction transaction = database.beginTx();
		//��ѯ���
		String query = "MATCH (node:n38) WHERE node.mi = \""+
					   methodID+"\" AND node.sl = \""+
					   lineNumber+"\"  RETURN node";
		//��ȡ��ѯ���
		ExtendedExecutionResult result = engine.execute(query);
		if (result.isEmpty()) {
			System.out.println("result empty");
			return -1;
		}
//		System.out.println(result.dumpToString());        //ֱ�ӽ��������ˣ�result�����
		//��ȡnode��
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
	 * ͨ����ʼ�кŻ�ȡ������ID
	 * @param filePath ����·��
	 * @param startLine ��ʼ�к�
	 * @return �����ڵ�����ݿ�ID
	 */
	public static long getMethodID(String filePath,int startLine) {
		//��������
		Transaction transaction = database.beginTx();
		//��ѯ���
		String query = "MATCH (node:n23) WHERE node.p = \""+
						filePath.replace("\\", "\\\\")+"\" AND node.sl = \""+
						startLine+"\" RETURN node";
		System.out.println(query);
		//��ȡ��ѯ���
		ExtendedExecutionResult result = engine.execute(query);
		//��ȡnode��
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
	 * ���ߵ���Ϣ����
	 * @param src Դ�ڵ�ID
	 * @param des Ŀ���ID
	 * @param edgeType �ߵ�����
	 * @return
	 */
	public static boolean store(long src, long des, DGEdge edgeType) {
		try (Transaction ts = database.beginTx()) {
			String query = "START a=node("+src+"), b=node("+des+") MATCH a-[threadStart]->b return b";
			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			//��ȡnode��
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
