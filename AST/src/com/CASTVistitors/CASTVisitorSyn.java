package com.CASTVistitors;

import java.util.ArrayList;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.CASTParser.CompileUnit;
import com.MDGHandle.Nodes.NotifyType;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadWaitNode;
import com.MDGHandle.Nodes.WaitType;

public class CASTVisitorSyn extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private ArrayList<ThreadNotifyNode> threadNotifyNodes;
	private ArrayList<ThreadWaitNode> threadWaitNodes;
	
	public ArrayList<ThreadNotifyNode> getThreadNotifyNodes() {
		return threadNotifyNodes;
	}

	public void setThreadNotifyNodes(ArrayList<ThreadNotifyNode> threadNotifyNodes) {
		this.threadNotifyNodes = threadNotifyNodes;
	}

	public CASTVisitorSyn() {
		super();
		this.threadNotifyNodes = new ArrayList<>();
		this.threadWaitNodes = new ArrayList<>();
	}

	//返回函数调用所在的类名
	public String acquireTheClass(MethodInvocation node) {
		String className="";
		ASTNode parent = node;	
		do {
			parent = parent.getParent();
			if (parent instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration)parent;
				className = typeDeclaration.resolveBinding().getQualifiedName();	
				if (className.equals("")) {
					className = typeDeclaration.resolveBinding().getBinaryName();
				}
				break;
			}
		} while (parent != compilationUnit);
		return className;
	}
	//获取唤醒节点以及阻塞节点的信息
	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().toString();
		//行号
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		if (methodName.equals("notify")||methodName.equals("notifyAll")||
			methodName.equals("signal")||methodName.equals("signalAll")||methodName.equals("countDown")) {
			if (node.getExpression()==null) {
				ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,acquireTheClass(node),"this");
				threadNotifyNodes.add(threadNotifyNode);
			}
			else {
				if (node.getExpression() instanceof SimpleName) {
					SimpleName simpleName = (SimpleName)node.getExpression();
					String objectTypeName = simpleName.resolveTypeBinding().getQualifiedName();
					String objectName = simpleName.getIdentifier();
					ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,objectTypeName,objectName);
					threadNotifyNodes.add(threadNotifyNode);
				}
			}
		}
		else if(methodName.equals("wait")||methodName.equals("await")) {
			if (node.getExpression()==null) {
				ThreadWaitNode threadWaitNode = new ThreadWaitNode(filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,acquireTheClass(node),"this");
				threadWaitNodes.add(threadWaitNode);
			}
			else {
				if (node.getExpression() instanceof SimpleName) {
					SimpleName simpleName = (SimpleName)node.getExpression();
					String objectTypeName = simpleName.resolveTypeBinding().getQualifiedName();
					String objectName = simpleName.getIdentifier();
					ThreadWaitNode threadWaitNode = new ThreadWaitNode(filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,objectTypeName,objectName);
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
