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

import com.iseu.CASTHelper.CASTHelper;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.ThreadInformation;
import com.iseu.Information.ThreadType;
import com.iseu.Information.ThreadVar;
import com.iseu.MDGHandle.Nodes.ThreadTriggerNode;

public class CASTVisitorTrigger extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private HashMap<String, ThreadInformation> threadsInfoHashMap;   //�߳���Ϣ
	private HashMap<String, ThreadVar> threadVarHashMap;             //�̱߳�����ϣ��
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;         //�̴߳����ڵ�
	private HashMap<String, Integer> threadToIntegerMap;
	/*
	 * ���ڼ�¼�߳������а����ĺ�������
	 * KEY:methodKey <��_��_����_����>
	 * VALUE:�߳�key��   <��_��> BinaryName
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
	 * ��ȡ�߳���ڵ��кţ��������øú������̷߳����Ӧ��set��
	 * ���������øú������̼߳�
	 * @param node:���������ڵ�
	 * @return �߳���ڵ��к�
	 */
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
	
	/**
	 * ��ȡ�����߳�����к�
	 * ���������øú������̼߳�
	 * @param node:�������������ڵ�
	 * @return �߳���ڵ��к�
	 */
	private int getAnonyThreadEntrance(AnonymousClassDeclaration node) {
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof MethodDeclaration) {
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
	
	/**
	 * �ж����Ͱ��Ƿ�Ϊ�߳���ص�����
	 * @param node �����Ͱ󶨽ڵ�
	 * @return �жϽ��
	 */
	private boolean isThreadRelate(ITypeBinding node) {
		if (node==null) {
			return false;
		}
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			//������ʽƥ��,����ƥ��RecursiveTask<T>
			String rTask ="RecursiveTask<.*>";
		    Pattern recursiveTaskPattern = Pattern.compile(rTask);
		    Matcher rMatcher = recursiveTaskPattern.matcher(supClass);
		    //������ʽƥ��,����ƥ��FutureTask<T>
		    String futureTask = "FutureTask<.*>";
		    Pattern futureTaskPattern = Pattern.compile(futureTask);
		    Matcher fmMatcher = futureTaskPattern.matcher(supClass);
			if (supClass.equals("Thread")||supClass.equals("RecursiveAction")||rMatcher.find()||fmMatcher.find()) { 
				return true;
			}
		}
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			//������ʽƥ�䣬Callable<T>
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			//������ʽƥ�䣬Future<T>
			String futureMatch = "Future<.*>";
			Pattern futurePattern = Pattern.compile(futureMatch);
			//������ʽƥ�䣬RunnableFuture<T>
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
	 * �߳���Ϣ��¼
	 * @param node       �����������ڵ�
	 * @param threadType ���߳����
	 * @param name       ���߳���
	 */
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
		if (node.resolveBinding().getBinaryName()==null) {
			System.out.println("Thread BinaryName is Null");
			return;
		}
		//���̰߳����ĳ�Ա�������ͼ��뵽�߳���Ϣ�ṹ��
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType().isSimpleType()) {
					SimpleType simpleType = (SimpleType) fieldDeclaration.getType();
					if (simpleType.resolveBinding()!=null) {
						System.out.println("SIMPLETYPE:"+simpleType.resolveBinding().getBinaryName());
						threadInformation.getVariableTypeSet().add(simpleType.resolveBinding().getBinaryName());
					}				
				}
			}
		}
		String key = node.resolveBinding().getBinaryName().toString();
		threadsInfoHashMap.put(key, threadInformation);
		if (!threadToIntegerMap.containsKey(key)) {
			threadToIntegerMap.put(key, threadID++);
		}
	}
	
	/**
	 * �����߳���Ϣ��¼
	 * @param node       ���������������ڵ�
	 * @param threadType ���߳����
	 * @param name		   ���߳���
	 */
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
		//���̰߳����ı������뵽�߳���Ϣ�ṹ��
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType().isSimpleType()) {
					SimpleType simpleType = (SimpleType) fieldDeclaration.getType();
					if (simpleType.resolveBinding()!=null) {
						System.out.println("SIMPLETYPE:"+simpleType.resolveBinding().getBinaryName());
						threadInformation.getVariableTypeSet().add(simpleType.resolveBinding().getBinaryName());
					}				
				}
			}
		}
		
		threadsInfoHashMap.put(name, threadInformation);   //�������key������������Ķ��������������
		if (!threadToIntegerMap.containsKey(name)) {
			threadToIntegerMap.put(name, threadID++);
		}
	}

	/**
	 * ��ȡ�̴߳����ڵ����Ϣ
	 * ·�����кţ�����Ӧ���̱߳���key
	 * @param astNode ���̱߳����ڵ�
	 * @param node    ���������ýڵ�
	 */
	private void acquireTriggerInfo(ASTNode astNode,MethodInvocation node) {

		//1.������ĵ���new Thread(){}.start()
		if(astNode instanceof ClassInstanceCreation){  
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) astNode;
			if (isThreadRelate(classInstanceCreation.getType().resolveBinding())) { 				///ȷ�����ö����߳����	
				int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());  //�������ô�
				int decLineNumber = compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()); //��������������
				String typeName = classInstanceCreation.getType().toString();
				String varName = classInstanceCreation.resolveTypeBinding().getBinaryName();
				ThreadTriggerNode threadTriggerNode = new ThreadTriggerNode(filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTriggerNode);
			}
		}
		//2.Thread����ĺ�������astNode.start()  ����   invoke(astNode...)
		if(isThreadRelate(CASTHelper.getInstance().getResolveTypeBinding(astNode))) {      //ȷ�����ö����߳����	
			int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
			//�������Ƕ�̬����
			if (astNode instanceof SimpleName) {
				SimpleName simpleName = (SimpleName)astNode;
				ASTNode decAstNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding()); 
				if (decAstNode==null) {
					return;
				}
				int decLineNumber = compilationUnit.getLineNumber(decAstNode.getStartPosition());
				String typeName = simpleName.resolveTypeBinding().getName();
				String varName = simpleName.getIdentifier().toString();
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTiggerNode);
			}
			//��������ֵ�ȣ�ֱ�ӿ��ǵõ�������
			else {
				ITypeBinding iTypeBinding = CASTHelper.getInstance().getResolveTypeBinding(astNode);
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(filePath, lineNumber, iTypeBinding.getBinaryName());
				threadTriggerNodes.add(threadTiggerNode);
			}
		}
	}
	
	/**
	 * �����߳�������������Ϣ����
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		//��ȡ�߳����������֣�(��.����)
		String name=null;
		if (node.getName().resolveTypeBinding()!=null) {
			name = node.getName().resolveTypeBinding().getBinaryName();
		}
		else {
			return super.visit(node);
		}
		
		//1.�и��࣬�����߳���صĸ������ƥ��
		if (node.getSuperclassType()!=null) {
			String supClass = node.getSuperclassType().toString();
			//������ʽƥ��,����ƥ��RecursiveTask<T>
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
		//2.�����߳���صĽӿ�
		else if(!node.superInterfaceTypes().isEmpty()){
			List<?> list = node.superInterfaceTypes();
			List<String> newList = new ArrayList<>();
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			for (Object ele : list) {
				newList.add(ele.toString());
			}
			for (String interfaceName : newList) {
				Matcher callableMacher = callablePattern.matcher(interfaceName);
				if (interfaceName.equals("Runnable")) {
					threadHandle(node, ThreadType.RUNNABLE, name);
				}
				else if (callableMacher.find()) {
					threadHandle(node, ThreadType.CALLABLE, name);
				}
			}
			return false;
		}
		return super.visit(node);
	}
	
	/**
	 * �������̵߳Ĵ���������Ϣ����
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ITypeBinding typeBinding = node.resolveBinding();
		if (typeBinding==null) {
			return false;
		}
		String number = typeBinding.getBinaryName();
		
		if (typeBinding.getSuperclass()!=null) {
			String supClass = typeBinding.getSuperclass().getName();
			//������ʽƥ��,����ƥ��RecursiveTask<T>
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
	
	/**��ȡ�߳���ر����󶨵��߳�������
	 * 
	 * @param node : ʵ�������ڵ�
	 * @return     : ���̵߳�������
	 */
	public String getBindingTypeName(ClassInstanceCreation node) {
		List<?> argvs = node.arguments();
		//�����б�Ϊ�����ȡ������Ϣ
		if (argvs.isEmpty()) {
			ITypeBinding typeBinding = node.resolveTypeBinding();
			if (typeBinding!=null&&isThreadRelate(typeBinding)) {
				return typeBinding.getBinaryName();
			}
		}
		//�����б�Ϊ��
		else {
			//����Ϊʵ������
			if (argvs.get(0) instanceof ClassInstanceCreation) {
				return getBindingTypeName((ClassInstanceCreation)argvs.get(0));
			}
			//��ͨ������
			else {
				for (Object object : argvs) {
					if (object instanceof SimpleName&&isThreadRelate(((SimpleName)object).resolveTypeBinding())) {
						SimpleName simpleName = (SimpleName)object;
						IVariableBinding variableBinding = (IVariableBinding)simpleName.resolveBinding();
						ASTNode decNode = compilationUnit.findDeclaringNode(variableBinding);
						int lineNumber = compilationUnit.getLineNumber(decNode.getStartPosition());
						String varKey = filePath+"_"+lineNumber+"_"+variableBinding.getType().getName()+"_"+variableBinding.getName();
						System.out.println("VARKEY:"+varKey);
						if (threadVarHashMap.containsKey(varKey)) {
							return threadVarHashMap.get(varKey).getThreadInfoKey();
						}
					}
				}
				//������Ϊ�߳��޹ص����ͣ����ж�ʵ�������Ƿ��߳����
				if (isThreadRelate(node.resolveTypeBinding())) {
					return node.resolveTypeBinding().getBinaryName();
				}
			}
		}
		return null;
	}
	
	/**
	 *(ʵ������)���ڼ�¼�̱߳����Ķ�̬������
	 *�����洢��HashMap
	 *KEY���ļ�·��+�к�+��������+������
	 *VAL:��������·�����������������ͣ��߳���Ϣkey
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode root = node.getRoot();
		ASTNode nextNode = node.getParent();
		if (node.resolveTypeBinding()==null||CASTHelper.getInstance().methodKey(node)==null) {
			return false;
		}
		String methodKey = CASTHelper.getInstance().methodKey(node);
		for(ASTNode pNode=nextNode;pNode!=root;nextNode = pNode,pNode=nextNode.getParent()){
			//1.���崴�� Thread a = new Thread();
			if (pNode instanceof VariableDeclarationFragment) {  
				VariableDeclarationFragment varDecFragment = (VariableDeclarationFragment)pNode;      //����Ƭ��
				ITypeBinding varTypeBinding = varDecFragment.resolveBinding().getType();              //�󶨽ڵ�
				
				//������Ϣ��ȡ
			    ASTNode astNode = compilationUnit.findDeclaringNode(varDecFragment.resolveBinding()); //�����ڵ�
				int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());           //$�кŻ�ȡ
				String varName = varDecFragment.getName().toString();            //$������
				String typeName = varTypeBinding.getName();                      //$����
				
				if (isThreadRelate(varTypeBinding)) {
					//$��ȡ�󶨵��߳���
					String bindingTypeName = getBindingTypeName(node); 
					if (bindingTypeName==null) {
						System.out.println("Thread BinaryName is Null");
						return super.visit(node);
					}
					//�̱߳����Ĵ�����洢
					ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName, bindingTypeName,methodKey);
					if (threadVarHashMap.containsKey(filePath+"_"+lineNumber+"_"+typeName+"_"+varName)) {
						return super.visit(node);
					}
					threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
				}
				break;
			}
			//2.�������ٶ��崴�� a = new Thread();
			else if(pNode instanceof Assignment) {
				ITypeBinding varTypeBinding = ((Assignment)pNode).resolveTypeBinding();
				if (isThreadRelate(varTypeBinding)) {
					//$��ȡ�󶨵��߳���
					String bindingTypeName = getBindingTypeName(node);  
					if (bindingTypeName==null) {
						System.out.println("Thread BinaryName is Null");
						return super.visit(node);
					}
					Expression expression = ((Assignment)pNode).getLeftHandSide();
					if (expression instanceof SimpleName) {
						SimpleName simpleName = (SimpleName)expression;
						if (simpleName.resolveTypeBinding()==null) {
							return false;
						}
						ASTNode astNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());  //�����ڵ�
						if (astNode==null) {
							return false;
						}
						int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());        //$������
						String varName = ((Assignment)pNode).getLeftHandSide().toString();                 //$������
						String typeName = varTypeBinding.getName();										   //$����
						ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName, bindingTypeName,methodKey);
						if (threadVarHashMap.containsKey(filePath+"_"+lineNumber+"_"+typeName+"_"+varName)) {
							return super.visit(node);
						}
						threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
					}	
				}
			}
			//3.������������new Thread(){}.start()
			else if(pNode instanceof MethodInvocation){   
				ITypeBinding iTypeBinding = node.getType().resolveBinding();
				if (isThreadRelate(iTypeBinding)) {
					//$��ȡ�󶨵��߳���
					String bindingTypeName = getBindingTypeName(node);  
					if (bindingTypeName==null) {
						System.out.println("Thread BinaryName is Null");
						return super.visit(node);
					}
					String typeName = node.getType().toString();									//$����
					String varName = node.resolveTypeBinding().getBinaryName();						//$������
					int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());		//$������
					ThreadVar threadVar = new ThreadVar(typeName, filePath, varName, bindingTypeName, bindingTypeName,methodKey);
					if (threadVarHashMap.containsKey(filePath+"_"+lineNumber+"_"+typeName+"_"+varName)) {
						return super.visit(node);
					}
					threadVarHashMap.put(filePath+"_"+lineNumber+"_"+typeName+"_"+varName, threadVar);
				}	
			}
		}
//		System.out.println("______________________________________________________________");
		return super.visit(node);
	}
	
	/**
	 * ���̴߳����ڵ�Ĵ���
	 * TriggerNode
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().toString();
		if (methodName.equals("start")) {
			Expression ex = node.getExpression();
			acquireTriggerInfo(ex, node);     //��ȡ�����ڵ����Ϣ
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
	 * �����߳�main�Ĵ���
	 */
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
					int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());
					threadInformation.setFilePath(filePath);
					threadInformation.setStartLineNumber(lineNumber);
					//�߳���Ϣ��
					threadsInfoHashMap.put(key, threadInformation);
					if (!threadToIntegerMap.containsKey(key)) {
						threadToIntegerMap.put(key, threadID++);
					}
					HashSet<String> set = new HashSet<>();
					set.add(key);
					//�̵߳��ú�����
					threadMethodMapTable.put(CASTHelper.getInstance().methodKey(node), set);
					//�̱߳�����Ϣ�洢
					ThreadVar threadVar = new ThreadVar("Main", filePath, "main", key, key,CASTHelper.getInstance().methodKey(node));
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


