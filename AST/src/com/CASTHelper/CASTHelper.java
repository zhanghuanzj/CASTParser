package com.CASTHelper;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.Information.DeclarePosition;

public class CASTHelper {
	private final static CASTHelper CAST_HELPER = new CASTHelper();
	private CASTHelper() {	
		
	}
	public static CASTHelper getInstance() {
		return CAST_HELPER;
	}
	//查看变量是否为函数内部的局部变量
	public DeclarePosition varDeclaredInCurrentMethod(ASTNode decNode) {
		//(1)成员变量
		if (decNode instanceof VariableDeclarationFragment) {   
			if (decNode.getParent() instanceof FieldDeclaration) {  
				return DeclarePosition.INMEMBER;
			}
		}
		//(2)参数变量
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {  
			return DeclarePosition.INPARAMETER;
		}
		//不是成员变量和参数变量，或者java源码中的变量（null找不到）则归为INMETHOD，不做处理
		return DeclarePosition.INMETHOD;
	}
	//查看ASTNode中的变量是否为函数内部的局部变量
	public DeclarePosition isDeclaredInCurrentMethod(ASTNode astNode) {
		CompilationUnit compilationUnit = (CompilationUnit) astNode.getRoot();
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			SimpleName simpleName = (SimpleName) astNode;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return varDeclaredInCurrentMethod(decNode);
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return varDeclaredInCurrentMethod(decNode);
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return varDeclaredInCurrentMethod(decNode);
		}
		//4.ArrayAccess__a[20]
		else if (astNode instanceof ArrayAccess	) {
			ArrayAccess access = (ArrayAccess) astNode;
			return isDeclaredInCurrentMethod(access.getArray());
		}
		//5.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return isDeclaredInCurrentMethod(superFieldAccess.getName());
		}
		return DeclarePosition.INMETHOD;
	}
	//得到simpleName所属的最终变量全名
	public ASTNode getVarName(ASTNode astNode) {
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
	//得到关键的simpleName
	public ASTNode getKeyVarName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			return astNode;
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//找到最外面的那个对象xx.x.i中的xx
			if(!(qualifiedName.getQualifier() instanceof SimpleName)){
				return getKeyVarName(qualifiedName.getQualifier());
			}
			return qualifiedName.getQualifier();
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b找到最外面的那个对象
			if (!(fieldAccess.getExpression() instanceof SimpleName)) {
				return getKeyVarName(fieldAccess.getExpression());
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
			return getKeyVarName(access.getArray());
		}
		return null;
	}
	// 获取Node所在函数的KEY
	public String methodKey(ASTNode node) {	
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
	/*
	 * 返回simpleName所在函数调用的位置(-1出错，-2为对象，0~n为index)
	 * simpleName:要查询的变量名节点
	 * methodInvoke:变量所在的函数调用节点
	 */
	public int getIndexInMethodInvoke(SimpleName simpleName,ASTNode methodInvoke) {
		if (methodInvoke instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation)methodInvoke;
			ASTNode astNode = methodInvocation.getExpression();
			if (astNode!=null) {
				SimpleName varName = (SimpleName)getKeyVarName(astNode);
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
				SimpleName varName = (SimpleName)getKeyVarName(astNode);
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
				SimpleName varName = (SimpleName)getKeyVarName(astNode);
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
