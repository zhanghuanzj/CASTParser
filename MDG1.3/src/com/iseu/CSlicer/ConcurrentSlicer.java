package com.iseu.CSlicer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.cypher.internal.compiler.v2_2.helpers.StringRenderingSupport;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.shell.impl.SystemOutput;

import scala.deprecated;
import storage.DGEdge;
import storage.NEO4JAccess;

public class ConcurrentSlicer {
	public  GraphDatabaseService database = null;
	public  ExecutionEngine engine;
	public  String DBpath ;                           			//���ݿ�·��
	public  String SliceStoreRootFilePath;           			//���̴洢��Ŀ¼
	public  HashMap<String, TreeSet<Integer>> slices; 			//��Ƭ��
	public  HashSet<Long> visitedNodes;               			//���ʹ��Ľڵ�
	public  LinkedList<Long> currentNodes;
	//����ʱ�ߵķ���
	public  String threadRelateEdges;
	public  String withoutParameterOutEdges;
	public  String parameterOutEdge;
	public  String withoutParaInAndMethodInvokeEdges;
	          
	public  ArrayList<String> forwardEdges;
	
	{
		//���̵߳ı߼���
		threadRelateEdges = "notify|s_communicate|FutureGet|countDown|interrupt|authorityAcquire|authorityRelease";
		//�ǿ��̱߳�ȥ���������
		withoutParameterOutEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|dataDepd|inputParameter|methodInvocation|methodImplment|"
				+ "methodOverwrite|interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd|threadStart|theadSecurity|competenceAcquire|competenceRelease|competenceDepd|"
				+ "interClassMessage|anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//�ǿ��̱߳�ȥ���������뼰��������&�̵߳���
		withoutParaInAndMethodInvokeEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|dataDepd|outputParameter|methodImplment|methodOverwrite|"
				+ "interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd||theadSecurity|competenceAcquire|competenceRelease|competenceDepd|interClassMessage|"
				+ "anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//���������
		parameterOutEdge = "outputParameter";
	}
	/**
	 * ��ȡ���ݿ�
	 * @param dataBasePath ���ݿ��·��
	 * @return 
	 */
	public ConcurrentSlicer(String dataBasePath,String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                            //��Ƭ�洢��·��
		DBpath = dataBasePath;                                            //·������
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//���ݿⴴ�����ȡ
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("log"))); //��ѯ���洴��
		slices = new HashMap<>();
		visitedNodes = new HashSet<>();
		currentNodes = new LinkedList<>();
	}
	
	private void registerShutdownHook(final GraphDatabaseService graphDb) {
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
	 * ͨ���ڵ�ID��ȡ�ڵ�
	 * @param nodeID  �� �ڵ�ID
	 * @return
	 */
	public Node getNode(Long nodeID) {
		if (nodeID<0) {
			return null;
		}
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "START n=NODE("+nodeID+") return n";
			//��ȡ��ѯ���
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the node");
				return null;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("n");
			if(nodes.hasNext()){
				Node node = nodes.next();
				System.out.println(node);
				transaction.success();
				return node;
			}
		}	
		finally {
			transaction.close();
		}
		System.out.println("NULL");
		return null;
	}

	/**
	 * �ж��Ƿ�Ϊ������ڽڵ�
	 * @param node : ��ѯ�ڵ�
	 * @return
	 */
	public boolean isMethodEntryNode(Node node) {
		Transaction transaction = database.beginTx();
		try {
			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				String query = "START n=node("+node.getId()+") "
								+ "MATCH n-[:parameter]->b "
								+ "RETURN b";
				//��ȡ��ѯ���
				System.out.println(query);
				ExtendedExecutionResult result = engine.execute(query);
				if (!result.isEmpty()) {
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
	 * ��ȡfrom--->to������·��
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayList<PathImpl> getPaths(Long from,Long to) {
		ArrayList<PathImpl> resultPaths = new ArrayList<>();
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query =  " START a=node("+from+"),b=node("+to+")"+ 
							" MATCH p=a-[:classMember|controlDepd|toObject|typeAnalysis|methodInvocation|FutureGet*]->b"+ 
							" RETURN p";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Path not find!");
				return null;
			}
			//�洢�Ž��P:path1,P:path2...
			ResourceIterator<Map<String, Object>> nodeResult = result.javaIterator();
			while (nodeResult.hasNext()) {
				Map<String, Object> map = (Map<String, Object>) nodeResult.next();
				System.out.println(map);
				Collection<Object> nodes = map.values();
				for (Object object : nodes) {
					System.out.println(object.getClass());
					if (object instanceof PathImpl) {
						PathImpl path = (PathImpl)object;
						resultPaths.add(path);
					}
				}
			}
			transaction.success();
		}	
		finally {
			transaction.close();
		}
		return resultPaths;
	}
	


	/**
	 * ��ȡ�ڵ㼯��
	 * @param filePath ����·��
	 * @param lineNumber �ڵ��к�
	 * @return �����ڵ�����ݿ�ID�ţ�����Ϊlong
	 */
	public ArrayList<Node> getNodes(String filePath,int lineNumber) {
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "MATCH node WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//��ȡ��ѯ���
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the statnode");
				return null;
			}
			//System.out.println(result.dumpToString());ֱ�ӽ��������ˣ�result�����
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("node");
			ArrayList<Node> resultNodes = new ArrayList<>();
			while(nodes.hasNext()){
				Node node = nodes.next();
				resultNodes.add(node);
			}
			transaction.success();
			return resultNodes;
		}	
		finally {
			transaction.close();
		}
	}

	 /**
	  * ��ָ�������͵�src�ڵ�Ľڵ㼯(�������)
	  * @param src   �� Դ�ڵ�
	  * @param edges �������ͼ�
	  * @param nodeList:�ڵ�ID��Ҫ����ļ���
	  */
	public ArrayList<Node> getNodesToSrc(Long src,String edges){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��ȡnode��
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH a-[r:"+edges+"]->b return a";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return resultNodes;
			}
			
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			while(nodes.hasNext()){
				Node node = nodes.next();
				System.out.println(node);
				if (node.hasProperty("Startline")) {
					if (Integer.valueOf((String)node.getProperty("Startline"))==43) {
						System.out.println("SRCNODEID:"+src);
					}
				}
				
				resultNodes.add(node);
			}
			transaction.success();
			System.out.println(resultNodes.size());
			return resultNodes;
		}
		finally {
			transaction.close();
		}
	}
	
	 /**
	  * ���ؾ����ض��ߵ���Դ�ڵ�Ľڵ㼯
	  * @param src       ��Դ�ڵ�
	  * @param edgeType  ��������
	  */
	public ArrayList<Node> getNodesToSrcByEdgeType(Long src,DGEdge edgeType){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//��������
		Transaction transaction = database.beginTx();
		try{
			//��ѯ���
			String query = "START b=node("+src+") MATCH a-[r:"+edgeType+"]->b return a";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return resultNodes;
			}
			//��ȡnode��
			ResourceIterator<Node> nodes = result.javaColumnAs("a");
			while(nodes.hasNext()){
				Node node = nodes.next();	
				resultNodes.add(node);
				System.out.println(node.getId());
			}
			transaction.success();
		}	
		finally {
			transaction.close();
		}
		return resultNodes;
	}

	
	/**
	 * ��Ƭ�洢
	 */
	public void slicesStorage() {
		Set<Entry<String, TreeSet<Integer>>> slicesSet = slices.entrySet();
		for (Entry<String, TreeSet<Integer>> entry : slicesSet) {
			String filePath = entry.getKey();
			int startIndex = filePath.indexOf(':');
			//��洢���ļ�·��
			String newFilePath = SliceStoreRootFilePath+filePath.substring(startIndex);
			int desFileIndex = newFilePath.lastIndexOf('\\');
			//��洢��Ŀ¼·��
			String newDirPath = newFilePath.substring(0, desFileIndex);
			//Ŀ¼����
			File dir = new File(newDirPath);
			makeDir(dir);
			//������ļ�
			sliceOutputToFile(entry,filePath,newFilePath);
		}
	}
	
	/**
	 * ��Դ�ļ��ж�ȡ�е�����䣬�������洢��Ŀ���ļ���
	 * @param entry
	 * @param srcFilePath   ��    Դ�ļ�
	 * @param desFilePath   ��    Ŀ���ļ�
	 */
	public void sliceOutputToFile(Entry<String, TreeSet<Integer>> entry,String srcFilePath,String desFilePath) {
		//�ļ�����
		File file = new File(desFilePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter printWriter = new PrintWriter(file);
			BufferedReader srcFileReader = new BufferedReader(
										   new InputStreamReader(
										   new FileInputStream(
										   new File(srcFilePath))));
			TreeSet<Integer> fileSlices = entry.getValue();
			int lastLineNumber = entry.getValue().last();
			for(int i=1;i<=lastLineNumber;++i){
				if (fileSlices.contains(i)) {
					try {
						String line = srcFileReader.readLine();
						printWriter.println(line);
						printWriter.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						String line = srcFileReader.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(entry.getValue().last());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ݹ鴴��Ŀ¼
	 * @param file �� ��׶�Ŀ¼
	 */
	public void makeDir(File file){
		if(file.getParentFile().exists()){
		   file.mkdir();
		}
		else{
		   makeDir(file.getParentFile());
		   file.mkdir();
	    }
	}
	
}