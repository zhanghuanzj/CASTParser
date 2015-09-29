package com.CASTVistitors;

import java.util.ArrayList;

import javax.print.DocFlavor.STRING;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
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

	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().toString();
		String className ;
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		if (methodName.equals("notify")||methodName.equals("notifyAll")) {
			if (node.getExpression()==null) {
				ASTNode parent = node;	
				do {
					parent = parent.getParent();
					if (parent instanceof TypeDeclaration) {
						TypeDeclaration typeDeclaration = (TypeDeclaration)parent;
						className = typeDeclaration.getName().getFullyQualifiedName();
						ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(filePath, lineNumber, className, NotifyType.NOTIFY,true);
						threadNotifyNodes.add(threadNotifyNode);
						break;
					}
				} while (parent != compilationUnit);
			}
		}
		else if(methodName.equals("wait")) {
			if (node.getExpression()==null) {
				ASTNode parent = node;	
				do {
					parent = parent.getParent();
					if (parent instanceof TypeDeclaration) {
						TypeDeclaration typeDeclaration = (TypeDeclaration)parent;
						className = typeDeclaration.getName().getFullyQualifiedName();
						ThreadWaitNode threadWaitNode = new ThreadWaitNode(filePath, lineNumber, className, WaitType.WAIT, true);
						threadWaitNodes.add(threadWaitNode);
						break;
					}
				} while (parent != compilationUnit);
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
