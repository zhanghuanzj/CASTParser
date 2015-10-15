package com.CASTVistitors;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.CASTParser.CompileUnit;

public class CASTVisitorTEST extends ASTVisitor{
	private CompilationUnit compilationUnit ;
	private String filePath;
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		System.out.println(node);
		return super.visit(node);
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
