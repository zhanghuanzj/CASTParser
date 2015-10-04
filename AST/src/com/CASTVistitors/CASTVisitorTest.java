package com.CASTVistitors;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.CASTParser.CompileUnit;
import com.MDGHandle.Nodes.NotifyType;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadWaitNode;
import com.MDGHandle.Nodes.WaitType;

public class CASTVisitorTest extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private static int methodNum = 0;
	private static ArrayList<String> methods;

	private HashSet<String> changeMethods;
	private HashSet<String> unChangeMethods;
	private HashSet<String> unCertainMethods;
	
	static {

		methods = new ArrayList<String>(){
			{
				add("notify");
				add("wait");
				add("await");
				add("signal");
				add("notifyAll");
				add("signalAll");
				add("get");
				add("set");
				add("is");
				add("run");
				add("call");
				add("toString");
				add("add");
			}
		};
	}
	public CASTVisitorTest() {
		super();
		changeMethods = new HashSet<>();
		unCertainMethods = new HashSet<>();
		unChangeMethods = new HashSet<>();
	}

	//返回函数调用所在的类名
	public String acquireTheClass(MethodInvocation node) {
		String className="";
		ASTNode parent = node;	
		do {
			parent = parent.getParent();
			if (parent instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration)parent;
				className = typeDeclaration.resolveBinding().getQualifiedName();	
				if (className.equals("")) {
					className = typeDeclaration.resolveBinding().getBinaryName();
				}
				break;
			}
		} while (parent != compilationUnit);
		return className;
	}
	
//	@Override
//	public boolean visit(MethodInvocation node) {
//		System.out.println(node.getName());
//		System.out.println(node.resolveMethodBinding().getMethodDeclaration() );
//		return super.visit(node);
//	}
	
//	@Override
//	public boolean visit(MethodDeclaration node) {
//		//获取所在类
//		ITypeBinding typeBinding = node.resolveBinding().getDeclaringClass();
//		if (!typeBinding.isClass()) {    //若为接口则无法判断其函数对成员改变性
//			return super.visit(node);
//		}
//		//获取包名+类名+函数名+行号（KEY）
//		String className = typeBinding.getQualifiedName();
//		if (className.equals("")) {
//			className = typeBinding.getBinaryName();
//		}
//		String methodName = node.getName().toString()+compilationUnit.getLineNumber(node.getName().getStartPosition());
//		//参数列表
//		IVariableBinding [] variableBindings = typeBinding.getDeclaredFields();
//		if (variableBindings.length==0) {
//			unChangeMethods.add(className+methodName);
//			--methodNum;
//		}
//
//		System.out.println("________________________"+(++methodNum)+"_________________________________");
//		
//		System.out.println(filePath);
//		
//		System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
//		List<Statement> statements = node.getBody().statements();
//		for (Statement statement : statements) {
//			if (statement instanceof ExpressionStatement) {
//				
//			}
//		}
//		System.out.println("________________________"+methodNum+"_________________________________");
//		return super.visit(node);
//	}
	 
	public boolean isPrefixChange(PrefixExpression prefixExpression) {
		//分析前缀表达式变量
		System.out.println("Prefix");
		if (prefixExpression.getOperand() instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)prefixExpression.getOperand();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {
					return true;
				}
			}
			else if (decNode instanceof SingleVariableDeclaration) {
				return true;
			}
		}
		else if(prefixExpression.getOperand() instanceof QualifiedName){
			QualifiedName qualifiedName = (QualifiedName)prefixExpression.getOperand();
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {
					return true;
				}
			}
			else if (decNode instanceof SingleVariableDeclaration) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPostfixChange(PostfixExpression postfixExpression) {
		//分析后缀表达式变量
		System.out.println("Postfix");
		if (postfixExpression.getOperand() instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)postfixExpression.getOperand();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					return true;
				}
			}
			else if (decNode instanceof SingleVariableDeclaration) {  //参数变量
				return true;
			}
		}
		else if(postfixExpression.getOperand() instanceof QualifiedName){
			QualifiedName qualifiedName = (QualifiedName)postfixExpression.getOperand();
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {
					return true;
				}
			}
			else if (decNode instanceof SingleVariableDeclaration) {
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean visit(ExpressionStatement node) {
		/*
		 * 对各种表达式情况进行分析
		 */
		boolean isChange = false;          //用于判断对象是否改变
		//前缀表达式
		if (node.getExpression() instanceof PrefixExpression) {
			PrefixExpression prefixExpression = (PrefixExpression)node.getExpression();
			String prefixOp = prefixExpression.getOperator().toString();
			if (prefixOp.equals("--")||prefixOp.equals("++")) {
				isChange = isPrefixChange(prefixExpression);
			}
		}
		else if (node.getExpression() instanceof PostfixExpression) {
			PostfixExpression postfixExpression = (PostfixExpression)node.getExpression();
			String postfixOp = postfixExpression.getOperator().toString();
			if (postfixOp.equals("++")||postfixExpression.equals("--")) {
				isChange = isPostfixChange(postfixExpression);
			}
		}
		
		if (isChange) {
			//获取所在         < 函数名+行号>   （为了区分重载情况）
			ASTNode pNode = node.getParent() ;
			while(!(pNode instanceof MethodDeclaration)){
				if (pNode instanceof Initializer) {   //属于初始化块则直接返回
					return super.visit(node);
				}
				pNode = pNode.getParent();
			} 
			MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
			String methodName = methodDeclaration.getName().toString();                                   //函数名
			int decLine = compilationUnit.getLineNumber(methodDeclaration.getName().getStartPosition());  //声明行号
			
			//获取所在的   <包+类>  （考虑匿名类情况）
			String className ="";
			ASTNode classNode = methodDeclaration.getParent();
			if (classNode instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
				if (typeDeclaration.resolveBinding()!=null) {
					className = typeDeclaration.resolveBinding().getQualifiedName();
				}
				else {
					return super.visit(node);
				}
				
			}
			else if (classNode instanceof AnonymousClassDeclaration) {
				AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) classNode;
				if (anonymousClassDeclaration.resolveBinding()!=null) {
					className = anonymousClassDeclaration.resolveBinding().getBinaryName();
				}
				else {
					return super.visit(node);
				}
			}
			else {
				return super.visit(node);
			}
			changeMethods.add(className+methodName+decLine);
		}
		return super.visit(node);
	}
	
	public HashSet<String> getChangeMethods() {
		return changeMethods;
	}

	public void setChangeMethods(HashSet<String> changeMethods) {
		this.changeMethods = changeMethods;
	}

	public HashSet<String> getUnChangeMethods() {
		return unChangeMethods;
	}

	public void setUnChangeMethods(HashSet<String> unChangeMethods) {
		this.unChangeMethods = unChangeMethods;
	}

	public HashSet<String> getUnCertainMethods() {
		return unCertainMethods;
	}

	public void setUnCertainMethods(HashSet<String> unCertainMethods) {
		this.unCertainMethods = unCertainMethods;
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


