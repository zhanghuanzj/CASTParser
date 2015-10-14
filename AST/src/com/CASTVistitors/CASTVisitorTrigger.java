package com.CASTVistitors;

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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.osgi.internal.loader.buddy.SystemPolicy;

import com.CASTHelper.CASTHelper;
import com.CASTParser.CompileUnit;
import com.Information.ThreadInformation;
import com.Information.ThreadType;
import com.Information.ThreadVar;
import com.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTVisitorTrigger extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private HashMap<String, ThreadInformation> threadsInfoHashMap;
	private HashMap<String, ThreadVar> threadVarHashMap;
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;
	/*
	 * 用于记录线程运行中包含的函数调用
	 * KEY:methodKey <包_类_函数_参数>
	 * VALUE:线程key集   <包_类> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable;
	private String filePath;
	static int instanceNumber = 1;
	static int methodNum = 1;
	public CASTVisitorTrigger(HashMap<String, ThreadInformation> threadsInfoHashMap,
							  HashMap<String, ThreadVar> threadVarHashMap,
							  ArrayList<ThreadTriggerNode> threadTiggerNodes,
							  HashMap<String, HashSet<String>> threadMethodMapTable) {	
		this.threadsInfoHashMap = threadsInfoHashMap;
		this.threadVarHashMap = threadVarHashMap;
		this.threadTriggerNodes = threadTiggerNodes;
		this.threadMethodMapTable = threadMethodMapTable;
	}
	//Acquire the Thread entrance lineNumber
	private int getThreadEntrance(TypeDeclaration node) {
		for (MethodDeclaration methodDec : node.getMethods()) {
			String methodName = methodDec.getName().toString();
			if (methodName.equals("run")||methodName.equals("call")||methodName.equals("compute")) {	
				HashSet<String> set = new HashSet<>();
				set.add(node.resolveBinding().getBinaryName());
				threadMethodMapTable.put(CASTHelper.getInstance().methodKey(methodDec), set);
				return compilationUnit.getLineNumber(methodDec.getName().getStartPosition());
			}
		}
		return 0;
	}
	//Acquire the anonyThread entrance lineNumber
	private int getAnonyThreadEntrance(AnonymousClassDeclaration node) {
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof MethodDeclaration) {
				System.out.println("Find MethodDeclaration");
				MethodDeclaration methodDec = (MethodDeclaration)object;
				String methodName = methodDec.getName().toString();
				if (methodName.equals("run")||methodName.equals("call")||methodName.equals("compute")) {
					HashSet<String> set = new HashSet<>();
					set.add(node.resolveBinding().getBinaryName());
					threadMethodMapTable.put(CASTHelper.getInstance().methodKey(methodDec), set);
					return compilationUnit.getLineNumber(methodDec.getName().getStartPosition());
				}
			}
		}	
		return 0;
	}
	//Make sure whether the class is Thread related
	private boolean isThreadRelate(ITypeBinding node) {
		if (node==null) {
			return false;
		}
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			//正则表达式匹配,用于匹配RecursiveTask<T>
			String mre ="RecursiveTask<.*>";
		    Pattern p = Pattern.compile(mre);
		    Matcher m = p.matcher(supClass);	
			if (supClass.equals("Thread")) { 
//				System.out.println("Thread");
				return true;
			}
			else if(supClass.equals("RecursiveAction")) {
//				System.out.println("RecursiveAction");
				return true;
			}
			else if(m.find()) {
//				System.out.println("RecursiveTask");
				return true;
			}
		}
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			if (interfaceNames.contains("Runnable")) {
//				System.out.println(node.getName()+" is implement Runnable");
				return true;
			}
			else if (interfaceNames.contains("Callable")) {
//				System.out.println(node.getName()+" is implement Callable");
				return true;
			}
		}
		return false;
	}
	//Collect the information about the thread related class
	private void threadHandle(TypeDeclaration node,ThreadType threadType,String name) {
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		int lineNumber = 0;
		threadInformation.setFilePath(filePath);
		switch (threadType) {
		case THREAD:
		case RUNNABLE:
			lineNumber = getThreadEntrance(node);
			break;
		case CALLABLE:
			lineNumber = getThreadEntrance(node);
			break;
		case RECURSIVEACTION:
		case RECURSIVETASK:
			lineNumber = getThreadEntrance(node);
			break;
		default:
			break;
		}
		threadInformation.setStartLineNumber(lineNumber);
		if (node.resolveBinding()==null) {
			return;
		}
		String key = node.resolveBinding().getBinaryName().toString();
		threadsInfoHashMap.put(key, threadInformation);
	}
	//Collect the information about the thread related anonymousclass
	private void anonyThreadHandle(AnonymousClassDeclaration node,ThreadType threadType,String name) {
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		int lineNumber = 0;
		threadInformation.setFilePath(filePath);
		switch (threadType) {
		case THREAD:
		case RUNNABLE:
			lineNumber = getAnonyThreadEntrance(node);
			break;
		case CALLABLE:
			lineNumber = getAnonyThreadEntrance(node);
			break;
		case RECURSIVEACTION:
		case RECURSIVETASK:
			lineNumber = getAnonyThreadEntrance(node);
			break;
		default:
			break;
		}
		threadInformation.setStartLineNumber(lineNumber);
		threadsInfoHashMap.put(name, threadInformation);   //匿名类的key可以用匿名类的二进制命名来替代
	}
	//Acquire the trigger statement information
	private void acquireTriggerInfo(ASTNode astNode,MethodInvocation node) {
		if(astNode instanceof SimpleName) {    //Thread对象的函数调用object.start()
			SimpleName simpleName = (SimpleName) astNode;
			if (isThreadRelate(simpleName.resolveTypeBinding())) { //确保调用对象线程相关		
				ASTNode decAstNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding()); 
//				System.out.println("DecLineNumber: "+compilationUnit.getLineNumber(decAstNode.getStartPosition()));
//				System.out.println("TypeName: "+simpleName.resolveTypeBinding().getName());
//				System.out.println("VarName: "+simpleName.getIdentifier());
				int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
				int decLineNumber = compilationUnit.getLineNumber(decAstNode.getStartPosition());
				String typeName = simpleName.resolveTypeBinding().getName();
				String varName = simpleName.getIdentifier().toString();
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTiggerNode);
			}
		}
		else if(astNode instanceof ClassInstanceCreation){  //匿名类的调用new Thread(){}.start()
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) astNode;
			if (isThreadRelate(classInstanceCreation.getType().resolveBinding())) { ////确保调用对象线程相关	
//				System.out.println("LineNumber.....:"+compilationUnit.getLineNumber(node.getName().getStartPosition()));
//				System.out.println("DecLineNumber....:"+compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()));
//				System.out.println("ObjTypeName....:"+classInstanceCreation.getType());
//				System.out.println("VarName....:"+classInstanceCreation.resolveTypeBinding().getBinaryName());
				int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());  //函数调用处
				int decLineNumber = compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()); //匿名对象声明处
				String typeName = classInstanceCreation.getType().toString();
				String varName = classInstanceCreation.resolveTypeBinding().getBinaryName();
				ThreadTriggerNode threadTriggerNode = new ThreadTriggerNode(filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTriggerNode);
			}
		}
	}
	//Judge the class declaration to handle Thread related class
	@Override
	public boolean visit(TypeDeclaration node) {
		System.out.println("TypeDeclaration.................................................");
		//有父类，则与线程相关的父类进行匹配
		String name = node.getName().toString();
		if (node.getSuperclassType()!=null) {
			String supClass = node.getSuperclassType().toString();
			//正则表达式匹配,用于匹配RecursiveTask<T>
			String mre ="RecursiveTask<.*>";
		    Pattern p = Pattern.compile(mre);
		    Matcher m = p.matcher(supClass);
			
			if (supClass.equals("Thread")) { 
				threadHandle(node, ThreadType.THREAD, name);
			}
			else if(supClass.equals("RecursiveAction")) {
				threadHandle(node, ThreadType.RECURSIVEACTION, name);
			}
			else if(m.find()) {
				threadHandle(node, ThreadType.RECURSIVETASK, name);
			}
		}
		//有与线程相关的接口
		else if(!node.superInterfaceTypes().isEmpty()){
			List<?> list = node.superInterfaceTypes();
			List<String> newList = new ArrayList<>();
			for (Object ele : list) {
				newList.add(ele.toString());
			}
			if (newList.contains("Runnable")) {
				threadHandle(node, ThreadType.RUNNABLE, name);
			}
			else if (newList.contains("Callable")) {
				threadHandle(node, ThreadType.CALLABLE, name);
			}
			return false;
		}
		return super.visit(node);
	}
	//Judge the anonymous class declaration to handle Thread related class
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
//		System.out.println("AnonymousClass_______________________________________________________");
		ITypeBinding typeBinding = node.resolveBinding();
		if (typeBinding==null) {
			return false;
		}
		String number = typeBinding.getBinaryName();
		
		if (typeBinding.getSuperclass()!=null) {
			String supClass = typeBinding.getSuperclass().getName();
			//正则表达式匹配,用于匹配RecursiveTask<T>
			String mre ="RecursiveTask<.*>";
		    Pattern p = Pattern.compile(mre);
		    Matcher m = p.matcher(supClass);	
			if (supClass.equals("Thread")) { 
				anonyThreadHandle(node, ThreadType.THREAD, number);
			}
			else if(supClass.equals("RecursiveAction")) {
				anonyThreadHandle(node, ThreadType.RECURSIVEACTION, number);
			}
			else if(m.find()) {
				anonyThreadHandle(node, ThreadType.RECURSIVETASK, number);
			}
		}
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			if (interfaceNames.contains("Runnable")) {
				anonyThreadHandle(node, ThreadType.RUNNABLE, number);
			}
			else if (interfaceNames.contains("Callable")) {
				anonyThreadHandle(node, ThreadType.CALLABLE, number);
			}
		}
		return super.visit(node);
	}
	/*(实例创建)用于记录线程变量的动态绑定类型
	 *变量存储的HashMap，KEY：文件路径+行号+变量类型+变量名
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
//		System.out.println("ClassInstanceCreation.................................................");
		ASTNode root = node.getRoot();
		ASTNode nextNode = node.getParent();
		if (node.resolveTypeBinding()==null) {
			return false;
		}
		String bindingTypeName = node.resolveTypeBinding().getBinaryName();

//		System.out.println(instanceNumber++);
//		System.out.println(filePath);
//		System.out.println("Binding Class: "+node.getType());
//		System.out.println("ThreadInfoKey: "+threadInfoKey);

		for(ASTNode pNode=nextNode;pNode!=root;nextNode = pNode,pNode=nextNode.getParent()){
			if (pNode instanceof VariableDeclarationFragment) {
//				System.out.println("VariableName  : "+((VariableDeclarationFragment) pNode).getName());
//				System.out.println("TypeName: "+((VariableDeclarationFragment) pNode).resolveBinding().getType().getName());
				ITypeBinding objectClassType = ((VariableDeclarationFragment) pNode).resolveBinding().getType();
				if (isThreadRelate(objectClassType)) {
					System.out.println("It's Thread Related");
				    ASTNode astNode = compilationUnit.findDeclaringNode(((VariableDeclarationFragment) pNode).resolveBinding());
					int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());
					String varName = ((VariableDeclarationFragment) pNode).getName().toString();
					String typeName = objectClassType.getName();
					ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName, bindingTypeName);
					threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
				}
				break;
			}
			else if(pNode instanceof Assignment) {
//				System.out.println("VariableName as  : "+((Assignment)pNode).getLeftHandSide());
				ITypeBinding objectClassType = ((Assignment)pNode).resolveTypeBinding();
//				System.out.println("TypeName: "+objectClassType.getName());
				if (isThreadRelate(objectClassType)) {
//					System.out.println("It's Thread Related");
					Expression expression = ((Assignment)pNode).getLeftHandSide();
					if (expression instanceof SimpleName) {
						SimpleName simpleName = (SimpleName)((Assignment)pNode).getLeftHandSide();
						if (simpleName.resolveTypeBinding()==null) {
							return false;
						}
						ASTNode astNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
						if (astNode==null) {
							return false;
						}
						int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());
//						System.out.println("DeclareNumber :"+lineNumber);
						String varName = ((Assignment)pNode).getLeftHandSide().toString();
						String typeName = objectClassType.getName();
						ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName, bindingTypeName);
						threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
					}	
				}
			}
			else if(pNode instanceof MethodInvocation){   //new Thread(){}.start()
				ITypeBinding iTypeBinding = node.getType().resolveBinding();
				if (isThreadRelate(iTypeBinding)) {
//					System.out.println("Anonymous Threa class create and invoke");
//					System.out.println("LineNumber____:"+compilationUnit.getLineNumber(node.getStartPosition()));
//					System.out.println("TypeName____:"+node.getType());
//					System.out.println("VarName____:"+node.resolveTypeBinding().getBinaryName());
					String typeName = node.getType().toString();
					String varName = node.resolveTypeBinding().getBinaryName();
					int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
					ThreadVar threadVar = new ThreadVar(typeName, filePath, varName, bindingTypeName, bindingTypeName);
					threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
				}
	
			}
		}
//		System.out.println("______________________________________________________________");
		return super.visit(node);
	}
	
	//记录触发节点信息
	@Override
	public boolean visit(MethodInvocation node) {
//		System.out.println("MethodInvocation.................................................");
		String methodName = node.getName().toString();
		if (methodName.equals("start")) {
//			System.out.println("Find start method__________________________________________");
//			System.out.println("FilePath: "+filePath);
//			System.out.println("Trigger LineNumber: "+compilationUnit.getLineNumber(node.getStartPosition()));
			Expression ex = node.getExpression();
			acquireTriggerInfo(ex, node);
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
	
	@Override
	public boolean visit(MethodDeclaration node) {
		String methodName = node.getName().toString();
		if (methodName.equals("main")) {
			ASTNode astNode;
			for(astNode  = node.getParent();astNode!=compilationUnit;astNode = astNode.getParent()){
				if (astNode instanceof TypeDeclaration) {
					TypeDeclaration typeDeclaration = (TypeDeclaration) astNode;
					String key = typeDeclaration.resolveBinding().getBinaryName()+"_MAIN";
					ThreadInformation threadInformation = new ThreadInformation(typeDeclaration.getName().toString(), ThreadType.MAIN);
					threadInformation.setFilePath(filePath);
					threadInformation.setStartLineNumber(compilationUnit.getLineNumber(node.getName().getStartPosition()));
					threadsInfoHashMap.put(key, threadInformation);
					HashSet<String> set = new HashSet<>();
					set.add(key);
					threadMethodMapTable.put(CASTHelper.getInstance().methodKey(node), set);
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


