package com.iseu.CASTVistitors;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.osgi.internal.container.Capabilities;
import org.eclipse.osgi.internal.loader.buddy.SystemPolicy;
import org.neo4j.cypher.internal.compiler.v2_2.helpers.StringRenderingSupport;

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadType;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTVisitorTrigger extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private HashMap<String, ThreadInformation> threadsInfoHashMap;   //线程信息
	private HashMap<String, ThreadVar> threadVarHashMap;             //线程变量哈希表
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;         //线程触发节点
	private HashMap<String, Integer> threadToIntegerMap;
	/*
	 * 用于记录线程运行中包含的函数调用
	 * KEY:methodKey <包_类_函数_参数>
	 * VALUE:线程key集   <key>
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable;
	private int threadID = 0;
	
	public CASTVisitorTrigger(HashMap<String, ThreadInformation> threadsInfoHashMap,
							  HashMap<String, ThreadVar> threadVarHashMap,
							  ArrayList<ThreadTriggerNode> threadTiggerNodes,
							  HashMap<String, HashSet<String>> threadMethodMapTable,
							  HashMap<String, Integer> threadToIntegerMap) {	
		this.threadsInfoHashMap = threadsInfoHashMap;
		this.threadVarHashMap = threadVarHashMap;
		this.threadTriggerNodes = threadTiggerNodes;
		this.threadMethodMapTable = threadMethodMapTable;
		this.threadToIntegerMap = threadToIntegerMap;
	}
	
	/**
	 * 获取线程入口的行号，并将调用该函数的线程放入对应的set中
	 * 函数：调用该函数的线程集
	 * @param node:类型声明节点
	 * @return 线程入口的行号
	 */
	private int getThreadEntrance(TypeDeclaration node) {
		for (MethodDeclaration methodDec : node.getMethods()) {
			String methodName = methodDec.getName().toString();
			if (methodName.equals("run")||methodName.equals("call")||methodName.equals("compute")) {	
				HashSet<String> set = new HashSet<>();
				set.add(node.resolveBinding().getKey());         //binaryName 改 key
				//更新线程函数信息表
				threadMethodMapTable.put(CASTHelper.getInstance().methodKey(methodDec), set);
				return compilationUnit.getLineNumber(methodDec.getName().getStartPosition());
			}
		}
		return 0;
	}
	
	/**
	 * 获取匿名线程入口行号
	 * 函数：调用该函数的线程集
	 * @param node:匿名类型声明节点
	 * @return 线程入口的行号
	 */
	private int getAnonyThreadEntrance(AnonymousClassDeclaration node) {
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof MethodDeclaration) {
				MethodDeclaration methodDec = (MethodDeclaration)object;
				String methodName = methodDec.getName().toString();
				if (methodName.equals("run")||methodName.equals("call")||methodName.equals("compute")) {
					HashSet<String> set = new HashSet<>();
					set.add(node.resolveBinding().getKey());      //binaryName 改 key
					//更新线程函数信息表
					threadMethodMapTable.put(CASTHelper.getInstance().methodKey(methodDec), set);
					return compilationUnit.getLineNumber(methodDec.getName().getStartPosition());
				}
			}
		}	
		return 0;
	}
	
	/**
	 * 判断类型绑定是否为线程相关的类型
	 * @param node ：类型绑定节点
	 * @return 判断结果
	 */
	private boolean isThreadRelate(ITypeBinding node) {
		if (node==null) {
			return false;
		}
		if (node.getKey().equals("Ljava/lang/Runnable;")) {
			return true;
		}
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			//正则表达式匹配,用于匹配RecursiveTask<T>
			String rTask ="RecursiveTask<.*>";
		    Pattern recursiveTaskPattern = Pattern.compile(rTask);
		    Matcher rMatcher = recursiveTaskPattern.matcher(supClass);
		    //正则表达式匹配,用于匹配FutureTask<T>
		    String futureTask = "FutureTask<.*>";
		    Pattern futureTaskPattern = Pattern.compile(futureTask);
		    Matcher fmMatcher = futureTaskPattern.matcher(supClass);
			if (supClass.equals("Thread")||supClass.equals("RecursiveAction")||rMatcher.find()||fmMatcher.find()) { 
				return true;
			}
			else if (isThreadRelate(node.getSuperclass())) {
				return true;
			}
		}
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			//正则表达式匹配，Callable<T>
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			//正则表达式匹配，Future<T>
			String futureMatch = "Future<.*>";
			Pattern futurePattern = Pattern.compile(futureMatch);
			//正则表达式匹配，RunnableFuture<T>
			String rFutureMatch = "RunnableFuture<.*>";
			Pattern rFuturePattern = Pattern.compile(rFutureMatch);
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			for (String interfaceName : interfaceNames) {
				Matcher cMatcher = callablePattern.matcher(interfaceName);
				Matcher fMatcher = futurePattern.matcher(interfaceName);
				Matcher rMatcher = rFuturePattern.matcher(interfaceName);
				if (interfaceName.equals("Runnable")||cMatcher.find()||fMatcher.find()||rMatcher.find()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 线程信息记录
	 * @param node       ：类型声明节点
	 * @param threadType ：线程类别
	 * @param name       ：线程名
	 */
	private void threadHandle(TypeDeclaration node,ThreadType threadType,String name) {
		if (node.resolveBinding()==null) {
			System.out.println("Thread ResolveBinding is Null");
			return;
		}
		//线程信息类
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		//(1)行号
		int lineNumber = getThreadEntrance(node);
		threadInformation.setStartLineNumber(lineNumber);
		//(2)路径
		threadInformation.setFilePath(filePath);	
		//(3)将线程包含的成员变量类型加入到线程信息结构中(变量类型集)
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType()!=null&&fieldDeclaration.getType().resolveBinding()!=null) {
					threadInformation.getVariableTypeSet().add(fieldDeclaration.getType().resolveBinding().getBinaryName());
				}
			}
		}
		//线程信息记录
		String key = node.resolveBinding().getKey().toString();
		threadsInfoHashMap.put(key, threadInformation);
		//更新线程映射表
		if (!threadToIntegerMap.containsKey(key)) {
			threadToIntegerMap.put(key, threadID++);
		}
	}
	
	/**
	 * 匿名线程信息记录
	 * @param node       ：匿名类型声明节点
	 * @param threadType ：线程类别
	 * @param name		   ：线程名
	 */
	private void anonyThreadHandle(AnonymousClassDeclaration node,ThreadType threadType,String name) {
		//线程信息类
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		//(1)行号
		int lineNumber = getAnonyThreadEntrance(node);
		threadInformation.setStartLineNumber(lineNumber);
		//(2)路径
		threadInformation.setFilePath(filePath);
		//(3)将线程包含的变量加入到线程信息结构中(变量类型记录)
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType()!=null&&fieldDeclaration.getType().resolveBinding()!=null) {
					threadInformation.getVariableTypeSet().add(fieldDeclaration.getType().resolveBinding().getBinaryName());
				}
			}
		}
		threadsInfoHashMap.put(name, threadInformation);   //匿名类的key可以用匿名类的二进制命名来替代
		if (!threadToIntegerMap.containsKey(name)) {
			threadToIntegerMap.put(name, threadID++);
		}
	}

	/**
	 * 获取线程触发节点的信息
	 * 路径，行号，及相应的线程变量key
	 * @param astNode ：线程变量节点
	 * @param node    ：函数调用节点
	 */
	private void acquireTriggerInfo(ASTNode astNode,MethodInvocation node) {
		System.out.println("NODE::"+astNode);
		//1.匿名类的调用new Thread(){}.start()或参数实例
		if(astNode instanceof ClassInstanceCreation){  
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) astNode;
			if (isThreadRelate(classInstanceCreation.getType().resolveBinding())) { 				///确保调用对象线程相关
				//(1)调用行号
				int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());  //函数调用处
				//(2)声明行号
				int decLineNumber = compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()); //匿名对象声明处
				//(3)变量类型名
				String typeName = classInstanceCreation.getType().toString();
				//(4)变量名(匿名类用key作为变量名)
				String varName = classInstanceCreation.resolveTypeBinding().getKey();     //binaryName 改 key
				ThreadTriggerNode threadTriggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTriggerNode);
			}
		}
		//2.Thread对象的函数调用astNode.start()  或者   invoke(astNode...)
		if(isThreadRelate(CASTHelper.getInstance().getResolveTypeBinding(astNode))) {      //确保调用对象线程相关	
			System.out.println("TRUE:");
			//(1)调用行号
			int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
			//变量考虑多态处理
			if (astNode instanceof SimpleName) {
				SimpleName simpleName = (SimpleName)astNode;
				ASTNode decAstNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding()); 
				if (decAstNode==null) {
					return;
				}
				//(2)声明行号
				int decLineNumber = compilationUnit.getLineNumber(decAstNode.getStartPosition());
				//(3)变量类型名
				String typeName = simpleName.resolveTypeBinding().getName();
				//(4)变量名
				String varName = simpleName.getIdentifier().toString();
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTiggerNode);
			}
			//函数返回值等，直接考虑得到的类型
			else {
				ITypeBinding iTypeBinding = CASTHelper.getInstance().getResolveTypeBinding(astNode);
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, iTypeBinding.getKey());   //binaryName 改 key
				threadTriggerNodes.add(threadTiggerNode);
			}
		}
	}
	
	/**
	 * 线程信息处理
	 * 1.普通类
	 * 处理线程声明，并将信息保存
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		//(1)获取线程的类型
		ThreadType threadType = CASTHelper.getInstance().acquireThreadType(node.resolveBinding());
		if (threadType!=null) {
			//(2)获取线程声明的名字：(key)
			String name=null;
			if (node.getName().resolveTypeBinding()!=null) {
				name = node.getName().resolveTypeBinding().getKey(); //binaryName 改 key
			}
			else {
				return super.visit(node);
			}
			//(3)线程信息处理
			threadHandle(node, threadType, name);
		}
		return super.visit(node);
	}
	
	/**
	 * 线程信息处理
	 * 2.匿名类
	 * 对匿名线程的处理，并将信息保存
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		//实例创建时node为null
		if (node==null||node.resolveBinding()==null) {
			return super.visit(node);
		}
		ITypeBinding typeBinding = node.resolveBinding();
		//(1)获取线程的类型
		ThreadType threadType = CASTHelper.getInstance().acquireThreadType(typeBinding);
		if (threadType!=null) {
			//(2)获取线程声明的名字：(key)
			String anonyName = typeBinding.getKey();   //binaryName 改 key
			//(3)线程信息处理
			anonyThreadHandle(node, threadType, anonyName);
		}
		return super.visit(node);
	}
	
	/**获取线程相关变量绑定的线程类型名
	 * 由于左部变量为线程相关，则右部放宽处理
	 * @param node : 实例创建节点
	 * @return     : 绑定线程的类型名
	 */
	public String getBindingTypeName(ClassInstanceCreation node) {
		List<?> argvs = node.arguments();
		//参数列表为空则获取类型信息
		if (argvs.isEmpty()) {
			System.out.println("Arguments is empty!");
			ITypeBinding typeBinding = node.resolveTypeBinding();
			if (typeBinding!=null) {
				return typeBinding.getKey();  //binaryName 改 key
			}
		}
		//参数列表不为空
		else {
			for(int i=0;i<argvs.size();++i){
				//参数为实例创建
				if (argvs.get(i) instanceof ClassInstanceCreation) {
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)argvs.get(i);
					if (isThreadRelate(classInstanceCreation.resolveTypeBinding())) {
						return getBindingTypeName((ClassInstanceCreation)argvs.get(i));
					}	
				}
				//普通变量名
				else {
					Object object =	argvs.get(i);
					if (object instanceof SimpleName&&isThreadRelate(((SimpleName)object).resolveTypeBinding())) {
						SimpleName simpleName = (SimpleName)object;
						IVariableBinding variableBinding = (IVariableBinding)simpleName.resolveBinding();
						ASTNode decNode = compilationUnit.findDeclaringNode(variableBinding);
						int lineNumber = compilationUnit.getLineNumber(decNode.getStartPosition());
						String varKey = filePath+"_"+lineNumber+"_"+variableBinding.getType().getName()+"_"+variableBinding.getName();
						System.out.println("VARKEY:"+varKey);
						if (threadVarHashMap.containsKey(varKey)) {
							return threadVarHashMap.get(varKey).getBindingTypeName();
						}
					}
				}				
			}	
		}
		//参数皆为线程无关的类型，则判断实例类型是否线程相关
		if (node.resolveTypeBinding()!=null) {
			return node.resolveTypeBinding().getKey();  //binaryName 改 key
		}
		return null;
	}
	
	/**
	 *(实例创建)用于记录线程变量的动态绑定类型
	 *变量存储的HashMap
	 *KEY：文件路径+行号+变量类型+变量名
	 *VAL:类型名，路径，变量名，绑定类型，线程信息key
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode root = node.getRoot();
		ASTNode pNode = node.getParent();
		if (node.resolveTypeBinding()==null||CASTHelper.getInstance().methodKey(node)==null) {
			return false;
		}
		String methodKey = CASTHelper.getInstance().methodKey(node);
		
		//1.定义创建 Thread a = new Thread();
		if (pNode instanceof VariableDeclarationFragment) {  
			VariableDeclarationFragment varDecFragment = (VariableDeclarationFragment)pNode;      //声明片段
			ITypeBinding varTypeBinding = varDecFragment.resolveBinding().getType();              //绑定节点
		    //线程相关变量
			if (isThreadRelate(varTypeBinding)) {
				//变量信息获取
			    ASTNode astNode = compilationUnit.findDeclaringNode(varDecFragment.resolveBinding()); //声明节点
			    //(1)行号
				int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());  
				//(2)类型名
				String typeName = varTypeBinding.getName(); 
				//(3)变量名
				String varName = varDecFragment.getName().toString();                      
				//(4)获取绑定的线程名key
				String bindingTypeName = getBindingTypeName(node); 
				if (bindingTypeName==null) {
					return super.visit(node);
				}
				//线程变量的创建与存储
				String varKey = filePath+"_"+lineNumber+"_"+typeName+"_"+varName;
				ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName,methodKey);
				if (threadVarHashMap.containsKey(varKey)) {
					return super.visit(node);
				}
				threadVarHashMap.put(varKey, threadVar);
			}
		}
		//2.先声明再定义创建 a = new Thread();
		else if(pNode instanceof Assignment) {
			ITypeBinding varTypeBinding = ((Assignment)pNode).resolveTypeBinding();
			if (isThreadRelate(varTypeBinding)) {
				Expression expression = ((Assignment)pNode).getLeftHandSide();
				if (expression instanceof SimpleName) {
					SimpleName simpleName = (SimpleName)expression;
					if (simpleName.resolveTypeBinding()==null) {
						return false;
					}
					ASTNode astNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());  //声明节点
					if (astNode==null) {
						return false;
					}
					//(1)变量行号
					int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());
					//(2)变量类型名
					String typeName = varTypeBinding.getName();	
					//(3)变量名
					String varName = ((Assignment)pNode).getLeftHandSide().toString();   
					//(4)获取绑定的线程名
					String bindingTypeName = getBindingTypeName(node);  
					if (bindingTypeName==null) {
						return super.visit(node);
					}
					ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName,methodKey);
					String varKey = filePath+"_"+lineNumber+"_"+typeName+"_"+varName;
					if (threadVarHashMap.containsKey(varKey)) {
						return super.visit(node);
					}
					threadVarHashMap.put(varKey, threadVar);
				}	
			}
		}
		//3.匿名创建运行new Thread(){}.start()
		else if(pNode instanceof MethodInvocation){   
			ITypeBinding iTypeBinding = node.getType().resolveBinding();
			if (isThreadRelate(iTypeBinding)) {	
				//(1)行号
				int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
				//(2)变量类型名
				String typeName = node.getType().toString();
				//(3)变量名(匿名类用key作为变量名)
				String varName = node.resolveTypeBinding().getKey();
				//(4)获取绑定的线程名
				String bindingTypeName = getBindingTypeName(node);  
				if (bindingTypeName==null) {
					System.out.println("Thread BinaryName is Null");
					return super.visit(node);
				}
				ThreadVar threadVar = new ThreadVar(typeName, filePath, varName, bindingTypeName,methodKey);
				String varKey = filePath+"_"+lineNumber+"_"+typeName+"_"+varName;
				if (threadVarHashMap.containsKey(varKey)) {
					return super.visit(node);
				}
				threadVarHashMap.put(varKey, threadVar);
			}	
		}

		return super.visit(node);
	}
	
	/**
	 * 对线程触发节点的处理
	 * TriggerNode
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().toString();
		if (methodName.equals("start")) {
			Expression ex = node.getExpression();
			acquireTriggerInfo(ex, node);     //获取触发节点的信息
		}
		else if (methodName.equals("invokeAll")||
				 methodName.equals("invokeAny")||
				 methodName.equals("execute")||
				 methodName.equals("invoke")||
				 methodName.equals("submit")) {		
			List<?> arguments = node.arguments();
			for (Object object : arguments) {
				if (object instanceof ASTNode) {
					ASTNode astNode = (ASTNode) object;
					acquireTriggerInfo(astNode, node);
				}	
			}
		}
		return super.visit(node);		
	}	
	
	/**
	 * 对主线程main的处理
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		String methodName = node.getName().toString();
		if (methodName.equals("main")) {
			ASTNode astNode;
			for(astNode  = node.getParent();astNode!=compilationUnit;astNode = astNode.getParent()){
				if (astNode instanceof TypeDeclaration) {
					TypeDeclaration typeDeclaration = (TypeDeclaration) astNode;
					//线程key
					String key = typeDeclaration.resolveBinding().getKey()+"_MAIN";
					/*
					 * 线程信息：(1)名字&(2)类型
					 */
					ThreadInformation threadInformation = new ThreadInformation(typeDeclaration.getName().toString(), ThreadType.MAIN);
					//(3)路径&(4)起始行号
					int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());
					threadInformation.setFilePath(filePath);
					threadInformation.setStartLineNumber(lineNumber);
					//线程信息表
					threadsInfoHashMap.put(key, threadInformation);
					if (!threadToIntegerMap.containsKey(key)) {
						threadToIntegerMap.put(key, threadID++);
					}
					HashSet<String> set = new HashSet<>();
					set.add(key);
					//线程调用函数表
					threadMethodMapTable.put(CASTHelper.getInstance().methodKey(node), set);
					//线程变量信息存储
					ThreadVar threadVar = new ThreadVar("Main", filePath, "main", key,CASTHelper.getInstance().methodKey(node));
					threadVarHashMap.put(filePath+"_"+lineNumber+"_"+"Main"+"_"+"main", threadVar);
				}
			}
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


