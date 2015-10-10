package com.CASTParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;


public class CASTCreater {
	static int i=1;
	
	private String projectPath;
	private ArrayList<String> srcFilePathList;
	private ArrayList<String> binFilePathList;
	private CFileASTRequestor cFileASTRequestor;
	public CASTCreater(String projectPath,CFileASTRequestor cFileASTRequestor) {
		this.projectPath = projectPath;
		this.cFileASTRequestor = cFileASTRequestor;
		this.srcFilePathList = new ArrayList<>();
		this.binFilePathList = new ArrayList<>();
	}
	public void createASTs() {
		//初始化，读取全部.java文件名
		getFilePaths();
		System.out.println("GetFiles is over");
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		
		
		String[] jar = new String[0];
		String[] src = {projectPath};
		parser.setEnvironment(jar, src, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		@SuppressWarnings("unchecked")
		Hashtable<String, String> complierOptions = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, complierOptions);
		parser.setCompilerOptions(complierOptions);
		parser.setUnitName(projectPath);
		
		/*
		 * String[] sourceFilePaths: paths of all *.java files in a version
		 * String[] bindingKeys
		 * String[] encodings
		 * FileASTRequestor requestor
		 * IProgressMonitor monitor
		 */
		String [] sourcePaths = new String[srcFilePathList.size()];
		srcFilePathList.toArray(sourcePaths);
		parser.createASTs(sourcePaths, null, new String[0], cFileASTRequestor, null);
	}
	
	public void getFilePaths() {
		
		File directory = new File(projectPath);
		ArrayList<File> directoryFiles = new ArrayList<>();
		directoryFiles.add(directory);
		while (!directoryFiles.isEmpty()) {
			File direct = directoryFiles.get(0);
			directoryFiles.remove(0);
			File[] files = direct.listFiles();
			if (files==null) {
				return;
			}
			for (File file : files) {
				if (file.isDirectory()) {
					directoryFiles.add(file);
					i++;
				}
				else if(file.getPath().endsWith(".java")){
					System.out.println(file.getPath());
					srcFilePathList.add(file.getAbsolutePath());
				}
				else if(file.getPath().endsWith(".class")){
					System.out.println(file.getPath());
					binFilePathList.add(file.getAbsolutePath());
				}
			}
		}
		System.out.println("Directory number is :"+i);
	}
	
}
