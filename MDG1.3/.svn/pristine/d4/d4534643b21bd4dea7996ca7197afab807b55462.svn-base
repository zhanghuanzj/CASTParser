package com.iseu.DataDependenceHelper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class CFileASTRequestor extends FileASTRequestor{
	private ArrayList<CompileUnit> compileUnits;
	
	public CFileASTRequestor() {
		compileUnits = new ArrayList<>();
	}

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		super.acceptAST(sourceFilePath, ast);
		CompileUnit compileUnit = new CompileUnit(sourceFilePath,ast);
		compileUnits.add(compileUnit);
	}

	public ArrayList<CompileUnit> getCompileUnits() {
		return compileUnits;
	}

	public void setCompileUnits(ArrayList<CompileUnit> compileUnits) {
		this.compileUnits = compileUnits;
	}
}
