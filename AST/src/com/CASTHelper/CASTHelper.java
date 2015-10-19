package com.CASTHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.Information.DeclarePosition;

public class CASTHelper {
	HashSet<String> varType = new HashSet<>();
	double []a;
	private final static CASTHelper CAST_HELPER = new CASTHelper();
	private CASTHelper() {	
		varType.add("String");
		varType.add("Short");
		varType.add("Long");
		varType.add("Integer");
		varType.add("Float");
		varType.add("Double");
		varType.add("Character");
		varType.add("Byte");
		varType.add("Boolean");
	}
	public static CASTHelper getInstance() {
		return CAST_HELPER;
	}
	/**变量声明位置的处理,用来得到声明变量具体声明的位置
	 * 
	 * @param decNode：变量声明节点
	 * @return 变量定义的位置
	 */
	public DeclarePosition varDeclaredPositionHandle(ASTNode decNode) {
		//1.成员变量
		if (decNode instanceof VariableDeclarationFragment) {   
			//(1).普通成员变量
			if (decNode.getParent() instanceof FieldDeclaration) { 
				boolean isFinal = false;
				FieldDeclaration fieldDeclaration = (FieldDeclaration)decNode.getParent();
				List<?> list = fieldDeclaration.modifiers();
				for (Object object : list) {
					if (object instanceof Modifier&&((Modifier)object).isFinal()) {
						isFinal = true;
					}
				}
				//不可改类型或原始变量为final
				if (varType.contains(fieldDeclaration.getType().resolveBinding().getName())||
					(fieldDeclaration.getType().isPrimitiveType()&&isFinal)	) {
					return DeclarePosition.INMETHOD;
				}
				if (fieldDeclaration.getType().isPrimitiveType()) {
					return DeclarePosition.INMEMBERPRIMITIVE;
				}
				return DeclarePosition.INMEMBER;
			}
			//(2)在main函数内部
			else if (methodKey(decNode)!=null&&methodKey(decNode).endsWith("main_S")) {
				if (decNode.getParent() instanceof VariableDeclarationStatement) {  // int a = 3;
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)decNode.getParent();
					if (variableDeclarationStatement.getType().isPrimitiveType()||
						varType.contains(variableDeclarationStatement.getType().resolveBinding().getName())) {
						return DeclarePosition.INMETHOD;
					}
				}
				else if(decNode.getParent() instanceof VariableDeclarationExpression){//for(int a=3;...)
					VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)decNode.getParent();
					if (variableDeclarationExpression.getType().isPrimitiveType()||
						varType.contains(variableDeclarationExpression.getType().resolveBinding().getName())) {
						return DeclarePosition.INMETHOD;
					}
				}
				return DeclarePosition.INMEMBER;
			}
		}
		//2.参数变量
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {  
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)decNode;
			if (singleVariableDeclaration.getType().isPrimitiveType()||
				varType.contains(singleVariableDeclaration.getType().resolveBinding().getName())) {
				return DeclarePosition.INMETHOD;
			}
			return DeclarePosition.INPARAMETER;
		}
		//不是成员变量和参数变量，或者java源码中的变量（null找不到）则归为INMETHOD，不做处理
		return DeclarePosition.INMETHOD;
	}
	/**查看ASTNode中变量的声明位置
	 * 
	 * @param astNode 变量名
	 * @return 声明位置
	 */
	public DeclarePosition varDeclaredPosition(ASTNode astNode) {
		CompilationUnit compilationUnit = (CompilationUnit) astNode.getRoot();
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			SimpleName simpleName = (SimpleName) astNode;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			//声明语句中的变量，不做处理
			if (astNode.getParent() == decNode) {
				return DeclarePosition.INMETHOD;
			}
			return varDeclaredPositionHandle(decNode);
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//找到最外面的那个对象xx.x.i中的xx
			if(!(qualifiedName.getQualifier() instanceof SimpleName)){
				return varDeclaredPosition(qualifiedName.getQualifier());
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return varDeclaredPositionHandle(decNode);
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b找到最外面的那个对象
			if (!(fieldAccess.getExpression() instanceof SimpleName)) {
				return varDeclaredPosition(fieldAccess.getExpression());
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return varDeclaredPositionHandle(decNode);
		}
		//4.ArrayAccess__a[20]
		else if (astNode instanceof ArrayAccess	) {
			ArrayAccess access = (ArrayAccess) astNode;
			return varDeclaredPosition(access.getArray());
		}
		//5.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return varDeclaredPosition(superFieldAccess.getName());
		}
		return DeclarePosition.INMETHOD;
	}
	
	/**
	 * 得到simpleName所属的最终变量全名
	 * @param astNode 变量节点
	 * @return  变量全名
	 */
	public ASTNode getVarFullName(ASTNode astNode) {
		ASTNode pNode = astNode;
		//1.obj.name
		if(pNode.getParent() instanceof QualifiedName){
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof QualifiedName);	
			return pNode;
		}
		//2.this.name
		else if (pNode.getParent() instanceof FieldAccess) {
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof FieldAccess);	
			return pNode;
		}
		//3.super.name
		else if (pNode.getParent() instanceof SuperFieldAccess) {
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof SuperFieldAccess);	
			return pNode;
		}
		
		//4.simepleName
		return astNode;
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
	 * 得到全名的最右名
	 * @param astNode 全名节点
	 * @return 全名的最右变量名
	 */
	public String getObjectName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName&&((SimpleName)astNode).resolveTypeBinding()!=null) {                            
			return ((SimpleName)astNode).resolveTypeBinding().getBinaryName();
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			return getObjectName(qualifiedName.getName());
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			return getObjectName(fieldAccess.getName());
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return getObjectName(superFieldAccess.getName());
		}
		//5.ArrayAccess buffer[tail++]  取buffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getObjectName(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getObjectName(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return getObjectName(methodInvocation.getName());
		}
		return null;
	}
	
	/**
	 * 获取语句最终得到的对象的ITypeBinding
	 * @param astNode ASTNode
	 * @return  对象的ITypeBinding
	 */
	public ITypeBinding getResolveTypeBinding(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName&&((SimpleName)astNode).resolveTypeBinding()!=null) {                            
			return ((SimpleName)astNode).resolveTypeBinding();
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			return getResolveTypeBinding(qualifiedName.getName());
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			return getResolveTypeBinding(fieldAccess.getName());
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return getResolveTypeBinding(superFieldAccess.getName());
		}
		//5.ArrayAccess buffer[tail++]  取buffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getResolveTypeBinding(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getResolveTypeBinding(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation&&((MethodInvocation)astNode).resolveMethodBinding()!=null){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return methodInvocation.resolveMethodBinding().getReturnType();
		}
		return null;
	}
	/**
	 * 判断类型绑定是否为线程相关的类型
	 * @param node ：类型绑定节点
	 * @return 判断结果
	 */
	public boolean isThreadRelate(ITypeBinding node) {
		if (node==null) {
			return false;
		}
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			//正则表达式匹配,用于匹配RecursiveTask<T>
			String rTask ="RecursiveTask<.*>";
		    Pattern recursiveTaskPattern = Pattern.compile(rTask);
		    Matcher rMatcher = recursiveTaskPattern.matcher(supClass);
		    //正则表达式匹配,用于匹配FutureTask<T>
		    String futureTask = "FutureTask<.*>";
		    Pattern futureTaskPattern = Pattern.compile(futureTask);
		    Matcher fmMatcher = futureTaskPattern.matcher(supClass);
			if (supClass.equals("Thread")||supClass.equals("RecursiveAction")||rMatcher.find()||fmMatcher.find()) { 
				return true;
			}
		}
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			//正则表达式匹配，Callable<T>
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			//正则表达式匹配，Future<T>
			String futureMatch = "Future<.*>";
			Pattern futurePattern = Pattern.compile(futureMatch);
			//正则表达式匹配，RunnableFuture<T>
			String rFutureMatch = "RunnableFuture<.*>";
			Pattern rFuturePattern = Pattern.compile(rFutureMatch);
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			for (String interfaceName : interfaceNames) {
				Matcher cMatcher = callablePattern.matcher(interfaceName);
				Matcher fMatcher = futurePattern.matcher(interfaceName);
				Matcher rMatcher = rFuturePattern.matcher(interfaceName);
				if (interfaceName.equals("Runnable")||cMatcher.find()||fMatcher.find()||rMatcher.find()) {
					return true;
				}
			}
		}
		return false;
	}
	// 获取Node所在函数的KEY
	public String methodKey(ASTNode node) {	
		if (node==null) {
			return null;
		}
		//获取所在         < 函数名+参数列表>   （为了区分重载情况）
		ASTNode pNode = node;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==node.getRoot()) {   //属于初始化块，不在函数中则直接返回
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//如果是构造函数则不算在内
		if (methodDeclaration.isConstructor()) {
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
	
	//获取调用函数的key,astNode为MethodInvocaton或者SuperMethodInvocation
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
	//获得SimpleName的declared节点
	public ASTNode getDecNode(SimpleName simpleName) {
		CompilationUnit compilationUnit = (CompilationUnit) simpleName.getRoot();
		ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
		return decNode;
	}
	//返回参数所在函数的参数列表中的index 从0开始
	public int getParaIndex(SimpleName simpleName) {
		ASTNode astNode = getDecNode(simpleName);
		if (astNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration node = (SingleVariableDeclaration)astNode;
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
		}
		return -1;
	}
	/**
	 * 返回simpleName所在函数调用的位置(-1出错，-2为对象，0~n为index)
	 * simpleName:要查询的变量名节点
	 * methodInvoke:变量所在的函数调用节点
	 */
	public int getIndexInMethodInvoke(SimpleName simpleName,ASTNode methodInvoke) {
		if (methodInvoke instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation)methodInvoke;
			ASTNode astNode = methodInvocation.getExpression();
			if (astNode!=null) {
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				//若为调用函数的对象则返回-2
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
					System.out.println("对象调用函数");
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
					System.out.println("INDEX: "+index);
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
					System.out.println("INDEX: "+index);
					return index;
				}
				index++;
			}
		}
		return -1;
	}
}
