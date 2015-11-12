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
	private HashMap<String, ThreadInformation> threadsInfoHashMap;   //�߳���Ϣ
	private HashMap<String, ThreadVar> threadVarHashMap;             //�̱߳�����ϣ��
	private ArrayList<ThreadTriggerNode> threadTriggerNodes;         //�̴߳����ڵ�
	private HashMap<String, Integer> threadToIntegerMap;
	/*
	 * ���ڼ�¼�߳������а����ĺ�������
	 * KEY:methodKey <��_��_����_����>
	 * VALUE:�߳�key��   <key>
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
				set.add(node.resolveBinding().getKey());         //binaryName �� key
				//�����̺߳�����Ϣ��
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
					set.add(node.resolveBinding().getKey());      //binaryName �� key
					//�����̺߳�����Ϣ��
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
		if (node.getKey().equals("Ljava/lang/Runnable;")) {
			return true;
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
			else if (isThreadRelate(node.getSuperclass())) {
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
		if (node.resolveBinding()==null) {
			System.out.println("Thread ResolveBinding is Null");
			return;
		}
		//�߳���Ϣ��
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		//(1)�к�
		int lineNumber = getThreadEntrance(node);
		threadInformation.setStartLineNumber(lineNumber);
		//(2)·��
		threadInformation.setFilePath(filePath);	
		//(3)���̰߳����ĳ�Ա�������ͼ��뵽�߳���Ϣ�ṹ��(�������ͼ�)
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType()!=null&&fieldDeclaration.getType().resolveBinding()!=null) {
					threadInformation.getVariableTypeSet().add(fieldDeclaration.getType().resolveBinding().getBinaryName());
				}
			}
		}
		//�߳���Ϣ��¼
		String key = node.resolveBinding().getKey().toString();
		threadsInfoHashMap.put(key, threadInformation);
		//�����߳�ӳ���
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
		//�߳���Ϣ��
		ThreadInformation threadInformation = new ThreadInformation(name, threadType);
		//(1)�к�
		int lineNumber = getAnonyThreadEntrance(node);
		threadInformation.setStartLineNumber(lineNumber);
		//(2)·��
		threadInformation.setFilePath(filePath);
		//(3)���̰߳����ı������뵽�߳���Ϣ�ṹ��(�������ͼ�¼)
		List<?> list = node.bodyDeclarations();
		for (Object object : list) {
			if (object instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration)object;
				if (fieldDeclaration.getType()!=null&&fieldDeclaration.getType().resolveBinding()!=null) {
					threadInformation.getVariableTypeSet().add(fieldDeclaration.getType().resolveBinding().getBinaryName());
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
		System.out.println("NODE::"+astNode);
		//1.������ĵ���new Thread(){}.start()�����ʵ��
		if(astNode instanceof ClassInstanceCreation){  
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) astNode;
			if (isThreadRelate(classInstanceCreation.getType().resolveBinding())) { 				///ȷ�����ö����߳����
				//(1)�����к�
				int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());  //�������ô�
				//(2)�����к�
				int decLineNumber = compilationUnit.getLineNumber(classInstanceCreation.getStartPosition()); //��������������
				//(3)����������
				String typeName = classInstanceCreation.getType().toString();
				//(4)������(��������key��Ϊ������)
				String varName = classInstanceCreation.resolveTypeBinding().getKey();     //binaryName �� key
				ThreadTriggerNode threadTriggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTriggerNode);
			}
		}
		//2.Thread����ĺ�������astNode.start()  ����   invoke(astNode...)
		if(isThreadRelate(CASTHelper.getInstance().getResolveTypeBinding(astNode))) {      //ȷ�����ö����߳����	
			System.out.println("TRUE:");
			//(1)�����к�
			int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
			//�������Ƕ�̬����
			if (astNode instanceof SimpleName) {
				SimpleName simpleName = (SimpleName)astNode;
				ASTNode decAstNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding()); 
				if (decAstNode==null) {
					return;
				}
				//(2)�����к�
				int decLineNumber = compilationUnit.getLineNumber(decAstNode.getStartPosition());
				//(3)����������
				String typeName = simpleName.resolveTypeBinding().getName();
				//(4)������
				String varName = simpleName.getIdentifier().toString();
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, filePath+"_"+decLineNumber+"_"+typeName+"_"+varName);
				threadTriggerNodes.add(threadTiggerNode);
			}
			//��������ֵ�ȣ�ֱ�ӿ��ǵõ�������
			else {
				ITypeBinding iTypeBinding = CASTHelper.getInstance().getResolveTypeBinding(astNode);
				ThreadTriggerNode threadTiggerNode = new ThreadTriggerNode(CASTHelper.getInstance().getMethodName(node),filePath, lineNumber, iTypeBinding.getKey());   //binaryName �� key
				threadTriggerNodes.add(threadTiggerNode);
			}
		}
	}
	
	/**
	 * �߳���Ϣ����
	 * 1.��ͨ��
	 * �����߳�������������Ϣ����
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		//(1)��ȡ�̵߳�����
		ThreadType threadType = CASTHelper.getInstance().acquireThreadType(node.resolveBinding());
		if (threadType!=null) {
			//(2)��ȡ�߳����������֣�(key)
			String name=null;
			if (node.getName().resolveTypeBinding()!=null) {
				name = node.getName().resolveTypeBinding().getKey(); //binaryName �� key
			}
			else {
				return super.visit(node);
			}
			//(3)�߳���Ϣ����
			threadHandle(node, threadType, name);
		}
		return super.visit(node);
	}
	
	/**
	 * �߳���Ϣ����
	 * 2.������
	 * �������̵߳Ĵ���������Ϣ����
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		//ʵ������ʱnodeΪnull
		if (node==null||node.resolveBinding()==null) {
			return super.visit(node);
		}
		ITypeBinding typeBinding = node.resolveBinding();
		//(1)��ȡ�̵߳�����
		ThreadType threadType = CASTHelper.getInstance().acquireThreadType(typeBinding);
		if (threadType!=null) {
			//(2)��ȡ�߳����������֣�(key)
			String anonyName = typeBinding.getKey();   //binaryName �� key
			//(3)�߳���Ϣ����
			anonyThreadHandle(node, threadType, anonyName);
		}
		return super.visit(node);
	}
	
	/**��ȡ�߳���ر����󶨵��߳�������
	 * �����󲿱���Ϊ�߳���أ����Ҳ��ſ���
	 * @param node : ʵ�������ڵ�
	 * @return     : ���̵߳�������
	 */
	public String getBindingTypeName(ClassInstanceCreation node) {
		List<?> argvs = node.arguments();
		//�����б�Ϊ�����ȡ������Ϣ
		if (argvs.isEmpty()) {
			System.out.println("Arguments is empty!");
			ITypeBinding typeBinding = node.resolveTypeBinding();
			if (typeBinding!=null) {
				return typeBinding.getKey();  //binaryName �� key
			}
		}
		//�����б�Ϊ��
		else {
			for(int i=0;i<argvs.size();++i){
				//����Ϊʵ������
				if (argvs.get(i) instanceof ClassInstanceCreation) {
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)argvs.get(i);
					if (isThreadRelate(classInstanceCreation.resolveTypeBinding())) {
						return getBindingTypeName((ClassInstanceCreation)argvs.get(i));
					}	
				}
				//��ͨ������
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
		//������Ϊ�߳��޹ص����ͣ����ж�ʵ�������Ƿ��߳����
		if (node.resolveTypeBinding()!=null) {
			return node.resolveTypeBinding().getKey();  //binaryName �� key
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
		ASTNode pNode = node.getParent();
		if (node.resolveTypeBinding()==null||CASTHelper.getInstance().methodKey(node)==null) {
			return false;
		}
		String methodKey = CASTHelper.getInstance().methodKey(node);
		
		//1.���崴�� Thread a = new Thread();
		if (pNode instanceof VariableDeclarationFragment) {  
			VariableDeclarationFragment varDecFragment = (VariableDeclarationFragment)pNode;      //����Ƭ��
			ITypeBinding varTypeBinding = varDecFragment.resolveBinding().getType();              //�󶨽ڵ�
		    //�߳���ر���
			if (isThreadRelate(varTypeBinding)) {
				//������Ϣ��ȡ
			    ASTNode astNode = compilationUnit.findDeclaringNode(varDecFragment.resolveBinding()); //�����ڵ�
			    //(1)�к�
				int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());  
				//(2)������
				String typeName = varTypeBinding.getName(); 
				//(3)������
				String varName = varDecFragment.getName().toString();                      
				//(4)��ȡ�󶨵��߳���key
				String bindingTypeName = getBindingTypeName(node); 
				if (bindingTypeName==null) {
					return super.visit(node);
				}
				//�̱߳����Ĵ�����洢
				String varKey = filePath+"_"+lineNumber+"_"+typeName+"_"+varName;
				ThreadVar threadVar = new ThreadVar(typeName,filePath, varName, bindingTypeName,methodKey);
				if (threadVarHashMap.containsKey(varKey)) {
					return super.visit(node);
				}
				threadVarHashMap.put(varKey, threadVar);
			}
		}
		//2.�������ٶ��崴�� a = new Thread();
		else if(pNode instanceof Assignment) {
			ITypeBinding varTypeBinding = ((Assignment)pNode).resolveTypeBinding();
			if (isThreadRelate(varTypeBinding)) {
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
					//(1)�����к�
					int lineNumber = compilationUnit.getLineNumber(astNode.getStartPosition());
					//(2)����������
					String typeName = varTypeBinding.getName();	
					//(3)������
					String varName = ((Assignment)pNode).getLeftHandSide().toString();   
					//(4)��ȡ�󶨵��߳���
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
		//3.������������new Thread(){}.start()
		else if(pNode instanceof MethodInvocation){   
			ITypeBinding iTypeBinding = node.getType().resolveBinding();
			if (isThreadRelate(iTypeBinding)) {	
				//(1)�к�
				int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
				//(2)����������
				String typeName = node.getType().toString();
				//(3)������(��������key��Ϊ������)
				String varName = node.resolveTypeBinding().getKey();
				//(4)��ȡ�󶨵��߳���
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
					//�߳�key
					String key = typeDeclaration.resolveBinding().getKey()+"_MAIN";
					/*
					 * �߳���Ϣ��(1)����&(2)����
					 */
					ThreadInformation threadInformation = new ThreadInformation(typeDeclaration.getName().toString(), ThreadType.MAIN);
					//(3)·��&(4)��ʼ�к�
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


