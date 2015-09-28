package com.ast;

import org.eclipse.jdt.core.dom.CompilationUnit;



public class DemoVisitorTest {
	
	public DemoVisitorTest(String path) {
		CompilationUnit comp = ASTViewer.getCompilationUnit(path);
		
		
		DemoVisitor visitor = new DemoVisitor(comp);
		comp.accept(visitor);
	}
	
	public static void main(String args[]){
		DemoVisitorTest dv = new DemoVisitorTest("H:\\Projects\\Zest\\src\\Test\\MainTest.java");
	}
}