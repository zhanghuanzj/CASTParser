package com.CASTParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class CompileUnit {
	private String filePath;
	private CompilationUnit compilationUnit;
	public CompileUnit(String filePath, CompilationUnit compilationUnit) {
		this.filePath = filePath;
		this.compilationUnit = compilationUnit;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}
	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}	
}
