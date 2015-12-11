package com.iseu.DataDependenceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import com.iseu.Information.MethodInformation;

@SuppressWarnings("unchecked")
public class DataDependenceHelper {
	private HashMap<String, MethodInformation> sourceMethodsInfo;   //工程函数信息	
	private HashMap<String, MethodInformation> javaMethodsInfo; 	//java源码函数信息
	private HashMap<String, Integer> sourceMethodsMapTable;     	//工程包名映射表
	private HashMap<String, Integer> javaMethodsMapTable;       	//java包名映射表
	
	private final static DataDependenceHelper DDHelper = new DataDependenceHelper();
	private DataDependenceHelper(){
		File file = new File("javaMethodsInfo\\javaMethodInfo.obj");
		File file2 = new File("javaMethodsInfo\\javaMethodMapTable.obj");
		File file3 = new File("srcMethodsInfo\\srcMethodInfo.obj");
		File file4 = new File("srcMethodsInfo\\srcMethodMapTable.obj");
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
					
					PrintWriter pWriter = new PrintWriter("srcMethodsInfo\\justTest.txt");
					Set<Map.Entry<String, Integer>> set = sourceMethodsMapTable.entrySet();
					for (Entry<String, Integer> entry : set) {
						pWriter.println(entry.getKey());
						pWriter.print(entry.getValue());
					}
					pWriter.flush();
					pWriter.close();
					
					//System.out.println("The javaMethodsInfo size is :"+sourceMethodsInfo.size());
					//System.out.println("The javaMethodsMapTable size is :"+sourceMethodsMapTable.size());
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
	
	public static DataDependenceHelper getInstance(){
		return DDHelper;
	}
	public boolean isVarChange(ASTNode node,ASTNode methodInvoke) {
		int index = getIndexInMethodInvoke(node, methodInvoke);
		MethodInformation methodInformation = getMethodInformation(getInvokeMethodKey(methodInvoke));
		
		if (index == -1||methodInformation==null) {
			return false;
		}
		else if (index == -2) {
			return methodInformation.isObjChange();
		}
		else if (index >=0) {
			return methodInformation.isParameterChange(index);
		}
		return false;
	}
	/**
	 * 将函数的KEY转换为javaMethodsInfo的KEY
	 * @param key
	 * @return
	 */
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
	
	/**
	 * 将函数的KEY转换为sourceMethodsInfo的KEY
	 * @param key
	 * @return
	 */
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
	/**
	 * 通过函数的KEY经过转换得到相应的MethodInformation
	 * @param key
	 * @return methodInfo
	 */
	public MethodInformation getMethodInformation(String key) {
		MethodInformation methodInformation = null;
		if (switchToJavaMethodKey(key)!=null) {
			//java源码函数
			methodInformation = javaMethodsInfo.get(switchToJavaMethodKey(key));
		}
		else if (switchToSrcMethodKey(key)!=null) {
			//工程函数
			methodInformation = sourceMethodsInfo.get(switchToSrcMethodKey(key));
		}
		return methodInformation;
	}
	/**
	 * 获取调用函数的key
	 * @param astNode  MethodInvocaton或者SuperMethodInvocation
	 * @return key (包路径+类名+函数名+参数类型列表)
	 */
	public String getInvokeMethodKey(ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//包路径+类名
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//函数名+参数类型列表
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
				//包路径+类名
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//函数名+参数类型列表
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
	
	/**
	 * 得到全名的最左名
	 * @param astNode 全名节点
	 * @return  全名的最左变量名
	 */
	public ASTNode getLeftVarName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			return astNode;
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//找到最外面的那个对象xx.x.i中的xx
			if(!(qualifiedName.getQualifier() instanceof SimpleName)){
				return getLeftVarName(qualifiedName.getQualifier());
			}
			return qualifiedName.getQualifier();
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b找到最外面的那个对象
			if (!(fieldAccess.getExpression() instanceof ThisExpression)) {
				return getLeftVarName(fieldAccess.getExpression());
			}
			return fieldAccess.getName();
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return superFieldAccess.getName();
		}
		//5.ArrayAccess buffer[tail++]  取buffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getLeftVarName(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getLeftVarName(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return getLeftVarName(methodInvocation.getExpression());
		}
		return null;
	}
	
	/**
	 * 返回simpleName所在函数调用的位置(-1出错，-2为对象，0~n为index)
	 * simpleName:要查询的变量名节点
	 * methodInvoke:变量所在的函数调用节点
	 */
	public int getIndexInMethodInvoke(ASTNode node,ASTNode methodInvoke) {
		SimpleName simpleName = (SimpleName)getLeftVarName(node);
		if (simpleName == null) {
			return -1;
		}
		if (methodInvoke instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation)methodInvoke;
			ASTNode astNode = methodInvocation.getExpression();
			if (astNode!=null) {
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				//若为调用函数的对象则返回-2
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
					//System.out.println("对象调用函数");
					return -2;
				}
			}
			List<?> argumetns = methodInvocation.arguments();
			int index = 0;
			//逐一考察参数列表中的参数
			for (Object object : argumetns) {
				astNode = (ASTNode)object;
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
					//System.out.println("INDEX: "+index);
					return index;
				}
				index++;
			}
		}
		else if (methodInvoke instanceof SuperMethodInvocation) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)methodInvoke;
			List<?> arguments = superMethodInvocation.arguments();
			int index = 0;
			ASTNode astNode;
			for (Object object : arguments) {
				astNode = (ASTNode)object;
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
					//System.out.println("INDEX: "+index);
					return index;
				}
				index++;
			}
		}
		return -1;
	}
}


