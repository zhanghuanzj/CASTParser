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
	public  String DBpath ;                           			//���ݿ�·��
	public  String SliceStoreRootFilePath;           			//���̴洢��Ŀ¼
	public  HashMap<String, TreeSet<Integer>> slices; 			//��Ƭ��

	
	public SliceStorage(String dataBasePath,String rootFilePath) {
		SliceStoreRootFilePath = rootFilePath;                            //��Ƭ�洢��·��
		DBpath = dataBasePath;                                            //���ݿ�·��
		database = new GraphDatabaseFactory().newEmbeddedDatabase(DBpath);//���ݿⴴ�����ȡ
		registerShutdownHook(database);                                
		engine = new ExecutionEngine(database,StringLogger.logger(new File("log"))); //��ѯ���洴��
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
