package com.iseu.DataDependenceHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.SynchronizedStatement;

import com.iseu.DataDependenceHelper.CASTCreater;
import com.iseu.DataDependenceHelper.CASTVisitorSrcMethodsCheck;
import com.iseu.DataDependenceHelper.CASTVisitorSrcMethodsPrepare;
import com.iseu.DataDependenceHelper.CFileASTRequestor;
import com.iseu.DataDependenceHelper.CompileUnit;
import com.iseu.Information.MethodInformation;

import scala.annotation.elidable;

public class DataDependenceHandler {
	public static void main(String[] args) {
		String projectPath = "H:\\Projects\\TestCase\\src\\com\\TestCase02";
		
		
		String analyseProjectName = projectPath.substring(projectPath.lastIndexOf('\\')+1);
		File file = new File("PROJECTMETHODS");
		
		System.out.println("ProjectName:"+projectPath.substring(projectPath.lastIndexOf('\\')+1));
		if (file.exists()) {
			String recordProjectName = null;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				try {
					recordProjectName = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			//工程已经分析过
			if (analyseProjectName.equals(recordProjectName)) {
				return;
			}
			else
			{
				file.delete();
			}
		}

		try {
			System.out.println("CREATENEWFILE");
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(file);
			printWriter.write(analyseProjectName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally {
			printWriter.close();
		}


		//AST树的创建与编译单元组的获取
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		DataDependenceHandler dataDependenceHandler = new DataDependenceHandler();
		dataDependenceHandler.communicationParserPre(compileUnits);


	}
	
	/**
	 * 递归删除目录及目录下的文件
	 * @param file ：文件
	 * @return 删除是否成功
	 */
	public boolean deleteDirFiles(File file) {
		if (file.isDirectory()) {
			String[] fileNames = file.list();
			for(int i=0;i<fileNames.length;++i){
				if (!deleteDirFiles(new File(file, fileNames[i]))) {
					return false;
				}
			}
		}
		return file.delete();
	}
	/**
	 * 线程通信依赖解析--函数信息获取(即函数是否修改变量)
	 * @param compileUnits ：编译单元列表
	 */
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.先将赋值语句，前缀表达式，后缀表达式会引起变量变化的函数记录下来
		CASTVisitorSrcMethodsPrepare castVisitorPrepare = new CASTVisitorSrcMethodsPrepare();
		castVisitorPrepare.traverse(compileUnits);
		//函数修改变量的信息
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.将函数调用引起变量改变的函数添加进记录表		
		int times = 1;     //迭代次数
		CASTVisitorSrcMethodsCheck castVisitorCheck = new CASTVisitorSrcMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //处理函数信息
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //清除那些对调用对象&&参数没有修改的函数
		ArrayList<String> keys = new ArrayList<>();
		Set<Map.Entry<String, MethodInformation>> methodInformations = changeMethods.entrySet();
		for (Entry<String, MethodInformation> entry : methodInformations) {
			if (!entry.getValue().isObjChange()&&!entry.getValue().isAnyParaChange()) {
				keys.add(entry.getKey());
			}
		}
		for (String  key : keys) {
			changeMethods.remove(key);
		}
		
		//函数信息处理与保存
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //映射表<包名，编号>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //将包名用编号替换的函数信息表
		int keyNumber = 0;
		try {
			File dir = new File("srcMethodsInfo\\");
			if (dir.exists()) {
				if (!deleteDirFiles(dir)) {
					System.err.println("File delete error!");
				}
				else{
					System.out.println("File delete sucess!");
				}
			}
			if (dir.mkdirs()) {
				System.out.println("MADE!");
				PrintWriter methodsInfoOutToFile = new PrintWriter("srcMethodsInfo\\Methods.txt");
				PrintWriter methodsMapTableOutToFile = new PrintWriter("srcMethodsInfo\\methodMapTable.txt");
				File file1 = new File("srcMethodsInfo\\srcMethodMapTable.obj");
				File file2 = new File("srcMethodsInfo\\srcMethodInfo.obj");
				FileOutputStream fileOut = new FileOutputStream(file1);
				FileOutputStream fileOut2 = new FileOutputStream(file2);
				
				//将函数信息写入文件————并建立映射表
				for (Entry<String, MethodInformation> entry : methodInformationsNew) {
					//找到包名
					String mapKey = entry.getKey().substring(0, entry.getKey().indexOf('_'));
					if (!methodMapTable.containsKey(mapKey)) {
						methodMapTable.put(mapKey, keyNumber++);
					}
					methodsInfoOutToFile.println(entry.getKey());
					methodsInfoOutToFile.print(entry.getValue());
					//键值为<包编号_类名_函数名及参数列表>
					javaMethodInfo.put(methodMapTable.get(mapKey)+entry.getKey().substring(entry.getKey().indexOf('_')), entry.getValue());
				}
				methodsInfoOutToFile.flush();
				methodsInfoOutToFile.close();
				
				//将映射表写入文件
				Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
				for (Entry<String, Integer> entry : mapTableSet) {
					methodsMapTableOutToFile.println(entry.getKey());
					methodsMapTableOutToFile.println(entry.getValue());
				}
				methodsMapTableOutToFile.flush();
				methodsMapTableOutToFile.close();
				System.out.println("The total mapTable size is: "+mapTableSet.size());
				
				//将映射表以对象的形式写入文件srcMethodMapTable.obj
				try {
					ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
					objectOut.writeObject(methodMapTable);
					objectOut.flush();
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//将java函数信息以对象的形式写入文件srcMethodInfo.obj
				try {
					ObjectOutputStream objectOut2 = new ObjectOutputStream(fileOut2);
					objectOut2.writeObject(javaMethodInfo);
					objectOut2.flush();
					objectOut2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("After reduce the methods size is :"+changeMethods.size());
		System.out.println("The total times is :"+times);
	}
}
