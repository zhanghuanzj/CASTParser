package com.iseu.CASTVistitors;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.osgi.framework.wiring.dto.BundleWiringDTO.NodeDTO;

import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.MethodInformation;
import com.iseu.MDGHandle.Nodes.NotifyType;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;
import com.iseu.MDGHandle.Nodes.WaitType;

public class CASTVisitorJavaMethodsCheck extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private boolean isMethodInfoChange = false;                 //用于记录是否有函数信息修改
	private HashMap<String, MethodInformation> changeMethods;   //修改函数记录
	private HashMap<String, MethodInformation> javaMethodsInfo; //java源码函数修改信息
	private HashMap<String, Integer> javaMethodsMapTable;       //java源码函数映射表 


	public CASTVisitorJavaMethodsCheck(HashMap<String, MethodInformation> changeMethods) {
		super();
		this.changeMethods = changeMethods;
	}

	//返回参数所在函数的参数列表中的index 从0开始
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
	// KEY 的确定
	public String methodKey(ASTNode node) {	
		//获取所在         < 函数名+参数列表>   （为了区分重载情况）
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==compilationUnit) {   //属于初始化块则直接返回
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//如果是构造函数则不算在内
		if (methodDeclaration.resolveBinding().isConstructor()) {
			return null;
		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());   //函数名                             
		List<?> parameters  = methodDeclaration.parameters();  //声明行号
		for (Object object : parameters) {
			if (object instanceof SingleVariableDeclaration	) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)object;
				methodName.append("_"+singleVariableDeclaration.getType().toString().charAt(0));
			}
		}
		//获取所在的   <包+类>  （考虑匿名类情况）
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
	//函数会引起调用对象改变
	public boolean methodRegisterOfObj(String key,boolean isChange) {
		if (key == null) {       //KEY为空
			return false;
		}
		if (changeMethods.containsKey(key)) {
			MethodInformation methodInformation = changeMethods.get(key);
			if (!methodInformation.isObjChange()) {
				methodInformation.setObjChange(true);
				isMethodInfoChange = true;
				System.out.println("__________________The member variable change!_____________________");
				return true;
			}
			
		}
		else {
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("__________________The member variable change!_____________________");
			return true;
		}
		return false;
	}
	//函数会引起参数改变
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
				return true;
			}         
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.parameterChange(index);              //改变第index 参数的修改情况
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("_______________The parameter change , index is:"+index+"__________");
			return true;
		}
		return false;
	}
	//引用重置情况处理
	public void referenceReset(String key,int checkTableIndex) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			int checkTable = methodInformation.getCheckTable();
			checkTable = checkTable&(~(1<<checkTableIndex));          //改变第index 参数的修改情况
			methodInformation.setCheckTable(checkTable);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			int checkTable = methodInformation.getCheckTable();
			methodInformation.setCheckTable(checkTable&(~(1<<checkTableIndex)));
			changeMethods.put(key, methodInformation);
		}	
	}
	//获取调用函数的key
	public String getMethodKey(ASTNode astNode) {
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
	
	//得到声明变量的具体位置，是成员变量-2，还是函数中的参数变量index
	public int indexOfVarChange(ASTNode decNode) {
		if (decNode instanceof VariableDeclarationFragment) {
			if (decNode.getParent() instanceof FieldDeclaration) {      //成员变量
				return -2;       //-2表示成员变量进行了更改
			}
		}
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量
			return getParaIndex((SingleVariableDeclaration)decNode);    //第index个参数被修改了
		}
		else if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
			if (decNode.getParent() instanceof FieldDeclaration) {
				return -2;       //-2表示成员变量进行了更改
			}
		}
		return -1;
	}
	//-2表示成员变量进行了更改，-1表示没有更改，其余表示index号参数更改（此函数用于处理调用函数的对象）
	public int memberVar(Expression expression){
		// 1. 函数直接调用function（...）
		if (expression==null) {
			return -2;               //-2表示成员变量进行了更改
		}
		// 2. 对象调用函数obj.function(...)
		else if(expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)expression;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 3. 对象成员调用函数obj.mem.function(...)
		else if(expression instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) expression;
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 4. this引用指引调用this.obj.function(...)
		else if (expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	//-2表示成员变量进行了更改，-1表示没有更改，其余表示index号参数更改（此函数用于处理调用函数中的参数）
	public int parameterVar(Object object) {
		// 1. 对象调用函数obj.function(...)
		if(object instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)object;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 2. 对象成员调用函数obj.mem.function(...)
		else if(object instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) object;
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 3. this引用指引调用this.obj.function(...)
		else if (object instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) object;
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	/*
	 * 改变变量的处理
	 * index 表示调用函数所在的函数的变量改变的索引，-1表示没有改变，-2表示成员变量改变，其余表示改变参数的索引（0，1...）
	 * node 表示调用函数的ASTnode，用于获取所在函数的KEY
	 */
	public void changeHandle(int index,ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			String key = methodKey(node);
			boolean isChange = false;
			if (index==-1||key==null) {
				return ;
			}
			else if (index==-2) {
				isChange = methodRegisterOfObj(key, true);	
			}
			else if (index>=0&&index<=31) {
				isChange = methodRegisterOfParameters(key, index);
			}
			else {
				return;
			}
			if (isChange) {
				System.out.println(node);
			}
		}
		else if (astNode instanceof SuperMethodInvocation) {
			SuperMethodInvocation node = (SuperMethodInvocation)astNode;
			String key = methodKey(node);
			boolean isChange = false;
			if (index==-1||key==null) {
//				System.out.println("Didn't change!");
				return ;
			}
			else if (index==-2) {
				isChange = methodRegisterOfObj(key, true);	
			}
			else if (index>=0&&index<=31) {
				isChange = methodRegisterOfParameters(key, index);
			}
			else {
				return;
			}
			if (isChange) {
				System.out.println(node);
			}
		}		
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String key = getMethodKey(node);
		//key 获取失败
		if (key==null) {
			return super.visit(node);
		}
		//所调用函数会改变值
		MethodInformation methodInformation;
		if (changeMethods.containsKey(key)) {             //属于工程函数    
			methodInformation = changeMethods.get(key);
			//会改变调用对象的值
			if (methodInformation.isObjChange()) {
				//对象为成员变量
				int result = memberVar(node.getExpression());
				changeHandle(result, node);
			}
			//会改变参数对象的值
			if (methodInformation.getIsParaChange()>0) {
				int parameters = methodInformation.getIsParaChange();
				//获取参数列表
				List<?> paraList = node.arguments();
				int i = 0;
				while(parameters>0){
					if ((parameters&1)==1) {   //index为i的参数发生了改变
						int x = parameterVar(paraList.get(i));
						changeHandle(x, node);
					}
					i++;
					parameters = parameters>>1;
				}
			}	
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		String key = getMethodKey(node);
		//key 获取失败
		if (key==null) {
			return super.visit(node);
		}
		//所调用函数会改变值
		MethodInformation methodInformation;
		if (changeMethods.containsKey(key)) {             //属于工程函数    
			methodInformation = changeMethods.get(key);
			//会改变调用对象的值isObjChange == true;
			if (methodInformation.isObjChange()) {
				//对象为成员变量,super.fun
				changeHandle(-2, node);
			}
			//会改变参数对象的值isParaChange>0;
			if (methodInformation.getIsParaChange()>0) {
				int parameters = methodInformation.getIsParaChange();
				//获取参数列表
				List<?> paraList = node.arguments();
				int i = 0;
				while(parameters>0){
					if ((parameters&1)==1) {   //index为i的参数发生了改变
						int x = parameterVar(paraList.get(i));
						changeHandle(x, node);
					}
					i++;
					parameters = parameters>>1;
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


