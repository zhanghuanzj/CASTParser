package com.iseu.CASTVistitors;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.Information.MethodInformation;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTVisitorComHelper extends ASTVisitor {
	private String filePath;
	private CompilationUnit compilationUnit;
	private String varType;
	private HashMap<String, ThreadInformation> threadInfo;			//线程信息表，用于记录def，use
	private HashMap<String, ThreadVar> threadVarHashMap;             //线程变量哈希表
	private boolean isEffect ;
	
	public CASTVisitorComHelper(String filePath, CompilationUnit compilationUnit,
			HashMap<String, ThreadInformation> threadInfo, HashMap<String, ThreadVar> threadVarHashMap) {
		super();
		this.filePath = filePath;
		this.compilationUnit = compilationUnit;
		this.varType = null;
		this.threadInfo = threadInfo;
		this.threadVarHashMap = threadVarHashMap;
		this.isEffect = false;
	}

	@Override
	public boolean visit(MethodInvocation methodInvoke) {	
		String methodName = methodInvoke.getName().toString();
		if (methodName.equals("start")) {
			Expression ex = methodInvoke.getExpression();
			isEffect = isEffect|isThreadEffectTheVar(ex, methodInvoke);     //获取触发节点的信息
		}
		else if (methodName.equals("invokeAll")||
				 methodName.equals("invokeAny")||
				 methodName.equals("execute")||
				 methodName.equals("invoke")||
				 methodName.equals("submit")) {		
			List<?> arguments = methodInvoke.arguments();
			for (Object object : arguments) {
				if (object instanceof ASTNode) {
					ASTNode argument = (ASTNode) object;
					isEffect = isEffect|isThreadEffectTheVar(argument, methodInvoke);
				}	
			}
		}
		return super.visit(methodInvoke);
	}
	
	//判断线程是否有可能对变量产生影响
	private boolean isThreadEffectTheVar(ASTNode astNode,MethodInvocation node) {
		Set<String> threadVarTypeSet = getThreadVarTypeSet(astNode, node);
		if (threadVarTypeSet==null) {
			return false;
		}
		else{
			return threadVarTypeSet.contains(varType);
		}
	}
	
	/**
	 * 获取线程的成员变量类型集
	 * @param astNode 线程变量
	 * @param node    启动函数调用
	 * @return  线程的成员变量类型集
	 */
	private Set<String> getThreadVarTypeSet(ASTNode astNode,MethodInvocation node) {
		String threadVarHMKey = threadVarKey(astNode, node);
		if (threadVarHashMap.containsKey(threadVarHMKey)&&threadInfo.containsKey(threadVarHashMap.get(threadVarHMKey).getBindingTypeName())) {
			ThreadInformation threadInformation = threadInfo.get(threadVarHashMap.get(threadVarHMKey).getBindingTypeName());
			return threadInformation.getVariableTypeSet();
		}
		//函数返回，则varkey直接为线程key
		else if (threadInfo.containsKey(threadVarHMKey)) {
			ThreadInformation threadInformation = threadInfo.get(threadVarHMKey);
			return threadInformation.getVariableTypeSet();
		}
		return null;
	}
	
	//获取触发节点的varkey
	private String threadVarKey(ASTNode astNode,MethodInvocation node) {
		CompilationUnit compilationUnit = (CompilationUnit) astNode.getRoot();
		//1.匿名类的调用new Thread(){}.start()
		if(astNode instanceof ClassInstanceCreation){  
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) astNode;
			if (CASTHelper.getInstance().isThreadRelate(classInstanceCreation.getType().resolveBinding())) { 				///确保调用对象线程相关			
				int decLineNumber = compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()); //匿名对象声明处
				String typeName = classInstanceCreation.getType().toString();
				String varName = classInstanceCreation.resolveTypeBinding().getKey();
				return filePath+"_"+decLineNumber+"_"+typeName+"_"+varName;			
			}
		}
		//2.Thread对象的函数调用astNode.start()  或者   invoke(astNode...)
		if(CASTHelper.getInstance().isThreadRelate(CASTHelper.getInstance().getResolveTypeBinding(astNode))) {      //确保调用对象线程相关	
			//变量考虑多态处理
			if (astNode instanceof SimpleName) {
				SimpleName simpleName = (SimpleName)astNode;
				ASTNode decAstNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding()); 
				if (decAstNode==null) {
					return null;
				}
				int decLineNumber = compilationUnit.getLineNumber(decAstNode.getStartPosition());
				String typeName = simpleName.resolveTypeBinding().getName();
				String varName = simpleName.getIdentifier().toString();
				return filePath+"_"+decLineNumber+"_"+typeName+"_"+varName;
			}
			//函数返回值等，直接考虑得到的类型
			else {
				ITypeBinding iTypeBinding = CASTHelper.getInstance().getResolveTypeBinding(astNode);
				return iTypeBinding.getKey();
			}
		}
		return null;
	}

	public boolean isEffect() {
		return isEffect;
	}

	public void setEffect(boolean isEffect) {
		this.isEffect = isEffect;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
	
	
	
}
