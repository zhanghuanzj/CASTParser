package com.CASTVistitors;

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

import com.CASTHelper.CASTHelper;
import com.CASTParser.CompileUnit;
import com.Information.ThreadInformation;
import com.Information.ThreadVar;
import com.MDGHandle.Nodes.Node;
import com.MDGHandle.Nodes.ThreadInterruptNode;

public class CASTVisitorInterrupt extends ASTVisitor{
	private CompilationUnit compilationUnit ;
	private String filePath;
	
	private HashMap<String, HashSet<String>> threadMethodMapTable;  //���ڼ�¼�߳������а����ĺ�������
	private HashMap<String, ThreadInformation> threadInfo;			//�߳���Ϣ�����ڼ�¼�жϻ�ȡ�ڵ�
	private Set<ThreadInterruptNode> threadInterruptNodes;          //���ڼ�¼�߳��жϴ����ڵ�
	private HashMap<String, ThreadVar> threadVarHashMap;     	    //�̱߳���
	
	private CASTHelper castHelper;
	

	public CASTVisitorInterrupt(HashMap<String, HashSet<String>> threadMethodMapTable,
			HashMap<String, ThreadInformation> threadInfo, HashMap<String, ThreadVar> threadVarHashMap) {
		super();
		this.threadMethodMapTable = threadMethodMapTable;
		this.threadInfo = threadInfo;
		this.threadVarHashMap = threadVarHashMap;
		this.threadInterruptNodes = new HashSet<>();
	}


	public void interruptNodeHandle(MethodInvocation node,ThreadInterruptNode threadInterruptNode) {
		String threadKey;
		Expression expression = node.getExpression();
		ITypeBinding iTypeBinding = castHelper.getResolveTypeBinding(expression);
		//��ͨ����
		if (expression instanceof SimpleName&&((SimpleName)expression).resolveBinding()!=null) {
			SimpleName simpleName = (SimpleName)expression;
			
			int lineNumber = compilationUnit.getLineNumber(castHelper.getDecNode(simpleName).getStartPosition());
			String varKey = filePath+"_"+lineNumber+"_"+iTypeBinding.getName()+"_"+simpleName.getIdentifier();
			System.out.println(varKey);
			if (threadVarHashMap.containsKey(varKey)) {
				threadKey = threadVarHashMap.get(varKey).getThreadInfoKey();
				threadInterruptNode.getThreadKeyList().add(threadKey);
				//������ж�֪ͨ�ڵ㼯��
				threadInterruptNodes.add(threadInterruptNode);
			}
			else {
				System.out.println("ERROR: didn't contains the varKey");
			}
		}
		//��������
		else{
			threadKey = iTypeBinding.getBinaryName();
			threadInterruptNode.getThreadKeyList().add(threadKey);
		}
	}
	
	@Override
	public boolean visit(MethodInvocation node) {	
		String methodKey = castHelper.methodKey(node);
		String methodName = node.getName().toString();
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		//�����������̵߳��õ��ĺ�����
		if (threadMethodMapTable.containsKey(methodKey)&&node.resolveMethodBinding()!=null) {
			IMethodBinding iMethodBinding = node.resolveMethodBinding();
			//1.�ж��жϺ���(�жϽ��ܽڵ�)
			if ((methodName.equals("interrupted")||methodName.equals("isInterrupted"))) {
				if (iMethodBinding.getDeclaringClass()!=null&&iMethodBinding.getDeclaringClass().getBinaryName().equals("java.lang.Thread")) {
					System.out.println(node);
					System.out.println("interrupted judge");
					HashSet<String>threads = threadMethodMapTable.get(methodKey);
					//��ӵ�ÿ���̵߳��жϽ��ܽڵ㼯��
					for (String thread : threads) {
						ThreadInformation threadInformation = threadInfo.get(thread);
						Node interruptedNode = new Node(filePath, lineNumber);
						threadInformation.getInterruptNodes().add(interruptedNode);
					}
				}
			}
			//2.�����жϵĺ�������(�ж�֪ͨ�ڵ�)
			else if (methodName.equals("interrupt")) {
				ThreadInterruptNode threadInterruptNode = new ThreadInterruptNode(filePath, lineNumber);
				//(1)���ж�
				if (node.getExpression() instanceof MethodInvocation&&
					((MethodInvocation) node.getExpression()).getName().getIdentifier().equals("currentThread")) {	
					//���п��ܵ��ø����жϵ��߳���ӵ��ж�֪ͨ�ڵ���
					HashSet<String>threads = threadMethodMapTable.get(methodKey);
					for (String thread : threads) {
						threadInterruptNode.getThreadKeyList().add(thread);
					}
					threadInterruptNodes.add(threadInterruptNode);
				}
				//(2)�����ж�,ͨ����������λthreadKey
				else if (iMethodBinding.getDeclaringClass()!=null&&iMethodBinding.getDeclaringClass().getBinaryName().equals("java.lang.Thread")) {
					interruptNodeHandle(node,threadInterruptNode);
				}
			}
			//3.���׳��ж��쳣�ĺ���(�жϽ��ܽڵ�)
			else{
				ITypeBinding []typeBindings = node.resolveMethodBinding().getExceptionTypes();
				for (ITypeBinding iTypeBinding : typeBindings) {
					if (iTypeBinding.getBinaryName().equals("java.lang.InterruptedException")) {
						System.out.println(node);
						System.out.println("interrupted exception");
						HashSet<String>threads = threadMethodMapTable.get(methodKey);
						//��ӵ�ÿ���̵߳��жϽ��ܽڵ㼯��
						for (String thread : threads) {
							ThreadInformation threadInformation = threadInfo.get(thread);
							Node interruptedNode = new Node(filePath, lineNumber);
							threadInformation.getInterruptNodes().add(interruptedNode);
						}
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
