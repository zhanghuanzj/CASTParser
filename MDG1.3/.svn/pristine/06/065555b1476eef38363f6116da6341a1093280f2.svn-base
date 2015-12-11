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
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.mozilla.javascript.ast.AstNode;
import org.neo4j.cypher.internal.compiler.v2_1.perty.docbuilders.simpleDocBuilder;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadType;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTVisitorTestHelper extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	
	String getVarName(ASTNode node)
	{
		if (node instanceof SimpleName) {
			return ((SimpleName)node).getIdentifier();
		}
		else if (node instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName)node;
			if (qualifiedName.getQualifier() instanceof QualifiedName) {
				return getVarName(qualifiedName.getQualifier());
			}
			return getVarName(qualifiedName.getQualifier())+"."+getVarName(qualifiedName.getName());
		}
		return null;
	}
	public boolean visit(Assignment node) {
		String left = getVarName(node.getLeftHandSide());
		String right = getVarName(node.getRightHandSide());
		if (left!=null&&right!=null) {
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
		}
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