package com.CASTVistitors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.plaf.synth.SynthSpinnerUI;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.CASTHelper.CASTHelper;
import com.CASTParser.CompileUnit;
import com.Information.MethodInformation;

public class CASTVisitorCommunication extends ASTVisitor{
	private CompilationUnit compilationUnit ;
	private String filePath;

	private HashMap<String, MethodInformation> sourceMethodsInfo;   //���̺�����Ϣ	
	private HashMap<String, MethodInformation> javaMethodsInfo; //javaԴ�뺯����Ϣ
	private HashMap<String, Integer> sourceMethodsMapTable;     //���̰���ӳ���
	private HashMap<String, Integer> javaMethodsMapTable;       //java����ӳ���

	{
		File file = new File("javaMethodsInfo\\javaMethodInfo.obj");
		File file2 = new File("javaMethodsInfo\\javaMethodMapTable.obj");
		File file3 = new File("srcMethodInfo.obj");
		File file4 = new File("srcMethodMapTable.obj");
		FileInputStream fileInputStream;
		FileInputStream fileInputStream2;
		FileInputStream fileInputStream3;
		FileInputStream fileInputStream4;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream2 = new FileInputStream(file2);
			fileInputStream3 = new FileInputStream(file3);
			fileInputStream4 = new FileInputStream(file4);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				ObjectInputStream objectInputStream2 = new ObjectInputStream(fileInputStream2);
				ObjectInputStream objectInputStream3 = new ObjectInputStream(fileInputStream3);
				ObjectInputStream objectInputStream4 = new ObjectInputStream(fileInputStream4);
				try {
					javaMethodsInfo = (HashMap<String, MethodInformation>) objectInputStream.readObject();
					javaMethodsMapTable = (HashMap<String, Integer>) objectInputStream2.readObject();
					sourceMethodsInfo = (HashMap<String, MethodInformation>) objectInputStream3.readObject();
					sourceMethodsMapTable = (HashMap<String, Integer>) objectInputStream4.readObject();
					objectInputStream.close();
					objectInputStream2.close();
					objectInputStream3.close();
					objectInputStream4.close();
					fileInputStream.close();
					fileInputStream2.close();
					fileInputStream3.close();
					fileInputStream4.close();
					
					PrintWriter pWriter = new PrintWriter("justTest.txt");
					Set<Map.Entry<String, MethodInformation>> set = sourceMethodsInfo.entrySet();
					for (Entry<String, MethodInformation> entry : set) {
						pWriter.println(entry.getKey());
						pWriter.print(entry.getValue());
					}
					pWriter.flush();
					pWriter.close();
					
					System.out.println("The javaMethodsInfo size is :"+sourceMethodsInfo.size());
					System.out.println("The javaMethodsMapTable size is :"+sourceMethodsMapTable.size());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	//���ز������ں����Ĳ����б��е�index ��0��ʼ
	public int getParaIndex(SingleVariableDeclaration node) {
		if (node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) node.getParent();
			List<?> parameters = methodDeclaration.parameters();
			int position = 0;
			for(int i=0;i<parameters.size();i++){
				if (parameters.get(i) instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)parameters.get(i);
					if(singleVariableDeclaration.getName().toString().equals(node.getName().toString())){
						position = i;
						break;
					}
				}	
			}
			return position;
		}
		return -1;
	}
	// ��ȡ�ڵ����ں�����KEY
	public String methodKey(ASTNode node) {	
		//��ȡ����         < ������+�����б�>   ��Ϊ���������������
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==compilationUnit) {   //���ڳ�ʼ������ֱ�ӷ���
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//����ǹ��캯����������
		if (methodDeclaration.isConstructor()) {
			return null;
		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());   //������                             
		List<?> parameters  = methodDeclaration.parameters();  //�����к�
		for (Object object : parameters) {
			if (object instanceof SingleVariableDeclaration	) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)object;
				methodName.append("_"+singleVariableDeclaration.getType().toString().charAt(0));
			}
		}
		//��ȡ���ڵ�   <��+��>  �����������������
		String className ="";
		ASTNode classNode = methodDeclaration.getParent();
		if (classNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
			if (typeDeclaration.resolveBinding()!=null) {
				className = typeDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}		
		}
		else if (classNode instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) classNode;
			if (anonymousClassDeclaration.resolveBinding()!=null) {
				className = anonymousClassDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
		if (className==null||className.equals("")) {
			return null;
		}
		int dotPosition = className.lastIndexOf('.');
		if (dotPosition==-1) {
			return null;
		}
		return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
	}

	//��ȡ���ú�����key
	public String getMethodKey(ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_"+iTypeBinding.getName().toString().charAt(0));
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		else if (astNode instanceof SuperMethodInvocation) {
			SuperMethodInvocation node = (SuperMethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_"+iTypeBinding.getName().toString().charAt(0));
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		return null;
	}

	//�õ����������ľ���λ�ã��ǳ�Ա����-2�����Ǻ����еĲ�������index
	public int indexOfVarChange(ASTNode decNode) {
		if (decNode instanceof VariableDeclarationFragment) {
			if (decNode.getParent() instanceof FieldDeclaration) {      //��Ա����
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //��������
			return getParaIndex((SingleVariableDeclaration)decNode);    //��index���������޸���
		}
		else if (decNode instanceof VariableDeclarationFragment) {   //this.��Ա����
			if (decNode.getParent() instanceof FieldDeclaration) {
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		return -1;
	}
	
	//��������KEYת��ΪjavaMethodsInfo��KEY
	public String switchToJavaMethodKey(String key) {
		if (key==null) {
			return null;
		}
		String strKey = key.substring(0, key.indexOf('_'));
		if (javaMethodsMapTable.containsKey(strKey)) {
			return javaMethodsMapTable.get(strKey)+key.substring(key.indexOf('_'));
		}
		return null;
	}
	//��������KEYת��ΪsourceMethodsInfo��KEY
	public String switchToSrcMethodKey(String key) {
		if (key==null) {
			return null;
		}
		String strKey = key.substring(0, key.indexOf('_'));
		if (sourceMethodsMapTable.containsKey(strKey)) {
			return sourceMethodsMapTable.get(strKey)+key.substring(key.indexOf('_'));
		}
		return null;
	}
	//ͨ��������KEY����ת���õ���Ӧ��MethodInformation
	public MethodInformation getMethodInformation(String key) {
		MethodInformation methodInformation = null;
		if (switchToJavaMethodKey(key)!=null) {
			//javaԴ�뺯��
			methodInformation = javaMethodsInfo.get(switchToJavaMethodKey(key));
		}
		else if (switchToSrcMethodKey(key)!=null) {
			//���̺���
			methodInformation = sourceMethodsInfo.get(switchToSrcMethodKey(key));
		}
		return methodInformation;
	}
//	@Override
//	public boolean visit(MethodInvocation node) {
//		String key = getMethodKey(node);
//		
//		MethodInformation methodInformation = getMethodInformation(key);
//		if (methodInformation!=null) {
//			System.out.println("KEY:"+key);
//			System.out.println("METHODINFO:"+methodInformation);
//		}
//		
//		return super.visit(node);
//	}
	
	@Override
	public boolean visit(PrefixExpression node) {
		CASTHelper castHelper = CASTHelper.getInstance();
		System.out.println(filePath);
		System.err.println(compilationUnit.getLineNumber(node.getStartPosition()));
		System.out.println(node.getOperand());
		System.out.println("Is in method: "+castHelper.isDeclaredInCurrentMethod(node.getOperand()));
		return super.visit(node);
	}


	
	
	
	
	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}
