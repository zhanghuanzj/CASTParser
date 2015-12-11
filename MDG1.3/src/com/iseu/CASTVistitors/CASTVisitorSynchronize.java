package com.iseu.CASTVistitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.MDGHandle.Nodes.Node;
import com.iseu.MDGHandle.Nodes.NotifyType;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;
import com.iseu.MDGHandle.Nodes.WaitType;

public class CASTVisitorSynchronize extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private ArrayList<ThreadNotifyNode> threadNotifyNodes;         //线程通知节点
	private ArrayList<ThreadWaitNode> threadWaitNodes;             //线程阻塞节点
	private HashMap<Node, Node> blockControlDependence;            //阻塞节点的控制依赖处理
	
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
		this.blockControlDependence = new HashMap<>();
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
				if (typeDeclaration.resolveBinding().getBinaryName()==null) {
					className = typeDeclaration.resolveBinding().getKey();
				}
				else{
					className = typeDeclaration.resolveBinding().getBinaryName();
				}			
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
			methodName.equals("signal")||methodName.equals("signalAll")||
			methodName.equals("countDown")||methodName.equals("arrive")) {
			if (node.getExpression()==null) {
				//当expression为null时,所属类与锁对象相同
				ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,acquireTheClass(node));
				threadNotifyNodes.add(threadNotifyNode);
			}
			else {
				String objectTypeName = CASTHelper.getInstance().getObjectName(node.getExpression());
				if (objectTypeName!=null) {
					ThreadNotifyNode threadNotifyNode = new ThreadNotifyNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, acquireTheClass(node), NotifyType.NOTIFY,objectTypeName);
					threadNotifyNodes.add(threadNotifyNode);
				}
			}
		}
		else if(methodName.equals("wait")||methodName.equals("await")||methodName.equals("awaitAdvance")) {
			if (node.getExpression()==null) {
				ThreadWaitNode threadWaitNode = new ThreadWaitNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,acquireTheClass(node));
				threadWaitNodes.add(threadWaitNode);
			}
			else {
				String objectTypeName = CASTHelper.getInstance().getObjectName(node.getExpression());
				if (objectTypeName!=null) {
					ThreadWaitNode threadWaitNode = new ThreadWaitNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, acquireTheClass(node), WaitType.WAIT,objectTypeName);
					threadWaitNodes.add(threadWaitNode);
				}
			}
			blockControlDependenceHandle(node);
		}
		
		return super.visit(node);
	}
	
	public void blockControlDependenceHandle(MethodInvocation node) {
		if (node.resolveMethodBinding()!=null) {
			ITypeBinding[] typeBindings = node.resolveMethodBinding().getExceptionTypes();
			
			for (ITypeBinding iTypeBinding : typeBindings) {
				System.out.println(node.getName());
				if (iTypeBinding.getName().equals("InterruptedException")) {
					Node blockNode = new Node(node.getName().getIdentifier(), filePath, compilationUnit.getLineNumber(node.getStartPosition()));
					System.out.println(getStatementNode(node));
					int nextLineNumber = getNextStatementLine(getStatementNode(node));
					if (nextLineNumber!=-1) {
						blockControlDependence.put(blockNode, new Node(node.getName().getIdentifier(), filePath, nextLineNumber));
					}
				}
			}
		}
		
	}
	
	public Statement getStatementNode(ASTNode node) {
		ASTNode parentNode = node.getParent();
		while(!(parentNode instanceof Statement)&&parentNode!=node.getRoot()){
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof Statement) {
			return (Statement)parentNode;
		}
		else {
			return null;
		}
	}
	
	public int getNextStatementLine(Statement node) {
		if (node==null) {
			return -1;
		}
		ASTNode parentNode = node.getParent();
		while(!(parentNode instanceof Block)&&parentNode!=node.getRoot()){
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof Block) {
			List<Statement> statements = ((Block)parentNode).statements();
			for(int i=0;i<statements.size();++i){
				if (statements.get(i).equals(node)&&i!=statements.size()-1) {
					return compilationUnit.getLineNumber(statements.get(i+1).getStartPosition());
				}
				else if (i==statements.size()-1) {
					return getNextStatementLine(getStatementNode(parentNode));
				}
			}
		}
		return -1;
	}
	
	public ArrayList<ThreadWaitNode> getThreadWaitNodes() {
		return threadWaitNodes;
	}

	public void setThreadWaitNodes(ArrayList<ThreadWaitNode> threadWaitNodes) {
		this.threadWaitNodes = threadWaitNodes;
	}

	public HashMap<Node, Node> getBlockControlDependence() {
		return blockControlDependence;
	}

	public void setBlockControlDependence(HashMap<Node, Node> blockControlDependence) {
		this.blockControlDependence = blockControlDependence;
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
