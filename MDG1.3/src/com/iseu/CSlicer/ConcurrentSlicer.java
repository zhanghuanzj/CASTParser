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
	public  String DBpath ;                           			//数据库路径
	public  String SliceStoreRootFilePath;           			//工程存储根目录
	public  HashMap<String, TreeSet<Integer>> slices; 			//切片集
	public  HashSet<Long> visitedNodes;               			//访问过的节点
	public  LinkedList<Long> currentNodes;
	//遍历时边的分类
	public  String threadRelateEdges;
	public  String withoutParameterOutEdges;
	public  String parameterOutEdge;
	public  String withoutParaInAndMethodInvokeEdges;
	          
	public  ArrayList<String> forwardEdges;
	
	{
		//跨线程的边集合
		threadRelateEdges = "notify|s_communicate|FutureGet|countDown|interrupt|authorityAcquire|authorityRelease";
		//非跨线程边去除参数输出
		withoutParameterOutEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|dataDepd|inputParameter|methodInvocation|methodImplment|"
				+ "methodOverwrite|interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd|threadStart|theadSecurity|competenceAcquire|competenceRelease|competenceDepd|"
				+ "interClassMessage|anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//非跨线程边去除参数输入及方法调用&线程调用
		withoutParaInAndMethodInvokeEdges = "classMember|controlDepd|instantiation|objMember|parameter|typeAnalysis|dataDepd|outputParameter|methodImplment|methodOverwrite|"
				+ "interfaceImplement|inherit|abstractMember|pkgMember|pkgDepd||theadSecurity|competenceAcquire|competenceRelease|competenceDepd|interClassMessage|"
				+ "anonymousClassDeclare|CFGedge|summaryEdge|dataMember|toObject|threadRisk";
		//参数输出边
		parameterOutEdge = "outputParameter";
	}
	/**
	 * 读取数据库
	 * @param dataBasePath 数据库的路径
	 * @return 
	 */
	public ConcurrentSlicer(String dataBasePath,String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                            //切片存储根路径
		DBpath = dataBasePath;                                            //路径设置
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//数据库创建或读取
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("log"))); //查询引擎创建
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
	 * 通过节点ID获取节点
	 * @param nodeID  ： 节点ID
	 * @return
	 */
	public Node getNode(Long nodeID) {
		if (nodeID<0) {
			return null;
		}
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "START n=NODE("+nodeID+") return n";
			//获取查询结果
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the node");
				return null;
			}
			//获取node列
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
	 * 判断是否为函数入口节点
	 * @param node : 查询节点
	 * @return
	 */
	public boolean isMethodEntryNode(Node node) {
		Transaction transaction = database.beginTx();
		try {
			if (node.hasLabel(NEO4JAccess.StatementNode)) {
				String query = "START n=node("+node.getId()+") "
								+ "MATCH n-[:parameter]->b "
								+ "RETURN b";
				//获取查询结果
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
	 * 获取from--->to的所有路径
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayList<PathImpl> getPaths(Long from,Long to) {
		ArrayList<PathImpl> resultPaths = new ArrayList<>();
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query =  " START a=node("+from+"),b=node("+to+")"+ 
							" MATCH p=a-[:classMember|controlDepd|toObject|typeAnalysis|methodInvocation|FutureGet*]->b"+ 
							" RETURN p";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Path not find!");
				return null;
			}
			//存储着结果P:path1,P:path2...
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
	 * 获取节点集合
	 * @param filePath 绝对路径
	 * @param lineNumber 节点行号
	 * @return 触发节点的数据库ID号，类型为long
	 */
	public ArrayList<Node> getNodes(String filePath,int lineNumber) {
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "MATCH node WHERE node.FilePath = \""+
					filePath.replace("\\", "\\\\")+"\" AND node.Startline = \""+
					lineNumber+"\"  RETURN node";
			//获取查询结果
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the statnode");
				return null;
			}
			//System.out.println(result.dumpToString());直接将结果输出了，result被清空
			//获取node列
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
	  * 由指定边类型到src节点的节点集(反向遍历)
	  * @param src   ： 源节点
	  * @param edges ：变类型集
	  * @param nodeList:节点ID需要插入的集合
	  */
	public ArrayList<Node> getNodesToSrc(Long src,String edges){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//获取node列
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
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
	  * 返回经过特定边到达源节点的节点集
	  * @param src       ：源节点
	  * @param edgeType  ：边类型
	  */
	public ArrayList<Node> getNodesToSrcByEdgeType(Long src,DGEdge edgeType){
		ArrayList<Node> resultNodes = new ArrayList<>();
		//开启事务
		Transaction transaction = database.beginTx();
		try{
			//查询语句
			String query = "START b=node("+src+") MATCH a-[r:"+edgeType+"]->b return a";
			System.out.println(query);
			ExtendedExecutionResult result = engine.execute(query);
			if (result.isEmpty()) {
				System.out.println("Can not find the nodes!");
				return resultNodes;
			}
			//获取node列
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
	 * 切片存储
	 */
	public void slicesStorage() {
		Set<Entry<String, TreeSet<Integer>>> slicesSet = slices.entrySet();
		for (Entry<String, TreeSet<Integer>> entry : slicesSet) {
			String filePath = entry.getKey();
			int startIndex = filePath.indexOf(':');
			//需存储的文件路径
			String newFilePath = SliceStoreRootFilePath+filePath.substring(startIndex);
			int desFileIndex = newFilePath.lastIndexOf('\\');
			//需存储的目录路径
			String newDirPath = newFilePath.substring(0, desFileIndex);
			//目录创建
			File dir = new File(newDirPath);
			makeDir(dir);
			//输出到文件
			sliceOutputToFile(entry,filePath,newFilePath);
		}
	}
	
	/**
	 * 从源文件中读取切到的语句，并将它存储到目标文件中
	 * @param entry
	 * @param srcFilePath   ：    源文件
	 * @param desFilePath   ：    目标文件
	 */
	public void sliceOutputToFile(Entry<String, TreeSet<Integer>> entry,String srcFilePath,String desFilePath) {
		//文件创建
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
	 * 递归创建目录
	 * @param file ： 最底端目录
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
