package com.iseu.CASTVistitors;

import java.util.ArrayList;
import java.util.HashMap;



import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;


import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ExpressionType;
import com.iseu.Information.MethodInformation;

public class CASTVisitorMethodPre extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private HashMap<String, MethodInformation> changeMethods;

	public CASTVisitorMethodPre() {
		changeMethods = new HashMap<>();
	}
	
	/**
	 * 只有obj.var 或者arr[index]的参数变量才会引起参数的变化
	 * @param astNode
	 * @return
	 */
	public boolean isParameterWillChange(ASTNode astNode) {
		if (astNode instanceof QualifiedName ||astNode instanceof ArrayAccess) {
			return true;
		}
		return false;
	}
	
	/**
	 * 引用重置情况处理
	 * @param key   ： methodKey
	 * @param checkTableIndex
	 */
	public void referenceReset(String key,int checkTableIndex) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			methodInformation.checkTableAdjust(checkTableIndex);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.checkTableAdjust(checkTableIndex);
			changeMethods.put(key, methodInformation);
		}	
	}
	
	/**
	 * 函数会引起调用对象改变
	 * @param key  ：methodKey
	 */
	public void methodRegisterOfObj(String key) {
		if (key == null) {       //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {     //函数已记录
			MethodInformation methodInformation = changeMethods.get(key);
			methodInformation.setObjChange(true);
		} 
		else {                                    //函数未记录
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			changeMethods.put(key, methodInformation);
		}
	}
	
	/**
	 * 函数会引起参数改变
	 * @param key   :methodKey
	 * @param index :参数索引
	 */
	public void methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			methodInformation.parameterChange(index);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.parameterChange(index);
			changeMethods.put(key, methodInformation);
		}
	}
	
	/**
	 * 成员变量或参数变量改变的变量处理
	 * @param astNode : fullName
	 * @param node
	 */
	public void variableHandle(ASTNode astNode,ExpressionType expressionType) {
		//获取最左部的变量
		SimpleName var = (SimpleName)CASTHelper.getInstance().getLeftVarName(astNode);
		if (var!=null) {
			if (var.resolveBinding() instanceof IVariableBinding) {
				IVariableBinding variableBinding = (IVariableBinding) var.resolveBinding();
				//1.成员变量改变
				if (variableBinding.isField()) { 
					methodRegisterOfObj(CASTHelper.getInstance().methodKey(astNode));
				}
				//2.参数变量,且为obj.var||arr[i]
				else if (variableBinding.isParameter()&&isParameterWillChange(astNode)) {  
					methodRegisterOfParameters(CASTHelper.getInstance().methodKey(astNode), variableBinding.getVariableId());
				}
				/*
				 * (1)赋值语句
				 * (2)simpleName
				 * (3)参数变量
				 * -->引用重置
				 */
				else if (expressionType==ExpressionType.ASSIGNMENTEXPRESSION&&
						variableBinding.isParameter()&&!isParameterWillChange(astNode)) {
					referenceReset(CASTHelper.getInstance().methodKey(astNode), variableBinding.getVariableId());
				}
			}
			//3.访问类的变量
			else if (var.resolveTypeBinding()!=null) {     
				ITypeBinding typeBinding = var.resolveTypeBinding();
				if (typeBinding.isClass()) {
					methodRegisterOfObj(CASTHelper.getInstance().methodKey(astNode));
				}
			}
		}
	}

		
	/**
	 * 1.前缀表达式
	 */
	@Override
	public boolean visit(PrefixExpression node) {
		String prefixOp = node.getOperator().toString();
		if (prefixOp.equals("--")||prefixOp.equals("++")) {
			variableHandle(node.getOperand(), ExpressionType.PREEXPRESSION);
		}
		return super.visit(node);
	}
	
	/**
	 * 2.后缀表达式
	 */
	public boolean visit(PostfixExpression node) {	
		String postfixOp = node.getOperator().toString();
		if (postfixOp.equals("++")||postfixOp.equals("--")) {
			variableHandle(node.getOperand(), ExpressionType.POSTEXPRESSION);
		}
		return super.visit(node);
	}
	
	/**
	 * 3.赋值语句
	 */
	@Override
	public boolean visit(Assignment node) {
		variableHandle(node.getLeftHandSide(), ExpressionType.ASSIGNMENTEXPRESSION);
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
