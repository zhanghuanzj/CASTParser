package com.iseu.CASTVistitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadType;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.Node;
import com.iseu.MDGHandle.Nodes.NotifyType;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;
import com.iseu.MDGHandle.Nodes.WaitType;

public class CASTVisitorTest extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	
	/**
	 * 获取唤醒节点以及阻塞节点的信息提取
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		blockControlDependenceHandle(node);
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
						System.out.println("FROM:");
						System.out.println(blockNode);
						System.out.println("TO:");
						System.out.println(nextLineNumber);
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
	
	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}
