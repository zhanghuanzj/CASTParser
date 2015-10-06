package com.CASTVistitors;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.osgi.framework.wiring.dto.BundleWiringDTO.NodeDTO;

import com.CASTParser.CompileUnit;
import com.Information.MethodInformation;
import com.MDGHandle.Nodes.NotifyType;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadWaitNode;
import com.MDGHandle.Nodes.WaitType;

public class CASTVisitorCheck extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;

	private HashMap<String, MethodInformation> changeMethods;

	public CASTVisitorCheck(HashMap<String, MethodInformation> changeMethods) {
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
	public String methodKey(ExpressionStatement node) {	
		//获取所在         < 函数名+行号>   （为了区分重载情况）
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer) {   //属于初始化块则直接返回
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//如果是构造函数则不算在内
		if (methodDeclaration.resolveBinding().isConstructor()) {
			return null;
		}
		String methodName = methodDeclaration.getName().toString();                                   //函数名
		int decLine = compilationUnit.getLineNumber(methodDeclaration.getName().getStartPosition());  //声明行号
		
		//获取所在的   <包+类>  （考虑匿名类情况）
		String className ="";
		ASTNode classNode = methodDeclaration.getParent();
		if (classNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
			if (typeDeclaration.resolveBinding()!=null) {
				className = typeDeclaration.resolveBinding().getQualifiedName();
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
		return className+"_"+methodName+decLine;
	}	
	//函数会引起调用对象改变
	public void methodRegisterOfObj(String key,boolean isChange) {
		if (key == null) {       //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {
			MethodInformation methodInformation = changeMethods.get(key);
			methodInformation.setObjChange(isChange||methodInformation.isObjChange());
		}
		else {
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			changeMethods.put(key, methodInformation);
		}
	}
	//函数会引起参数改变
	public void methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			int parameters = methodInformation.getIsParaChange();
			int checkTable = methodInformation.getCheckTable();
			parameters = parameters|(1<<index)&checkTable;          //改变第index 参数的修改情况
			methodInformation.setIsParaChange(parameters);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			int parameters = methodInformation.getIsParaChange();
			methodInformation.setIsParaChange(parameters|(1<<index));
			changeMethods.put(key, methodInformation);
		}
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
	
	public String getMethodKey(MethodInvocation node) {
		if (node.resolveMethodBinding()!=null) {
			String methodName = node.resolveMethodBinding().getMethodDeclaration().getName();
			String className = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
			if (className.equals("")) {
				className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
			}
			ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
			for (ITypeBinding iTypeBinding : typeBinding) {
				methodName = methodName+"_"+iTypeBinding.getName();
			}
			return className+"_"+methodName;
		}
		return null;
	}
	
	public int indexOfVarChange(ASTNode decNode) {
		if (decNode instanceof VariableDeclarationFragment) {
			if (decNode.getParent() instanceof FieldDeclaration) {      //成员变量
				return -2;       //-2表示成员变量进行了更改
			}
		}
		else if (decNode instanceof SingleVariableDeclaration) {        //参数变量
			return getParaIndex((SingleVariableDeclaration)decNode);    //第index个参数被修改了
		}
		else if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
			if (decNode.getParent() instanceof FieldDeclaration) {
				return -2;       //-2表示成员变量进行了更改
			}
		}
		return -1;
	}
	//-2表示成员变量进行了更改，-1表示没有更改，其余表示参数index更改
	public int memberVar(Expression expression){
		if (expression==null) {
			return -2;               //-2表示成员变量进行了更改
		}
		else if(expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)expression;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		else if(expression instanceof QualifiedName) {                      //obj.a.fun();
			QualifiedName qualifiedName = (QualifiedName) expression;
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		else if (expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	@Override
	public boolean visit(MethodInvocation node) {
		String key = getMethodKey(node);
		int isParaChange = 0;
		boolean isObjChange = false;
//		System.out.println("CHECK");
//		System.out.println(key);
		//key 获取失败
		if (key==null) {
			return super.visit(node);
		}
		//所调用函数会改变值
		if (changeMethods.containsKey(key)) {           
			MethodInformation methodInformation = changeMethods.get(key);
			//会改变调用对象的值
			if (methodInformation.isObjChange()) {
				//对象为成员变量
				int result = memberVar(node.getExpression());
				System.out.println(filePath);
				System.out.println(node);
				System.out.println(result);
			}
//			a.visit();
//			System.out.println(key);
//			visit();
//			this.changeMethods.get(key);
		}
		
		return super.visit(node);
	}

	


	



	
	public HashMap<String, MethodInformation> getChangeMethods() {
		return changeMethods;
	}
	public void setChangeMethods(HashMap<String, MethodInformation> changeMethods) {
		this.changeMethods = changeMethods;
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


