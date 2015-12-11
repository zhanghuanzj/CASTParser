package com.iseu.CASTVistitors;

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
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.parboiled.parserunners.ReportingParseRunner;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.MethodInformation;

@SuppressWarnings("unchecked")
public class CASTVisitorMethodPost extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private CASTHelper castHelper;
	private boolean isMethodInfoChange = false;                 //用于记录是否有函数信息修改
	private HashMap<String, MethodInformation> changeMethods;   //修改函数记录
	private HashMap<String, MethodInformation> javaMethodsInfo; //java源码函数修改信息
	private HashMap<String, Integer> javaMethodsMapTable;       //java源码函数映射表 

	{
		File file = new File("javaMethodsInfo\\javaMethodInfo.obj");
		File file2 = new File("javaMethodsInfo\\javaMethodMapTable.obj");
		FileInputStream fileInputStream;
		FileInputStream fileInputStream2;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream2 = new FileInputStream(file2);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				ObjectInputStream objectInputStream2 = new ObjectInputStream(fileInputStream2);
				try {
					javaMethodsInfo = (HashMap<String, MethodInformation>) objectInputStream.readObject();
					javaMethodsMapTable = (HashMap<String, Integer>) objectInputStream2.readObject();
					objectInputStream.close();
					objectInputStream2.close();
					
					PrintWriter pWriter = new PrintWriter("justTest.txt");
					Set<Map.Entry<String, MethodInformation>> set = javaMethodsInfo.entrySet();
					for (Entry<String, MethodInformation> entry : set) {
						pWriter.println(entry.getKey());
						pWriter.print(entry.getValue());
					}
					pWriter.flush();
					pWriter.close();
					
					System.out.println("The javaMethodsInfo size is :"+javaMethodsInfo.size());
					System.out.println("The javaMethodsMapTable size is :"+javaMethodsMapTable.size());
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
	
	public CASTVisitorMethodPost(HashMap<String, MethodInformation> changeMethods) {
		super();
		this.castHelper = CASTHelper.getInstance();
		this.changeMethods = changeMethods;
	}


	/**
	 * 函数会引起调用对象改变
	 * @param key  : methodKey
	 * @return
	 */
	public boolean methodRegisterOfObj(String key) {
		if (key == null) {       //KEY为空
			return false;
		}
		if (changeMethods.containsKey(key)) {
			MethodInformation methodInformation = changeMethods.get(key);
			if (!methodInformation.isObjChange()) {
				methodInformation.setObjChange(true);
				isMethodInfoChange = true;
				System.out.println("__________________The member variable change!_____________________");
				System.out.println(this.filePath);
				return true;
			}		
		}
		else {
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("__________________The member variable change!_____________________");
			System.out.println(this.filePath);
			return true;
		}
		return false;
	}
	
	/**
	 * 函数会引起参数改变
	 * @param key   : methodKey
	 * @param index : 参数号
	 * @return
	 */
	public boolean methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEY为空
			return false;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			//函数对index号参数没有修改记录，且checkTable没有记录引用重置
			if (!methodInformation.isParameterChange(index)&&methodInformation.isCheckTableOk(index)) {
				methodInformation.parameterChange(index);          //改变第index 参数的修改情况
				isMethodInfoChange = true;
				System.out.println("_______________The parameter change , index is:"+index+"__________");
				System.out.println(this.filePath);	
				return true;
			}         
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.parameterChange(index);              //改变第index 参数的修改情况
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("_______________The parameter change , index is:"+index+"__________");
			System.out.println(this.filePath);
			return true;
		}
		return false;
	}
	



	//如果函数键值为javaMethodTable中的键值，则转换为java函数相关的KEY
	public String switchToJavaMethodKey(String key) {
		String strKey = key.substring(0, key.indexOf('_'));
		if (javaMethodsMapTable.containsKey(strKey)) {
			return javaMethodsMapTable.get(strKey)+key.substring(key.indexOf('_'));
		}
		return null;
	}
	
	/**
	 * 对象调用函数obj.fun()||fun()
	 * 会引起变量改变的函数调用所在函数的处理
	 * @param methodInvoke ： 成员变量或参数变量
	 */
	public void variableChangeHandle(MethodInvocation methodInvoke) {
		//获取函数调用所在的函数key
		String methodKey = castHelper.methodKey(methodInvoke);
		if (methodKey==null) {
			return;
		}
		//获取变量的最左部分
		SimpleName leftNode = (SimpleName) castHelper.getLeftVarName(methodInvoke.getExpression());
		if (leftNode!=null) {
			if (leftNode.resolveBinding() instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding)leftNode.resolveBinding();
				//1.成员变量
				if (variableBinding.isField()) {
					methodRegisterOfObj(methodKey);
				}
				//2.参数变量
				else if (variableBinding.isParameter()) {
					methodRegisterOfParameters(methodKey, variableBinding.getVariableId());
				}
			}
			//3.访问类的变量
			else if (leftNode.resolveTypeBinding()!=null) {     
				ITypeBinding typeBinding = leftNode.resolveTypeBinding();
				if (typeBinding.isClass()) {
					methodRegisterOfObj(methodKey);
				}
			}
		}
		//4.类调用成员函数
		else {
			methodRegisterOfObj(methodKey);
		}
	}

	public void parameterChangeHandle(ASTNode node) {
		//获取函数调用所在的函数key
		String methodKey = castHelper.methodKey(node);
		if (methodKey==null) {
			return;
		}
		//获取变量的最左部分
		SimpleName leftNode = (SimpleName) castHelper.getLeftVarName(node);
		if (leftNode!=null) {
			if (leftNode.resolveBinding() instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding)leftNode.resolveBinding();
				//1.成员变量
				if (variableBinding.isField()) {
					methodRegisterOfObj(methodKey);
				}
				//2.参数变量
				else if (variableBinding.isParameter()) {
					methodRegisterOfParameters(methodKey, variableBinding.getVariableId());
				}
			}
			//3.访问类的变量
			else if (leftNode.resolveTypeBinding()!=null) {     
				ITypeBinding typeBinding = leftNode.resolveTypeBinding();
				if (typeBinding.isClass()) {
					methodRegisterOfObj(methodKey);
				}
			}
		}
	}
	/**
	 * 获取函数的信息
	 * @param key  ： methodKey
	 * @return 函数信息
	 */
	public MethodInformation getMethodInformation(String key) {
		if (key == null) {
			return null;
		}
		//1.属于工程函数  
		else if (changeMethods.containsKey(key)) {               
			return changeMethods.get(key);
		}
		//2.属于java类库函数
		else if (javaMethodsInfo.containsKey(switchToJavaMethodKey(key))) {  
			return javaMethodsInfo.get(switchToJavaMethodKey(key));
		}
		return null;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		//获取调用函数的函数信息
		MethodInformation methodInformation = getMethodInformation(castHelper.getInvokeMethodKey(node));
		if (methodInformation==null) {
			return super.visit(node);
		}
		//1.会改变调用对象的值
		if (methodInformation.isObjChange()) {
			//对象为成员变量
			variableChangeHandle(node);
		}
		//2.会改变参数对象的值
		if (methodInformation.isAnyParaChange()) {
			//获取参数列表
			List<?> paraList = node.arguments();
			for(int i=0;i<paraList.size();i++){
				if (methodInformation.isParameterChange(i)) {
					parameterChangeHandle((ASTNode) paraList.get(i));
				}
			}
		}	
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		//获取调用函数的函数信息
		MethodInformation methodInformation = getMethodInformation(castHelper.getInvokeMethodKey(node));
		if (methodInformation==null) {
			return super.visit(node);
		}
		//会改变调用对象的值isObjChange == true;
		if (methodInformation.isObjChange()) {
			//对象为类,super.fun,直接改变
			methodRegisterOfObj(castHelper.methodKey(node));
		}
		//会改变参数对象的值isParaChange>0;
		if (methodInformation.isAnyParaChange()) {
			//获取参数列表
			List<?> paraList = node.arguments();
			for(int i=0;i<paraList.size();i++){
				if (methodInformation.isParameterChange(i)) {
					parameterChangeHandle((ASTNode) paraList.get(i));
				}
			}
		}
		return super.visit(node);
	}
	
	
	public HashMap<String, MethodInformation> getChangeMethods() {
		return changeMethods;
	}
	
	public void setChangeMethods(HashMap<String, MethodInformation> changeMethods) {
		this.changeMethods = changeMethods;
	}
	
	public boolean isMethodInfoChange() {
		return isMethodInfoChange;
	}

	public void setMethodInfoChange(boolean isMethodInfoChange) {
		this.isMethodInfoChange = isMethodInfoChange;
	}
	
	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		isMethodInfoChange = false;
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}
