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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExtendedExecutionResult;
import org.neo4j.cypher.internal.compiler.v2_2.PathImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.StringLogger;

import storage.DGEdge;
import storage.NEO4JAccess;

public class SliceStorage {
	public  GraphDatabaseService database = null;
	public  ExecutionEngine engine;
	public  String DBpath ;                           			//数据库路径
	public  String SliceStoreRootFilePath;           			//工程存储根目录
	public  HashMap<String, TreeSet<Integer>> slices; 			//切片集

	
	public SliceStorage(String dataBasePath,String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                            //切片存储根路径
		DBpath = dataBasePath;                                            //数据库路径
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//数据库创建或读取
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("log"))); //查询引擎创建
		slices = new HashMap<>();
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
	

	public void sliceHandle(ArrayList<Node> nodes) {
		Transaction transaction = database.beginTx();
		try {
			for (Node node : nodes) {
				if (node.hasProperty("FilePath")&&node.hasProperty("Startline")) {
					String filePath = (String)node.getProperty("FilePath");
					Integer startLine = Integer.valueOf((String) node.getProperty("Startline"));
					if (startLine<=0) {
						continue;
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
			slicesStorage();
		} finally {
			transaction.close();
		}
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
