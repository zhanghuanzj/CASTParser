package com.CASTVistitors;

import java.util.ArrayList;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.CASTHelper.CASTHelper;
import com.CASTParser.CompileUnit;
import com.MDGHandle.Nodes.NotifyType;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadWaitNode;
import com.MDGHandle.Nodes.WaitType;

public class CASTVisitorSynchronize extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private ArrayList<ThreadNotifyNode> threadNotifyNodes;         //线程通知节点
	private ArrayList<ThreadWaitNode> threadWaitNodes;             //线程阻塞节点
	
	public ArrayList<ThreadNotifyNode> getThreadNotifyNodes() {
		return threadNotifyNodes;
	}

	public void setThreadNotifyNodes(ArrayList<ThreadNotifyNode> threadNotifyNodes) {
		this.threadNotifyNodes = threadNotifyNodes;
	}

	public CASTVisitorSynchronize() {
		super();
		this.threadNotifyNodes = new ArrayList<>();
		this.threadWaitNodes = new ArrayList<>();
	}

	/**
	 * 返回函数调用所在的类名
	 * @param node ： 函数调用节点
	 * @return     ：返回函数调用所在的类
	 */
	public String acquireTheClass(MethodInvocation node) {
		String className="";
		ASTNode parent = node;	
		do {
			parent = parent.getParent();
			if (parent instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration)parent;
				className = typeDeclaration.resolveBinding().getBinaryName();
				break;
			}
		} while (parent != compilationUnit);
		return className;
	}
	
	

	
	/**
	 * 获取唤醒节点以及阻塞节点的信息提取
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().toString();
		//行号
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		if (methodName.equals("notify")||methodName.equals("notifyAll")||
			methodName.equals("signal")||methodName.equals("signalAll")||methodName.equals("countDown")) {
			if (node.getExpression()==null) {
				ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,acquireTheClass(node));
				threadNotifyNodes.add(threadNotifyNode);
			}
			else {
				String objectTypeName = CASTHelper.getInstance().getObjectName(node.getExpression());
				if (objectTypeName!=null) {
					ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,objectTypeName);
					threadNotifyNodes.add(threadNotifyNode);
				}
			}
		}
		else if(methodName.equals("wait")||methodName.equals("await")) {
			if (node.getExpression()==null) {
				ThreadWaitNode threadWaitNode = new ThreadWaitNode(filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,acquireTheClass(node));
				threadWaitNodes.add(threadWaitNode);
			}
			else {
				String objectTypeName = CASTHelper.getInstance().getObjectName(node.getExpression());
				if (objectTypeName!=null) {
					ThreadWaitNode threadWaitNode = new ThreadWaitNode(filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,objectTypeName);
					threadWaitNodes.add(threadWaitNode);
				}
			}
		}
		
		return super.visit(node);
	}
	
	public ArrayList<ThreadWaitNode> getThreadWaitNodes() {
		return threadWaitNodes;
	}

	public void setThreadWaitNodes(ArrayList<ThreadWaitNode> threadWaitNodes) {
		this.threadWaitNodes = threadWaitNodes;
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
