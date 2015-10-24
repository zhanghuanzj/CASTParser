package com.iseu.CASTVistitors;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
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
import org.osgi.framework.wiring.dto.BundleWiringDTO.NodeDTO;

import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.MethodInformation;
import com.iseu.MDGHandle.Nodes.NotifyType;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;
import com.iseu.MDGHandle.Nodes.WaitType;

public class CASTVisitorSrcMethodsPrepare extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private static int methodNum = 0;

	private HashMap<String, MethodInformation> changeMethods;

	public CASTVisitorSrcMethodsPrepare() {
		super();
		changeMethods = new HashMap<>();
	}

	//返回参数所在函数的参数列表中的index 从0开始
	public int getParaIndex(SingleVariableDeclaration node) {
		if (node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) node.getParent();
			List<?> parameters = methodDeclaration.parameters();
			int position = 0;
			for(int i=0;i<parameters.size();i++){
				if (parameters.get(i) instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)parameters.get(i);
					if(singleVariableDeclaration.getName().toString().equals(node.getName().toString())){
						position = i;
						break;
					}
				}	
			}
			return position;
		}
		return -1;
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
	
	// KEY 的确定
	public String methodKey(ExpressionStatement node) {	
		//获取所在         < 函数名+参数列表>   （为了区分重载情况）
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==compilationUnit) {   //属于初始化块则直接返回
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//如果是构造函数则不算在内
		System.out.println(filePath);
		System.out.println(compilationUnit.getLineNumber(methodDeclaration.getStartPosition()));
		if (methodDeclaration.isConstructor()) {
			return null;
		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());                                   //函数名
		List<?> parameters  = methodDeclaration.parameters();  //声明行号
		for (Object object : parameters) {
			if (object instanceof SingleVariableDeclaration	) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)object;
				methodName.append("_"+singleVariableDeclaration.getType().toString().charAt(0));
			}
		}
		//获取所在的   <包+类>  （考虑匿名类情况）
		String className ="";
		ASTNode classNode = methodDeclaration.getParent();
		if (classNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
			if (typeDeclaration.resolveBinding()!=null) {
				className = typeDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}
			
		}
		else if (classNode instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) classNode;
			if (anonymousClassDeclaration.resolveBinding()!=null) {
				className = anonymousClassDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
		if (className==null||className.equals("")) {
			return null;
		}
		int dotPosition = className.lastIndexOf('.');
		if (dotPosition==-1) {
			return null;
		}
		return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
	}
	
	//函数会引起调用对象改变
	public void methodRegisterOfObj(String key,boolean isChange) {
		if (key == null) {       //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {
			MethodInformation methodInformation = changeMethods.get(key);
			methodInformation.setObjChange(isChange||methodInformation.isObjChange());
		}
		else {
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			changeMethods.put(key, methodInformation);
		}
	}
	//函数会引起参数改变
	public void methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			int parameters = methodInformation.getIsParaChange();
			int checkTable = methodInformation.getCheckTable();
			parameters = parameters|(1<<index)&checkTable;          //改变第index 参数的修改情况
			methodInformation.setIsParaChange(parameters);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			int parameters = methodInformation.getIsParaChange();
			methodInformation.setIsParaChange(parameters|(1<<index));
			changeMethods.put(key, methodInformation);
		}
	}
	
	//引用重置情况处理
	public void referenceReset(String key,int checkTableIndex) {
		if (key == null) {     //KEY为空
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //已经存在相应的函数记录
			MethodInformation methodInformation = changeMethods.get(key);
			int checkTable = methodInformation.getCheckTable();
			checkTable = checkTable&(~(1<<checkTableIndex));          //改变第index 参数的修改情况
			methodInformation.setCheckTable(checkTable);
		}
		else {                                                     //没有相应的函数记录
			MethodInformation methodInformation = new MethodInformation();
			int checkTable = methodInformation.getCheckTable();
			methodInformation.setCheckTable(checkTable&(~(1<<checkTableIndex)));
			changeMethods.put(key, methodInformation);
		}	
	}
	public void variableHandle(ASTNode astNode,ExpressionStatement node) {
		if (astNode instanceof SimpleName) {                            //a=b;
			SimpleName simpleName = (SimpleName) astNode;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {  //参数变量，引用重置
				String key = methodKey(node);
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				referenceReset(key, index);
			}
		}
		else if(astNode instanceof QualifiedName) {                      //obj.a = b;
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {  //成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {   //参数变量，由于是对象中变量的改变，所以会引起参数变量的改变
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				String key = methodKey(node);
				methodRegisterOfParameters(key, index);
			}
		}
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
		}
	}
	//处理前缀表达式
	public void handlePrefixChange(PrefixExpression prefixExpression,ExpressionStatement node) {
		//分析前缀表达式变量
		System.out.println("Prefix");
		if (prefixExpression.getOperand() instanceof SimpleName) {           // var++;
			SimpleName simpleName = (SimpleName)prefixExpression.getOperand();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {      //成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量,由于是传值此时不会改变参数变量原本的值
				return;
			}
		}
		else if (prefixExpression.getOperand() instanceof ArrayAccess) {    //数组变量
			ArrayAccess access = (ArrayAccess) prefixExpression.getOperand();
			variableHandle(access.getArray(),node);
		}
		else if(prefixExpression.getOperand() instanceof QualifiedName){     // obj.var++;
			QualifiedName qualifiedName = (QualifiedName)prefixExpression.getOperand();
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {		//成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量，由于是对象中变量的改变，所以会引起参数变量的改变
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				String key = methodKey(node);
				methodRegisterOfParameters(key, index);
			}
		}
		else if (prefixExpression.getOperand() instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) prefixExpression.getOperand();
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
		}
	}
	//处理后缀表达式
	public void handlePostfixChange(PostfixExpression postfixExpression,ExpressionStatement node) {
		//分析后缀表达式变量
		System.out.println("Postfix");
		if (postfixExpression.getOperand() instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)postfixExpression.getOperand(); // var++;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {      //成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量,由于是传值此时不会改变参数变量原本的值
				return;
			}
		}
		else if (postfixExpression.getOperand() instanceof ArrayAccess) {    //数组变量
			ArrayAccess access = (ArrayAccess) postfixExpression.getOperand();
			variableHandle(access.getArray(),node);
		}
		else if(postfixExpression.getOperand() instanceof QualifiedName){         // obj.var++;
			QualifiedName qualifiedName = (QualifiedName)postfixExpression.getOperand();
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {		//成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量，由于是对象中变量的改变，所以会引起参数变量的改变
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				String key = methodKey(node);
				methodRegisterOfParameters(key, index);
			}
		}
		else if (postfixExpression.getOperand() instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) postfixExpression.getOperand();
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
		}
	}
	//处理赋值语句
	public void handleAssignmentChange(Assignment assignment,ExpressionStatement node) {
		System.out.println("Assignment");
		if (assignment.getLeftHandSide() instanceof SimpleName) {                            //a=b;
			SimpleName simpleName = (SimpleName) assignment.getLeftHandSide();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {  //参数变量，引用重置
				String key = methodKey(node);
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				referenceReset(key, index);
			}
		}
		else if (assignment.getLeftHandSide() instanceof ArrayAccess) {    //数组变量
			ArrayAccess access = (ArrayAccess) assignment.getLeftHandSide();
			variableHandle(access.getArray(),node);
		}
		else if(assignment.getLeftHandSide() instanceof QualifiedName) {                      //obj.a = b;
			QualifiedName qualifiedName = (QualifiedName) assignment.getLeftHandSide();
			//找到最外面的那个对象xx.x.i中的xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {
				if (decNode.getParent() instanceof FieldDeclaration) {  //成员变量
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
			else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {   //参数变量，由于是对象中变量的改变，所以会引起参数变量的改变
				int index = getParaIndex((SingleVariableDeclaration)decNode);
				if (index == -1) {
					return;
				}
				String key = methodKey(node);
				methodRegisterOfParameters(key, index);
			}
		}
		else if (assignment.getLeftHandSide() instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) assignment.getLeftHandSide();
			//this.a.b找到最外面的那个对象
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
				if (decNode.getParent() instanceof FieldDeclaration) {
					String key = methodKey(node);
					methodRegisterOfObj(key, true);
				}
			}
		}
	}

	/*
	 * 对各种表达式情况进行分析
	 */
	//前缀表达式
	@Override
	public boolean visit(PrefixExpression node) {
		String prefixOp = node.getOperator().toString();
		if (prefixOp.equals("--")||prefixOp.equals("++")) {
			//获取ExpressionStatement节点
			ASTNode pNode = node.getParent();
			while(!(pNode instanceof ExpressionStatement)){
				pNode = pNode.getParent();
				if (pNode==compilationUnit) {
					return super.visit(node);
				}
			}
			ExpressionStatement expressionStatement = (ExpressionStatement)pNode;
			handlePrefixChange(node,expressionStatement);
		}
		return super.visit(node);
	}
	//后缀表达式
	public boolean visit(PostfixExpression node) {	
		String postfixOp = node.getOperator().toString();
		if (postfixOp.equals("++")||postfixOp.equals("--")) {
			//获取ExpressionStatement节点
			ASTNode pNode = node.getParent();
			while(!(pNode instanceof ExpressionStatement)){
				pNode = pNode.getParent();
				if (pNode==compilationUnit) {
					return super.visit(node);
				}
			}
			ExpressionStatement expressionStatement = (ExpressionStatement)pNode;
			handlePostfixChange(node,expressionStatement);
		}
		return super.visit(node);
	}
	//赋值语句
	@Override
	public boolean visit(Assignment node) {
		//获取ExpressionStatement节点
		ASTNode pNode = node.getParent();
		while(!(pNode instanceof ExpressionStatement)){
			pNode = pNode.getParent();
			if (pNode==compilationUnit) {
				return super.visit(node);
			}
		}
		ExpressionStatement expressionStatement = (ExpressionStatement)pNode;
		handleAssignmentChange(node,expressionStatement);
		return super.visit(node);
	}
	


	
	public HashMap<String, MethodInformation> getChangeMethods() {
		return changeMethods;
	}
	public void setChangeMethods(HashMap<String, MethodInformation> changeMethods) {
		this.changeMethods = changeMethods;
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


