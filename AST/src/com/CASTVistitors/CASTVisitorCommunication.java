package com.CASTVistitors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;
import javax.swing.plaf.synth.SynthSpinnerUI;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
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
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.xml.sax.ext.Attributes2;

import com.CASTHelper.CASTHelper;
import com.CASTParser.CompileUnit;
import com.Information.DeclarePosition;
import com.Information.MethodInformation;
import com.Information.ThreadInformation;
import com.Information.ShareVarInfo;

public class CASTVisitorCommunication extends ASTVisitor{
	private CompilationUnit compilationUnit ;
	private String filePath;
	private HashMap<String, String> threadMethodMapTable;     		//用于记录线程运行中包含的函数调用
	private HashMap<String, ThreadInformation> threadInfo;			//线程信息表，用于记录def，use
	private CASTHelper castHelper;
	private static int accessNum = 1;

	private HashMap<String, MethodInformation> sourceMethodsInfo;   //工程函数信息	
	private HashMap<String, MethodInformation> javaMethodsInfo; 	//java源码函数信息
	private HashMap<String, Integer> sourceMethodsMapTable;     	//工程包名映射表
	private HashMap<String, Integer> javaMethodsMapTable;       	//java包名映射表

	{
		File file = new File("javaMethodsInfo\\javaMethodInfo.obj");
		File file2 = new File("javaMethodsInfo\\javaMethodMapTable.obj");
		File file3 = new File("srcMethodInfo.obj");
		File file4 = new File("srcMethodMapTable.obj");
		FileInputStream fileInputStream;
		FileInputStream fileInputStream2;
		FileInputStream fileInputStream3;
		FileInputStream fileInputStream4;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream2 = new FileInputStream(file2);
			fileInputStream3 = new FileInputStream(file3);
			fileInputStream4 = new FileInputStream(file4);
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				ObjectInputStream objectInputStream2 = new ObjectInputStream(fileInputStream2);
				ObjectInputStream objectInputStream3 = new ObjectInputStream(fileInputStream3);
				ObjectInputStream objectInputStream4 = new ObjectInputStream(fileInputStream4);
				try {
					javaMethodsInfo = (HashMap<String, MethodInformation>) objectInputStream.readObject();
					javaMethodsMapTable = (HashMap<String, Integer>) objectInputStream2.readObject();
					sourceMethodsInfo = (HashMap<String, MethodInformation>) objectInputStream3.readObject();
					sourceMethodsMapTable = (HashMap<String, Integer>) objectInputStream4.readObject();
					objectInputStream.close();
					objectInputStream2.close();
					objectInputStream3.close();
					objectInputStream4.close();
					fileInputStream.close();
					fileInputStream2.close();
					fileInputStream3.close();
					fileInputStream4.close();
					
					PrintWriter pWriter = new PrintWriter("justTest.txt");
					Set<Map.Entry<String, Integer>> set = sourceMethodsMapTable.entrySet();
					for (Entry<String, Integer> entry : set) {
						pWriter.println(entry.getKey());
						pWriter.print(entry.getValue());
					}
					pWriter.flush();
					pWriter.close();
					
					System.out.println("The javaMethodsInfo size is :"+sourceMethodsInfo.size());
					System.out.println("The javaMethodsMapTable size is :"+sourceMethodsMapTable.size());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	public CASTVisitorCommunication(HashMap<String, String> threadMethodMapTable,
									HashMap<String, ThreadInformation> threadInfo) {
		super();
		this.threadMethodMapTable = threadMethodMapTable;
		this.threadInfo = threadInfo;
	}

	// 获取节点所在函数的KEY
	public String methodKey(ASTNode node) {	
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
		if (methodDeclaration.isConstructor()) {
			return null;
		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());   //函数名                             
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

	
//	//得到声明变量的具体位置，是成员变量-2，还是函数中的参数变量index
//	public int indexOfVarChange(ASTNode decNode) {
//		if (decNode instanceof VariableDeclarationFragment) {
//			if (decNode.getParent() instanceof FieldDeclaration) {      //成员变量
//				return -2;       //-2表示成员变量进行了更改
//			}
//		}
//		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //参数变量
//			return getParaIndex((SingleVariableDeclaration)decNode);    //第index个参数被修改了
//		}
//		else if (decNode instanceof VariableDeclarationFragment) {   //this.成员变量
//			if (decNode.getParent() instanceof FieldDeclaration) {
//				return -2;       //-2表示成员变量进行了更改
//			}
//		}
//		return -1;
//	}
	
	//将函数的KEY转换为javaMethodsInfo的KEY
	public String switchToJavaMethodKey(String key) {
		if (key==null) {
			return null;
		}
		String strKey = key.substring(0, key.indexOf('_'));
		if (javaMethodsMapTable.containsKey(strKey)) {
			return javaMethodsMapTable.get(strKey)+key.substring(key.indexOf('_'));
		}
		return null;
	}
	//将函数的KEY转换为sourceMethodsInfo的KEY
	public String switchToSrcMethodKey(String key) {
		if (key==null) {
			return null;
		}
		String strKey = key.substring(0, key.indexOf('_'));
		if (sourceMethodsMapTable.containsKey(strKey)) {
			return sourceMethodsMapTable.get(strKey)+key.substring(key.indexOf('_'));
		}
		return null;
	}
	//通过函数的KEY经过转换得到相应的MethodInformation
	public MethodInformation getMethodInformation(String key) {
		MethodInformation methodInformation = null;
		if (switchToJavaMethodKey(key)!=null) {
			//java源码函数
			methodInformation = javaMethodsInfo.get(switchToJavaMethodKey(key));
		}
		else if (switchToSrcMethodKey(key)!=null) {
			//工程函数
			methodInformation = sourceMethodsInfo.get(switchToSrcMethodKey(key));
		}
		return methodInformation;
	}
//	@Override
//	public boolean visit(MethodInvocation node) {
//		String key = getMethodKey(node);
//		
//		MethodInformation methodInformation = getMethodInformation(key);
//		if (methodInformation!=null) {
//			System.out.println("KEY:"+key);
//			System.out.println("METHODINFO:"+methodInformation);
//		}
//		
//		return super.visit(node);
//	}
	
//	@Override
//	public boolean visit(PrefixExpression node) {
//		
//		System.out.println(filePath);
//		System.err.println(compilationUnit.getLineNumber(node.getStartPosition()));
//		System.out.println(node.getOperand());
//		System.out.println("Is in method: "+castHelper.isDeclaredInCurrentMethod(node.getOperand()));
//		return super.visit(node);
//	}
	//判断是否为参数列表中的simpleName
	public boolean isParaSimpleName(SimpleName simpleName) {
		ASTNode pNode = simpleName.getParent();
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof SingleVariableDeclaration) {
				return true;
			}
			else if(pNode == simpleName.getRoot()) {
				return false;
			}
			pNode = pNode.getParent();
		}
		return false;
	}
	//对相关变量的def，use判断(use:false,def:true)
	public boolean isDefinExpression(SimpleName node,DeclarePosition declarePosition) {
		//1.参数变量但引用重置则归为Use集合
		if (declarePosition==DeclarePosition.INPARAMETER) {   
			ASTNode decNode = castHelper.getDecNode(node);
			MethodInformation decNodeMethodInfo = getMethodInformation(castHelper.getMethodKey(decNode));
			if (decNodeMethodInfo!=null) {
				int postition = castHelper.getParaIndex(node);
				if (postition!=-1&&!decNodeMethodInfo.isCheckTableOk(postition)) {
					return false;
				}
			}
		}
		for(ASTNode pNode = node;pNode!=compilationUnit;pNode=pNode.getParent()){
			//2.前缀表达式
			if (pNode instanceof PrefixExpression) {
				PrefixExpression prefixExpression = (PrefixExpression) pNode;
				String preOperator = prefixExpression.getOperator().toString();  //获取操作符
				SimpleName preOperand = (SimpleName)castHelper.getKeyVarName(prefixExpression.getOperand()); //获取操作变量名
				if (preOperand.getIdentifier().equals(node.getIdentifier())&&
				    (preOperator.equals("++")||preOperator.equals("--"))) {
					System.out.println("pre change!");
					return true;
				}
			}
			//3.后缀表达式
			else if (pNode instanceof PostfixExpression) {	
				PostfixExpression postfixExpression = (PostfixExpression) pNode;
				String postOperator = postfixExpression.getOperator().toString();
				SimpleName postOperand = (SimpleName)castHelper.getKeyVarName(postfixExpression.getOperand());
				if (postOperand.getIdentifier().equals(node.getIdentifier())&&
					(postOperator.equals("++")||postOperator.equals("--"))) {
					System.out.println("post change!");
					return true;
				}
			}
			//4.赋值表达式
			else if (pNode instanceof Assignment) {
				Assignment assignment = (Assignment) pNode;
				SimpleName leftSimpleName = (SimpleName)CASTHelper.getInstance().getKeyVarName(assignment.getLeftHandSide());
				System.out.println("Orin: "+node);
				System.out.println("left: "+leftSimpleName);
				if (leftSimpleName.getIdentifier().equals(node.getIdentifier())) {
					System.out.println("equal");
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public boolean visit(SimpleName node) {
		//确保simpleName 在线程相关的函数内(run,main及其调用的函数)
		String methodKey = castHelper.methodKey(node);
		if (!threadMethodMapTable.containsKey(methodKey)) {
			return super.visit(node);
		}
		//获取simpleName全名 a-->var.a.c
		ASTNode astNode = castHelper.getVarName(node);
		//从全名中获取核心 var.a.c 中的var
		SimpleName astNode2 = (SimpleName) castHelper.getKeyVarName(astNode);
//		System.out.println("源变量名："+node.getIdentifier());
//		System.out.println("真变量名："+astNode2.getIdentifier());
		if (!node.getIdentifier().equals(astNode2.getIdentifier())) {
			System.out.println("不相同");
			return super.visit(node);
		}
		//获取定义的位置
		DeclarePosition declarePosition = castHelper.isDeclaredInCurrentMethod(astNode2);
		/* 1.局部变量
		 * 2.java源码变量名（println）
		 * 3.参数列表中的simpleName
		 *   不作为所需变量处理
		 */
		if (declarePosition==DeclarePosition.INMETHOD||
		   (declarePosition==DeclarePosition.INPARAMETER&&isParaSimpleName(astNode2))) {
			return super.visit(node);
		}
		//判断是属于Def集还是Use集
		boolean isDefVar ;
		isDefVar = isDefinExpression(astNode2, declarePosition);
		System.out.println("Beg____________________________________________________");
		System.out.println(filePath);
		System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
		System.out.println("Type_Name :"+node.resolveTypeBinding().getQualifiedName());
		System.out.println(declarePosition);
		System.out.println("is def: "+isDefVar);
		System.out.println("simpleName:"+astNode2);
		System.out.println("End____________________________________________________");
		//信息提取<(methodKey+行号),(行号、类型、路径)>
		ThreadInformation threadInformation = threadInfo.get(threadMethodMapTable.get(methodKey));
		int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		String varType = node.resolveTypeBinding().getQualifiedName();
		ShareVarInfo shareVarInfo = new ShareVarInfo(lineNumber, varType, filePath);
		if (isDefVar) {	
			threadInformation.addDefVar(methodKey+lineNumber+node.getIdentifier(), shareVarInfo);
		}

		
		return super.visit(node);
		
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
