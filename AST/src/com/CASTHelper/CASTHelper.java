package com.CASTHelper;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
		return DeclarePosition.INMETHOD;
	}
}
