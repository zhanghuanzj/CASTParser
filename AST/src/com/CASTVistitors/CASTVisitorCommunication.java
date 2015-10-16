package com.CASTVistitors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	/*
	 * 用于记录线程运行中包含的函数调用
	 * KEY:methodKey <包_类_函数_参数>
	 * VALUE:线程信息key <包_类> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable;     		//用于记录线程运行中包含的函数调用
	private HashMap<String, ThreadInformation> threadInfo;			//线程信息表，用于记录def，use
	private CASTHelper castHelper;
	private static int accessNum = 1;
	private boolean isUpdate = false;

	private HashMap<String, MethodInformation> sourceMethodsInfo;   //工程函数信息	
	private HashMap<String, MethodInformation> javaMethodsInfo; 	//java源码函数信息
	private HashMap<String, Integer> sourceMethodsMapTable;     	//工程包名映射表
	private HashMap<String, Integer> javaMethodsMapTable;       	//java包名映射表
	private Set<String> synMethodSet;

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
		
		synMethodSet = new HashSet<>();
		synMethodSet.add("wait");
		synMethodSet.add("await");
		synMethodSet.add("notify");
		synMethodSet.add("notifyAll");
		synMethodSet.add("signal");
		synMethodSet.add("signalAll");
		synMethodSet.add("lock");
		synMethodSet.add("unlock");
		synMethodSet.add("acquire");
		synMethodSet.add("release");
		synMethodSet.add("join");
		synMethodSet.add("execute");
		synMethodSet.add("invoke");
		synMethodSet.add("invokeAll");
		synMethodSet.add("invokeAny");
		synMethodSet.add("submit");
	}
	

	public CASTVisitorCommunication(HashMap<String, HashSet<String>> threadMethodMapTable,
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
	public boolean isDefinExpression(SimpleName node) {
		for(ASTNode pNode = node;pNode!=compilationUnit;pNode=pNode.getParent()){
			//1.前缀表达式
			if (pNode instanceof PrefixExpression) {
				PrefixExpression prefixExpression = (PrefixExpression) pNode;
				String preOperator = prefixExpression.getOperator().toString();  //获取操作符
				SimpleName preOperand = (SimpleName)castHelper.getKeyVarName(prefixExpression.getOperand()); //获取操作变量名
				if (preOperand!=null&&preOperand.getIdentifier().equals(node.getIdentifier())&&
				    (preOperator.equals("++")||preOperator.equals("--"))) {
					System.out.println("pre change!");
					return true;
				}
			}
			//2.后缀表达式
			else if (pNode instanceof PostfixExpression) {	
				PostfixExpression postfixExpression = (PostfixExpression) pNode;
				String postOperator = postfixExpression.getOperator().toString();
				SimpleName postOperand = (SimpleName)castHelper.getKeyVarName(postfixExpression.getOperand());
				if (postOperand!=null&&postOperand.getIdentifier().equals(node.getIdentifier())&&
					(postOperator.equals("++")||postOperator.equals("--"))) {
					System.out.println("post change!");
					return true;
				}
			}
			//3.赋值表达式
			else if (pNode instanceof Assignment) {
				Assignment assignment = (Assignment) pNode;
				SimpleName leftSimpleName = (SimpleName)CASTHelper.getInstance().getKeyVarName(assignment.getLeftHandSide());
				System.out.println("Orin: "+node);
				System.out.println("left: "+leftSimpleName);
				if (leftSimpleName!=null&&leftSimpleName.getIdentifier().equals(node.getIdentifier())) {
					System.out.println("equal");
					return true;
				}
			}
			//4.函数调用
			else if (pNode instanceof MethodInvocation) {
				System.out.println("MethodInvocation:"+node);
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				//获取调用函数信息
				MethodInformation methodInfo = getMethodInformation(methodKey);
				//函数信息不在会改变值的函数表中，则表明该函数不会改变对象或参数的值，所以将变量归为USE集，返回false
				if (methodInfo!=null) {
					int postition = castHelper.getIndexInMethodInvoke(node, pNode);
					System.out.println("Position: "+postition);
					if (postition == -2) {
						return methodInfo.isObjChange();
					}
					else if (postition == -1) {
						return false;
					}
					else {
						return methodInfo.isParameterChange(postition);
					}
				}
			}
			else if (pNode instanceof SuperMethodInvocation) {
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				//获取调用函数信息
				MethodInformation methodInfo = getMethodInformation(methodKey);
				//函数信息不在会改变值的函数表中，则表明该函数不会改变对象或参数的值，所以将变量归为USE集，返回false
				if (methodInfo!=null) {
					int postition = castHelper.getIndexInMethodInvoke(node, pNode);
					if (postition == -2) {
						return methodInfo.isObjChange();
					}
					else if (postition == -1) {
						return false;
					}
					else {
						return methodInfo.isParameterChange(postition);
					}
				}
			}
		}
		return false;
	}
	//函数调用记录处理
	public void methodRecord(SimpleName node,String threadKey) {
		ASTNode pNode = node.getParent();
		while(pNode!=compilationUnit){
			if (pNode instanceof MethodInvocation) {
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				if (methodKey==null) {
					return;
				}
				//调用函数为工程中的函数,且还未加入线程函数表
				if (!threadMethodMapTable.containsKey(methodKey)&&
					sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					HashSet<String> threadSet = new HashSet<>();
					threadSet.add(threadKey);
					threadMethodMapTable.put(methodKey, threadSet);
					isUpdate = true;
				}
				//调用函数为工程中的函数,且已经加入线程函数表
				else if (threadMethodMapTable.containsKey(methodKey)&&
						sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					//还未将与函数相关的线程加入hashset
					if (!threadMethodMapTable.get(methodKey).contains(threadKey)) {
						threadMethodMapTable.get(methodKey).add(threadKey);
						isUpdate = true;
					}
				}
			}
			else if (pNode instanceof SuperMethodInvocation) {
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				if (methodKey==null) {
					return;
				}
				//调用函数为工程中的函数,且还未加入线程函数表
				if (!threadMethodMapTable.containsKey(methodKey)&&
					sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					HashSet<String> threadSet = new HashSet<>();
					threadSet.add(threadKey);
					threadMethodMapTable.put(methodKey, threadSet);
					isUpdate = true;
				}
				//调用函数为工程中的函数,且已经加入线程函数表
				else if (threadMethodMapTable.containsKey(methodKey)&&
						sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					//还未将与函数相关的线程加入hashset
					if (!threadMethodMapTable.get(methodKey).contains(threadKey)) {
						threadMethodMapTable.get(methodKey).add(threadKey);
						isUpdate = true;
					}
				}
			}
			pNode = pNode.getParent();
		}
	}
	
    
	@Override
	public boolean visit(SimpleName node) {
		//确保simpleName 在线程相关的函数内(run,main及其调用的函数)
		String methodKey = castHelper.methodKey(node);
		if (!threadMethodMapTable.containsKey(methodKey)) {
			return super.visit(node);
		}
		HashSet<String> threadKeys = threadMethodMapTable.get(methodKey);
		for (String threadKey : threadKeys) {
			//函数调用记录
			methodRecord(node, threadKey);
			//获取simpleName全名 a-->var.a.c
			ASTNode astNode = castHelper.getVarName(node);
			//从全名中获取核心 var.a.c 中的var
			SimpleName keyVarName = (SimpleName) castHelper.getKeyVarName(astNode);

/*			System.out.println(filePath);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("源变量名："+node.getIdentifier());
			System.out.println("中间全名："+astNode);
			System.out.println("真变量名："+keyVarName);*/

			if (keyVarName==null||!node.getIdentifier().equals(keyVarName.getIdentifier())) {
				System.out.println("不相同");
				return super.visit(node);
			}
			//获取定义的位置
			DeclarePosition declarePosition = castHelper.varDeclaredPosition(keyVarName);
			/* 1.局部变量
			 * 2.java源码变量名（println）
			 * 3.参数列表中的simpleName
			 *   不作为所需变量处理
			 */
			if (node.getParent() instanceof MethodInvocation&&declarePosition!=DeclarePosition.INPARAMETER) {
				MethodInvocation methodInvocation = (MethodInvocation)node.getParent();
				if(synMethodSet.contains(methodInvocation.getName().toString())) {
					System.out.println("同步函数，不做处理！");
					return super.visit(node);
				}
			}

			if (declarePosition==DeclarePosition.INMETHOD||
			   (declarePosition==DeclarePosition.INPARAMETER&&isParaSimpleName(keyVarName))) {
				return super.visit(node);
			}
			//参数变量但引用重置则不做处理
			if (declarePosition==DeclarePosition.INPARAMETER) {   
				ASTNode decNode = castHelper.getDecNode(node);
				MethodInformation decNodeMethodInfo = getMethodInformation(castHelper.methodKey(decNode));
				if (decNodeMethodInfo!=null) {
					int postition = castHelper.getParaIndex(node);
					if (postition!=-1&&!decNodeMethodInfo.isCheckTableOk(postition)) {
						return super.visit(node);
					}
				}
			}
			//判断是属于Def集还是Use集
			boolean isDefVar ;
			System.out.println("NODE2:"+keyVarName);
			isDefVar = isDefinExpression(keyVarName);
			System.out.println("Beg____________________________________________________");
			System.out.println(filePath);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("NODE:"+node);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("Type_Name :"+((IVariableBinding)node.resolveBinding()).getType().getQualifiedName());
			System.out.println(declarePosition);
			System.out.println("is def: "+isDefVar);
			System.out.println("simpleName:"+keyVarName);
			System.out.println("End____________________________________________________");
			//信息提取<(包+类_行号_变量名),(行号、类型、路径)>
			ThreadInformation threadInformation = threadInfo.get(threadKey);
			int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
			IVariableBinding variableBinding = (IVariableBinding)node.resolveBinding();
			if (variableBinding==null) {
				System.out.println("Binding error!");
				return super.visit(node);	
			}
			String varType = variableBinding.getType().getQualifiedName();		
			ShareVarInfo shareVarInfo = new ShareVarInfo(lineNumber, varType, filePath,methodKey);
			//成员原始变量
			if (declarePosition==DeclarePosition.INMEMBERPRIMITIVE) {
				ASTNode decNode = castHelper.getDecNode(node);
				shareVarInfo.setPrimitive(true);
				shareVarInfo.setClassLineNumber(compilationUnit.getLineNumber(decNode.getStartPosition()));
				if (decNode instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment varDecFragment = (VariableDeclarationFragment)decNode;
					shareVarInfo.setBelongClass(varDecFragment.resolveBinding().getDeclaringClass().getBinaryName());
				}
			}
			if (isDefVar) {	
				threadInformation.addDefVar(threadKey+"_"+lineNumber+"_"+node.getIdentifier(), shareVarInfo);
			}
			else {
				threadInformation.addUseVar(threadKey+"_"+lineNumber+"_"+node.getIdentifier(), shareVarInfo);
			}
		}	
		return super.visit(node);	
	}

	
	
	
	
	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		isUpdate = false;
		castHelper = CASTHelper.getInstance();
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}
