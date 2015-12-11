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
		try {
			//��ѯ���
			String query = "MATCH (node:InstanceMethodNode) WHERE node.Name = \""+
					methodName+"\" AND node.FilePath = \""+
					filePath.replace("\\", "\\\\")+
					"\" RETURN node";

			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			//��ȡnode��
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
	 * ��ȡ�����ڵ�ID
	 * @param filePath ����·��
	 * @param methodName ��������û�а����ĺ�������main
	 * @param lineNumber �ڵ��к�
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public static long getStatementNodeID(String filePath,int lineNumber) {
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
				System.out.println("Can not find the statnode");
				return -1;
			}

			//			System.out.println(result.dumpToString());        //ֱ�ӽ��������ˣ�result�����
			//��ȡnode��
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
	 * ���½ڵ�ı�ǩ
	 * @param src Դ�ڵ�
	 * @param des Ŀ��ڵ�
	 * @param edgeType �ߵ�����
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
	 * ͨ����ʼ�кŻ�ȡ������ID
	 * @param filePath ����·��
	 * @param Startline ��ʼ�к�
	 * @return �����ڵ�����ݿ�ID
	 */
	public static long getMethodID(String filePath,int Startline) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH (node:InstanceMethodNode) WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					Startline+"\" RETURN node";
			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			//��ȡnode��
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
	 * ��ȡ��ڵ�ID
	 * @param qulifiedName ����·��
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public static long getClassNodeID(String qulifiedName) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH (node:ClassNode) WHERE node.Name = \""+
					qulifiedName+"\"  RETURN node";
			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("result empty");
				return -1;
			}
			//��ȡnode��
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
	 * ��ȡ�����ڵ�ID
	 * @param qulifiedName ����·��
	 * @param memberName ��Ա������
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public static long getMemberNodeID(String qulifiedName,String memberName) {
		long classID = getClassNodeID(qulifiedName);
		System.out.println("ClassID:"+classID);
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH (node:MemberVariableNode) WHERE node.ClassID = \""+
					classID+"\" AND node.Name = \""+
					memberName+"\"  RETURN node";
			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("result empty");
				return -1;
			}
			//��ȡnode��
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
	 * ���ߵ���Ϣ����
	 * @param src Դ�ڵ�ID
	 * @param des Ŀ���ID
	 * @param edgeType �ߵ�����
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
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			//��ȡnode��
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
	 * �����߳��˳�����ڵ���жϴ���
	 * @param methodID :���������ݿ��е�ID
	 * @return ��������߳��˳�����ڵ㣬�򷵻���Ӧ��ID�����򷵻�-1
	 */
	public static long threadExitNodeID(long methodID) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH (node:ThreadExitNode) WHERE node.MethodID = \""+methodID+"\" RETURN node";
			System.out.println(query);
			//��ȡ��ѯ���
			ExtendedExecutionResult result = engine.execute(query);
			//��ȡnode��
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
	 * �ڵ����
	 * @param node �� node �Լ����������ͽڵ�
	 * @return �½ڵ��ID
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
			node.setDGNode(newNode);  //��node�ڵ����Ϣ����newNode
			ts.success();
		}
		finally {
			ts.close();
		}
		return ret;
	}

}
