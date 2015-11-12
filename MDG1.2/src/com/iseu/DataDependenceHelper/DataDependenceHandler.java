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
			//�����Ѿ�������
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


		//AST���Ĵ�������뵥Ԫ��Ļ�ȡ
		CFileASTRequestor cFileASTRequestor = new CFileASTRequestor();
		CASTCreater castCreater = new CASTCreater(projectPath,cFileASTRequestor);
		castCreater.createASTs();
		ArrayList<CompileUnit> compileUnits = cFileASTRequestor.getCompileUnits();
		DataDependenceHandler dataDependenceHandler = new DataDependenceHandler();
		dataDependenceHandler.communicationParserPre(compileUnits);


	}
	
	/**
	 * �ݹ�ɾ��Ŀ¼��Ŀ¼�µ��ļ�
	 * @param file ���ļ�
	 * @return ɾ���Ƿ�ɹ�
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
	 * �߳�ͨ����������--������Ϣ��ȡ(�������Ƿ��޸ı���)
	 * @param compileUnits �����뵥Ԫ�б�
	 */
	public void communicationParserPre(ArrayList<CompileUnit> compileUnits) {
		// 1.�Ƚ���ֵ��䣬ǰ׺���ʽ����׺���ʽ����������仯�ĺ�����¼����
		CASTVisitorSrcMethodsPrepare castVisitorPrepare = new CASTVisitorSrcMethodsPrepare();
		castVisitorPrepare.traverse(compileUnits);
		//�����޸ı�������Ϣ
		HashMap<String, MethodInformation> changeMethods = castVisitorPrepare.getChangeMethods();
		// 2.������������������ı�ĺ�����ӽ���¼��		
		int times = 1;     //��������
		CASTVisitorSrcMethodsCheck castVisitorCheck = new CASTVisitorSrcMethodsCheck(changeMethods);
		do {
			castVisitorCheck.traverse(compileUnits);    //��������Ϣ
			System.out.println("The "+(times++)+"  time");
		} while (castVisitorCheck.isMethodInfoChange());		
		System.out.println("Method Change handle is finished total number is :"+changeMethods.size());
		
	    //�����Щ�Ե��ö���&&����û���޸ĵĺ���
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
		
		//������Ϣ�����뱣��
		Set<Map.Entry<String, MethodInformation>> methodInformationsNew =  changeMethods.entrySet();
		HashMap<String,Integer> methodMapTable = new HashMap<>();             //ӳ���<���������>
		HashMap<String, MethodInformation> javaMethodInfo = new HashMap<>();  //�������ñ���滻�ĺ�����Ϣ��
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
				
				//��������Ϣд���ļ���������������ӳ���
				for (Entry<String, MethodInformation> entry : methodInformationsNew) {
					//�ҵ�����
					String mapKey = entry.getKey().substring(0, entry.getKey().indexOf('_'));
					if (!methodMapTable.containsKey(mapKey)) {
						methodMapTable.put(mapKey, keyNumber++);
					}
					methodsInfoOutToFile.println(entry.getKey());
					methodsInfoOutToFile.print(entry.getValue());
					//��ֵΪ<�����_����_�������������б�>
					javaMethodInfo.put(methodMapTable.get(mapKey)+entry.getKey().substring(entry.getKey().indexOf('_')), entry.getValue());
				}
				methodsInfoOutToFile.flush();
				methodsInfoOutToFile.close();
				
				//��ӳ���д���ļ�
				Set<Map.Entry<String, Integer>> mapTableSet = methodMapTable.entrySet();
				for (Entry<String, Integer> entry : mapTableSet) {
					methodsMapTableOutToFile.println(entry.getKey());
					methodsMapTableOutToFile.println(entry.getValue());
				}
				methodsMapTableOutToFile.flush();
				methodsMapTableOutToFile.close();
				System.out.println("The total mapTable size is: "+mapTableSet.size());
				
				//��ӳ����Զ������ʽд���ļ�srcMethodMapTable.obj
				try {
					ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
					objectOut.writeObject(methodMapTable);
					objectOut.flush();
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//��java������Ϣ�Զ������ʽд���ļ�srcMethodInfo.obj
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
