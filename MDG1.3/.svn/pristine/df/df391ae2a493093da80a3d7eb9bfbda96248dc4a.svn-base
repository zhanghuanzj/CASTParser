package com.iseu.CASTVistitors;

import java.awt.datatransfer.FlavorEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.Node;
import com.iseu.MDGHandle.Nodes.ThreadInterruptNode;

public class CASTVisitorInterrupt extends ASTVisitor{
	private CompilationUnit compilationUnit ;
	private String filePath;
	
	private HashMap<String, HashSet<String>> threadMethodMapTable;  //用于记录线程运行中包含的函数调用
	private HashMap<String, ThreadInformation> threadInfo;			//线程信息表，用于记录中断获取节点
	private Set<ThreadInterruptNode> threadInterruptNodes;          //用于记录线程中断触发节点
	private HashMap<String, ThreadVar> threadVarHashMap;     	    //线程变量
	
	private CASTHelper castHelper;
	

	public CASTVisitorInterrupt(HashMap<String, HashSet<String>> threadMethodMapTable,
			HashMap<String, ThreadInformation> threadInfo, HashMap<String, ThreadVar> threadVarHashMap) {
		super();
		this.threadMethodMapTable = threadMethodMapTable;
		this.threadInfo = threadInfo;
		this.threadVarHashMap = threadVarHashMap;
		this.threadInterruptNodes = new HashSet<>();
	}

	/**
	 * 中断通知节点
	 * @param node ：函数调用
	 * @param threadInterruptNode ：中断节点类
	 */
	public void interruptNotifyNodeHandle(MethodInvocation node,ThreadInterruptNode threadInterruptNode) {
		String threadKey;
		Expression expression = node.getExpression();
		ITypeBinding iTypeBinding = castHelper.getResolveTypeBinding(expression);
		if (expression==null||iTypeBinding==null) {
			System.out.println(filePath);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("interrupt iTypeBinding error!");
			return;
		}
		//普通变量，从变量集中获取线程key
		if (expression instanceof SimpleName&&((SimpleName)expression).resolveBinding()!=null) {
			SimpleName simpleName = (SimpleName)expression;
			
			int lineNumber = compilationUnit.getLineNumber(castHelper.getDecNode(simpleName).getStartPosition());
			String varKey = filePath+"_"+lineNumber+"_"+iTypeBinding.getName()+"_"+simpleName.getIdentifier();
			System.out.println(varKey);
			if (threadVarHashMap.containsKey(varKey)) {
				threadKey = threadVarHashMap.get(varKey).getBindingTypeName();
				threadInterruptNode.getThreadKeyList().add(threadKey);
				//添加至中断通知节点集合
				threadInterruptNodes.add(threadInterruptNode);
			}
			else {
				System.out.println("ERROR: didn't contains the varKey");
			}
		}
		//其它调用(函数返回值等)：直接从返回值类型推断具体线程key
		else{
			threadKey = iTypeBinding.getKey();
			threadInterruptNode.getThreadKeyList().add(threadKey);
		}
	}
	
	/**
	 * 中断接受节点
	 * @param node ： 函数调用
	 */
	public void interruptAcceptNodeHandle(MethodInvocation node) {
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		Node interruptedNode = new Node(castHelper.getMethodName(node),filePath, lineNumber);
		String threadKey;
		Expression expression = node.getExpression();
		ITypeBinding iTypeBinding = castHelper.getResolveTypeBinding(expression);
		if (expression==null||iTypeBinding==null) {
			System.out.println(filePath);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("interrupt iTypeBinding error!");
			return;
		}
		System.out.println("EXPRESSION:"+expression);
		//普通变量
		if (expression instanceof SimpleName&&((SimpleName)expression).resolveBinding()!=null) {
			SimpleName simpleName = (SimpleName)expression;
			if (castHelper.getDecNode(simpleName)==null) {
				return;
			}
			int decLineNumber = compilationUnit.getLineNumber(castHelper.getDecNode(simpleName).getStartPosition());
			String varKey = filePath+"_"+decLineNumber+"_"+iTypeBinding.getName()+"_"+simpleName.getIdentifier();
			System.out.println(varKey);
			if (threadVarHashMap.containsKey(varKey)) {
				threadKey = threadVarHashMap.get(varKey).getBindingTypeName();
				if (threadInfo.get(threadKey).getInterruptNodes()!=null) {
					threadInfo.get(threadKey).getInterruptNodes().add(interruptedNode);
				}
			}
			else {
				System.out.println("ERROR: didn't contains the varKey");
			}
		}
		//其它调用
		else{
			threadKey = iTypeBinding.getKey();
			if (threadInfo.get(threadKey)!=null&&
				threadInfo.get(threadKey).getInterruptNodes()!=null) {
				threadInfo.get(threadKey).getInterruptNodes().add(interruptedNode);
			}		
		}
	}
	
	@Override
	public boolean visit(MethodInvocation node) {	
		String methodKey = castHelper.methodKey(node);
		String methodName = node.getName().toString();
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		//函数调用在线程调用到的函数中(单个函数可能牵扯到多个线程)
		if (threadMethodMapTable.containsKey(methodKey)&&node.resolveMethodBinding()!=null) {
			IMethodBinding iMethodBinding = node.resolveMethodBinding();
			//1.中断判断函数(中断接受节点)
			if ((methodName.equals("interrupted")||methodName.equals("isInterrupted"))) {
				if (iMethodBinding.getDeclaringClass()!=null&&iMethodBinding.getDeclaringClass().getBinaryName().equals("java.lang.Thread")) {
					System.out.println(node);
					System.out.println("interrupted judge");
					HashSet<String>threads = threadMethodMapTable.get(methodKey);
					//(1)添加到每个线程的中断接受节点集合(调用该语句所在函数的线程)
					for (String thread : threads) {
						ThreadInformation threadInformation = threadInfo.get(thread);
						Node interruptedNode = new Node(castHelper.getMethodName(node),filePath, lineNumber);
						threadInformation.getInterruptNodes().add(interruptedNode);
					}
					//(2)变量自身判断(根据自己所绑定的线程类型而添加中断接受节点)
					interruptAcceptNodeHandle(node);
				}
			}
			//2.引发中断的函数调用(中断通知节点)
			else if (methodName.equals("interrupt")) {
				ThreadInterruptNode threadInterruptNode = new ThreadInterruptNode(castHelper.getMethodName(node),filePath, lineNumber);
				//(1)自中断
				if (node.getExpression() instanceof MethodInvocation&&
					((MethodInvocation) node.getExpression()).getName().getIdentifier().equals("currentThread")) {	
					//对有可能调用该自中断的线程添加到中断通知节点中
					HashSet<String>threads = threadMethodMapTable.get(methodKey);
					for (String thread : threads) {
						threadInterruptNode.getThreadKeyList().add(thread);
					}
					threadInterruptNodes.add(threadInterruptNode);
				}
				//(2)调用中断,通过变量来定位threadKey
				else if (iMethodBinding.getDeclaringClass()!=null&&iMethodBinding.getDeclaringClass().getBinaryName().equals("java.lang.Thread")) {
					interruptNotifyNodeHandle(node,threadInterruptNode);
				}
			}
			//3.会抛出中断异常的函数(中断接受节点)
			else{
				ITypeBinding []typeBindings = node.resolveMethodBinding().getExceptionTypes();
				for (ITypeBinding iTypeBinding : typeBindings) {
					if (iTypeBinding.getBinaryName().equals("java.lang.InterruptedException")) {
						System.out.println(node);
						System.out.println("interrupted exception");
						HashSet<String>threads = threadMethodMapTable.get(methodKey);
						//(1)添加到每个线程的中断接受节点集合
						for (String thread : threads) {
							ThreadInformation threadInformation = threadInfo.get(thread);
							Node interruptedNode = new Node(castHelper.getMethodName(node),filePath, lineNumber);
							threadInformation.getInterruptNodes().add(interruptedNode);
						}
						System.out.println("BLOCK:"+node);
						//(2)变量自身判断(根据自己所绑定的线程类型而添加中断接受节点)(调用该语句所在函数的线程)
						interruptAcceptNodeHandle(node);
					}
				}
			}
		}
		return super.visit(node);
	}
	
	public Set<ThreadInterruptNode> getThreadInterruptNodes() {
		return threadInterruptNodes;
	}

	public void setThreadInterruptNodes(Set<ThreadInterruptNode> threadInterruptNodes) {
		this.threadInterruptNodes = threadInterruptNodes;
	}

	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		castHelper = CASTHelper.getInstance();
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}
