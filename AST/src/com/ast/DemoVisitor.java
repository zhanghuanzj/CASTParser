package com.ast;


import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.w3c.dom.Node;


public class DemoVisitor extends ASTVisitor {

	CompilationUnit compilationUnit ;
	public DemoVisitor(CompilationUnit cu) {
		compilationUnit = cu;
		
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//有父类，则与线程相关的父类进行匹配
		if (node.getSuperclassType()!=null) {
			System.out.println("Has super class");
			String supClass = node.getSuperclassType().toString();
			System.out.println(supClass);
			
			//正则表达式匹配
			String mre ="RecursiveTask<.*>";
		    Pattern p = Pattern.compile(mre);
		    Matcher m = p.matcher(supClass);
			
			if (supClass.equals("Thread")) { 
				System.out.println("Find thread!");
				System.out.println(supClass);
			}
			else if(supClass.equals("RecursiveAction")) {
				System.out.println("Find RecursiveAction!");
				System.out.println(supClass);
			}
			else if(m.find()) {
				System.out.println("Find RecursiveTask<T>!");
				System.out.println(supClass);
			}
		}
		//有与线程相关的接口
		else if(!node.superInterfaceTypes().isEmpty()){
			System.out.println("Has interface");
			
			List<?> list = node.superInterfaceTypes();
			List<String> newList = new ArrayList<>();
			for (Object ele : list) {
				newList.add(ele.toString());
			}
			if (newList.contains("Runnable")) {
				System.out.println("Find Runnable");
				
				for (MethodDeclaration methodDec : node.getMethods()) {
					if (methodDec.getName().toString().equals("run")) {
						System.out.println("get the run method at line:"+(compilationUnit.getLineNumber(methodDec.getStartPosition())+1));
					}
				}

			}
			else if (newList.contains("Callable")) {
				System.out.println("Find Callable");
			}
			return false;
		}

		return true;
	}
	@Override
	public boolean visit(MethodInvocation node) {
		// TODO Auto-generated method stub
		String methodName = node.getName().toString();
		if (methodName.equals("await")) {
			if (node.getExpression()!=null) {
				
				String objectName = node.getExpression().resolveTypeBinding().getName().toString();
				   if (objectName.equals("CountDownLatch")) {
					System.out.println("CountDownLatch");
				   }
				   else if(objectName.equals("Condition")){
					   System.out.println("Condition");
				   }
				   else {
					System.out.println("other");
				}
			}
			System.out.println(node.getName());
			System.out.println(node.getExpression());
		}
		else if (methodName.equals("interrupt")) {
			System.out.println(node.getName());
			System.out.println(compilationUnit.findDeclaringNode(node.getExpression().resolveTypeBinding()));
			System.out.println(compilationUnit.findDeclaringNode(node.getExpression().resolveTypeBinding()));
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		// TODO Auto-generated method stub
		String VariableClassName = node.getType().resolveBinding().getName().toString();
		if (VariableClassName.equals("Semaphore")) {
			System.out.println(VariableClassName);
		}
		
		return super.visit(node);
	}

}
