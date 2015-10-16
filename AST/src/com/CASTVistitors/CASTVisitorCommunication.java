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
	 * ���ڼ�¼�߳������а����ĺ�������
	 * KEY:methodKey <��_��_����_����>
	 * VALUE:�߳���Ϣkey <��_��> BinaryName
	 */
	private HashMap<String, HashSet<String>> threadMethodMapTable;     		//���ڼ�¼�߳������а����ĺ�������
	private HashMap<String, ThreadInformation> threadInfo;			//�߳���Ϣ�����ڼ�¼def��use
	private CASTHelper castHelper;
	private static int accessNum = 1;
	private boolean isUpdate = false;

	private HashMap<String, MethodInformation> sourceMethodsInfo;   //���̺�����Ϣ	
	private HashMap<String, MethodInformation> javaMethodsInfo; 	//javaԴ�뺯����Ϣ
	private HashMap<String, Integer> sourceMethodsMapTable;     	//���̰���ӳ���
	private HashMap<String, Integer> javaMethodsMapTable;       	//java����ӳ���
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

	// ��ȡ�ڵ����ں�����KEY
	public String methodKey(ASTNode node) {	
		//��ȡ����         < ������+�����б�>   ��Ϊ���������������
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==compilationUnit) {   //���ڳ�ʼ������ֱ�ӷ���
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//����ǹ��캯����������
		if (methodDeclaration.isConstructor()) {
			return null;
		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());   //������                             
		List<?> parameters  = methodDeclaration.parameters();  //�����к�
		for (Object object : parameters) {
			if (object instanceof SingleVariableDeclaration	) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)object;
				methodName.append("_"+singleVariableDeclaration.getType().toString().charAt(0));
			}
		}
		//��ȡ���ڵ�   <��+��>  �����������������
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

	
	//��������KEYת��ΪjavaMethodsInfo��KEY
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
	//��������KEYת��ΪsourceMethodsInfo��KEY
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
	//ͨ��������KEY����ת���õ���Ӧ��MethodInformation
	public MethodInformation getMethodInformation(String key) {
		MethodInformation methodInformation = null;
		if (switchToJavaMethodKey(key)!=null) {
			//javaԴ�뺯��
			methodInformation = javaMethodsInfo.get(switchToJavaMethodKey(key));
		}
		else if (switchToSrcMethodKey(key)!=null) {
			//���̺���
			methodInformation = sourceMethodsInfo.get(switchToSrcMethodKey(key));
		}
		return methodInformation;
	}

	//�ж��Ƿ�Ϊ�����б��е�simpleName
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
	//����ر�����def��use�ж�(use:false,def:true)
	public boolean isDefinExpression(SimpleName node) {
		for(ASTNode pNode = node;pNode!=compilationUnit;pNode=pNode.getParent()){
			//1.ǰ׺���ʽ
			if (pNode instanceof PrefixExpression) {
				PrefixExpression prefixExpression = (PrefixExpression) pNode;
				String preOperator = prefixExpression.getOperator().toString();  //��ȡ������
				SimpleName preOperand = (SimpleName)castHelper.getKeyVarName(prefixExpression.getOperand()); //��ȡ����������
				if (preOperand!=null&&preOperand.getIdentifier().equals(node.getIdentifier())&&
				    (preOperator.equals("++")||preOperator.equals("--"))) {
					System.out.println("pre change!");
					return true;
				}
			}
			//2.��׺���ʽ
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
			//3.��ֵ���ʽ
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
			//4.��������
			else if (pNode instanceof MethodInvocation) {
				System.out.println("MethodInvocation:"+node);
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				//��ȡ���ú�����Ϣ
				MethodInformation methodInfo = getMethodInformation(methodKey);
				//������Ϣ���ڻ�ı�ֵ�ĺ������У�������ú�������ı����������ֵ�����Խ�������ΪUSE��������false
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
				//��ȡ���ú�����Ϣ
				MethodInformation methodInfo = getMethodInformation(methodKey);
				//������Ϣ���ڻ�ı�ֵ�ĺ������У�������ú�������ı����������ֵ�����Խ�������ΪUSE��������false
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
	//�������ü�¼����
	public void methodRecord(SimpleName node,String threadKey) {
		ASTNode pNode = node.getParent();
		while(pNode!=compilationUnit){
			if (pNode instanceof MethodInvocation) {
				String methodKey = castHelper.getInvokeMethodKey(pNode);
				if (methodKey==null) {
					return;
				}
				//���ú���Ϊ�����еĺ���,�һ�δ�����̺߳�����
				if (!threadMethodMapTable.containsKey(methodKey)&&
					sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					HashSet<String> threadSet = new HashSet<>();
					threadSet.add(threadKey);
					threadMethodMapTable.put(methodKey, threadSet);
					isUpdate = true;
				}
				//���ú���Ϊ�����еĺ���,���Ѿ������̺߳�����
				else if (threadMethodMapTable.containsKey(methodKey)&&
						sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					//��δ���뺯����ص��̼߳���hashset
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
				//���ú���Ϊ�����еĺ���,�һ�δ�����̺߳�����
				if (!threadMethodMapTable.containsKey(methodKey)&&
					sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					HashSet<String> threadSet = new HashSet<>();
					threadSet.add(threadKey);
					threadMethodMapTable.put(methodKey, threadSet);
					isUpdate = true;
				}
				//���ú���Ϊ�����еĺ���,���Ѿ������̺߳�����
				else if (threadMethodMapTable.containsKey(methodKey)&&
						sourceMethodsMapTable.containsKey(methodKey.substring(0, methodKey.indexOf('_')))) {
					//��δ���뺯����ص��̼߳���hashset
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
		//ȷ��simpleName ���߳���صĺ�����(run,main������õĺ���)
		String methodKey = castHelper.methodKey(node);
		if (!threadMethodMapTable.containsKey(methodKey)) {
			return super.visit(node);
		}
		HashSet<String> threadKeys = threadMethodMapTable.get(methodKey);
		for (String threadKey : threadKeys) {
			//�������ü�¼
			methodRecord(node, threadKey);
			//��ȡsimpleNameȫ�� a-->var.a.c
			ASTNode astNode = castHelper.getVarName(node);
			//��ȫ���л�ȡ���� var.a.c �е�var
			SimpleName keyVarName = (SimpleName) castHelper.getKeyVarName(astNode);

/*			System.out.println(filePath);
			System.out.println(compilationUnit.getLineNumber(node.getStartPosition()));
			System.out.println("Դ��������"+node.getIdentifier());
			System.out.println("�м�ȫ����"+astNode);
			System.out.println("���������"+keyVarName);*/

			if (keyVarName==null||!node.getIdentifier().equals(keyVarName.getIdentifier())) {
				System.out.println("����ͬ");
				return super.visit(node);
			}
			//��ȡ�����λ��
			DeclarePosition declarePosition = castHelper.varDeclaredPosition(keyVarName);
			/* 1.�ֲ�����
			 * 2.javaԴ���������println��
			 * 3.�����б��е�simpleName
			 *   ����Ϊ�����������
			 */
			if (node.getParent() instanceof MethodInvocation&&declarePosition!=DeclarePosition.INPARAMETER) {
				MethodInvocation methodInvocation = (MethodInvocation)node.getParent();
				if(synMethodSet.contains(methodInvocation.getName().toString())) {
					System.out.println("ͬ����������������");
					return super.visit(node);
				}
			}

			if (declarePosition==DeclarePosition.INMETHOD||
			   (declarePosition==DeclarePosition.INPARAMETER&&isParaSimpleName(keyVarName))) {
				return super.visit(node);
			}
			//��������������������������
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
			//�ж�������Def������Use��
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
			//��Ϣ��ȡ<(��+��_�к�_������),(�кš����͡�·��)>
			ThreadInformation threadInformation = threadInfo.get(threadKey);
			int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
			IVariableBinding variableBinding = (IVariableBinding)node.resolveBinding();
			if (variableBinding==null) {
				System.out.println("Binding error!");
				return super.visit(node);	
			}
			String varType = variableBinding.getType().getQualifiedName();		
			ShareVarInfo shareVarInfo = new ShareVarInfo(lineNumber, varType, filePath,methodKey);
			//��Աԭʼ����
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
