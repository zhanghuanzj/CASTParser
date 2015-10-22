package com.iseu.CASTVistitors;


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
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
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
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.osgi.framework.wiring.dto.BundleWiringDTO.NodeDTO;

import com.iseu.CASTParser.CompileUnit;
import com.iseu.Information.MethodInformation;
import com.iseu.MDGHandle.Nodes.NotifyType;
import com.iseu.MDGHandle.Nodes.ThreadNotifyNode;
import com.iseu.MDGHandle.Nodes.ThreadWaitNode;
import com.iseu.MDGHandle.Nodes.WaitType;

public class CASTVisitorJavaMethodsCheck extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;
	private boolean isMethodInfoChange = false;                 //���ڼ�¼�Ƿ��к�����Ϣ�޸�
	private HashMap<String, MethodInformation> changeMethods;   //�޸ĺ�����¼
	private HashMap<String, MethodInformation> javaMethodsInfo; //javaԴ�뺯���޸���Ϣ
	private HashMap<String, Integer> javaMethodsMapTable;       //javaԴ�뺯��ӳ��� 


	public CASTVisitorJavaMethodsCheck(HashMap<String, MethodInformation> changeMethods) {
		super();
		this.changeMethods = changeMethods;
	}

	//���ز������ں����Ĳ����б��е�index ��0��ʼ
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
	// KEY ��ȷ��
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
		if (methodDeclaration.resolveBinding().isConstructor()) {
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
	//������������ö���ı�
	public boolean methodRegisterOfObj(String key,boolean isChange) {
		if (key == null) {       //KEYΪ��
			return false;
		}
		if (changeMethods.containsKey(key)) {
			MethodInformation methodInformation = changeMethods.get(key);
			if (!methodInformation.isObjChange()) {
				methodInformation.setObjChange(true);
				isMethodInfoChange = true;
				System.out.println("__________________The member variable change!_____________________");
				return true;
			}
			
		}
		else {
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.setObjChange(true);
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("__________________The member variable change!_____________________");
			return true;
		}
		return false;
	}
	//��������������ı�
	public boolean methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEYΪ��
			return false;
		}
		if (changeMethods.containsKey(key)) {                      //�Ѿ�������Ӧ�ĺ�����¼
			MethodInformation methodInformation = changeMethods.get(key);
			//������index�Ų���û���޸ļ�¼����checkTableû�м�¼��������
			if (!methodInformation.isParameterChange(index)&&methodInformation.isCheckTableOk(index)) {
				methodInformation.parameterChange(index);          //�ı��index �������޸����
				isMethodInfoChange = true;
				System.out.println("_______________The parameter change , index is:"+index+"__________");
				return true;
			}         
		}
		else {                                                     //û����Ӧ�ĺ�����¼
			MethodInformation methodInformation = new MethodInformation();
			methodInformation.parameterChange(index);              //�ı��index �������޸����
			isMethodInfoChange = true;
			changeMethods.put(key, methodInformation);
			System.out.println("_______________The parameter change , index is:"+index+"__________");
			return true;
		}
		return false;
	}
	//���������������
	public void referenceReset(String key,int checkTableIndex) {
		if (key == null) {     //KEYΪ��
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //�Ѿ�������Ӧ�ĺ�����¼
			MethodInformation methodInformation = changeMethods.get(key);
			int checkTable = methodInformation.getCheckTable();
			checkTable = checkTable&(~(1<<checkTableIndex));          //�ı��index �������޸����
			methodInformation.setCheckTable(checkTable);
		}
		else {                                                     //û����Ӧ�ĺ�����¼
			MethodInformation methodInformation = new MethodInformation();
			int checkTable = methodInformation.getCheckTable();
			methodInformation.setCheckTable(checkTable&(~(1<<checkTableIndex)));
			changeMethods.put(key, methodInformation);
		}	
	}
	//��ȡ���ú�����key
	public String getMethodKey(ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_"+iTypeBinding.getName().toString().charAt(0));
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
		}
		else if (astNode instanceof SuperMethodInvocation) {
			SuperMethodInvocation node = (SuperMethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_"+iTypeBinding.getName().toString().charAt(0));
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
		}
		return null;
	}
	
	//�õ����������ľ���λ�ã��ǳ�Ա����-2�����Ǻ����еĲ�������index
	public int indexOfVarChange(ASTNode decNode) {
		if (decNode instanceof VariableDeclarationFragment) {
			if (decNode.getParent() instanceof FieldDeclaration) {      //��Ա����
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {        //��������
			return getParaIndex((SingleVariableDeclaration)decNode);    //��index���������޸���
		}
		else if (decNode instanceof VariableDeclarationFragment) {   //this.��Ա����
			if (decNode.getParent() instanceof FieldDeclaration) {
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		return -1;
	}
	//-2��ʾ��Ա���������˸��ģ�-1��ʾû�и��ģ������ʾindex�Ų������ģ��˺������ڴ�����ú����Ķ���
	public int memberVar(Expression expression){
		// 1. ����ֱ�ӵ���function��...��
		if (expression==null) {
			return -2;               //-2��ʾ��Ա���������˸���
		}
		// 2. ������ú���obj.function(...)
		else if(expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)expression;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 3. �����Ա���ú���obj.mem.function(...)
		else if(expression instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) expression;
			//�ҵ���������Ǹ�����xx.x.i�е�xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 4. this����ָ������this.obj.function(...)
		else if (expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			//this.a.b�ҵ���������Ǹ�����
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	//-2��ʾ��Ա���������˸��ģ�-1��ʾû�и��ģ������ʾindex�Ų������ģ��˺������ڴ�����ú����еĲ�����
	public int parameterVar(Object object) {
		// 1. ������ú���obj.function(...)
		if(object instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)object;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 2. �����Ա���ú���obj.mem.function(...)
		else if(object instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) object;
			//�ҵ���������Ǹ�����xx.x.i�е�xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		// 3. this����ָ������this.obj.function(...)
		else if (object instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) object;
			//this.a.b�ҵ���������Ǹ�����
			while(fieldAccess.getExpression() instanceof FieldAccess){
				fieldAccess = (FieldAccess) fieldAccess.getExpression();
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	/*
	 * �ı�����Ĵ���
	 * index ��ʾ���ú������ڵĺ����ı����ı��������-1��ʾû�иı䣬-2��ʾ��Ա�����ı䣬�����ʾ�ı������������0��1...��
	 * node ��ʾ���ú�����ASTnode�����ڻ�ȡ���ں�����KEY
	 */
	public void changeHandle(int index,ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			String key = methodKey(node);
			boolean isChange = false;
			if (index==-1||key==null) {
				return ;
			}
			else if (index==-2) {
				isChange = methodRegisterOfObj(key, true);	
			}
			else if (index>=0&&index<=31) {
				isChange = methodRegisterOfParameters(key, index);
			}
			else {
				return;
			}
			if (isChange) {
				System.out.println(node);
			}
		}
		else if (astNode instanceof SuperMethodInvocation) {
			SuperMethodInvocation node = (SuperMethodInvocation)astNode;
			String key = methodKey(node);
			boolean isChange = false;
			if (index==-1||key==null) {
//				System.out.println("Didn't change!");
				return ;
			}
			else if (index==-2) {
				isChange = methodRegisterOfObj(key, true);	
			}
			else if (index>=0&&index<=31) {
				isChange = methodRegisterOfParameters(key, index);
			}
			else {
				return;
			}
			if (isChange) {
				System.out.println(node);
			}
		}		
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String key = getMethodKey(node);
		//key ��ȡʧ��
		if (key==null) {
			return super.visit(node);
		}
		//�����ú�����ı�ֵ
		MethodInformation methodInformation;
		if (changeMethods.containsKey(key)) {             //���ڹ��̺���    
			methodInformation = changeMethods.get(key);
			//��ı���ö����ֵ
			if (methodInformation.isObjChange()) {
				//����Ϊ��Ա����
				int result = memberVar(node.getExpression());
				changeHandle(result, node);
			}
			//��ı���������ֵ
			if (methodInformation.getIsParaChange()>0) {
				int parameters = methodInformation.getIsParaChange();
				//��ȡ�����б�
				List<?> paraList = node.arguments();
				int i = 0;
				while(parameters>0){
					if ((parameters&1)==1) {   //indexΪi�Ĳ��������˸ı�
						int x = parameterVar(paraList.get(i));
						changeHandle(x, node);
					}
					i++;
					parameters = parameters>>1;
				}
			}	
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		String key = getMethodKey(node);
		//key ��ȡʧ��
		if (key==null) {
			return super.visit(node);
		}
		//�����ú�����ı�ֵ
		MethodInformation methodInformation;
		if (changeMethods.containsKey(key)) {             //���ڹ��̺���    
			methodInformation = changeMethods.get(key);
			//��ı���ö����ֵisObjChange == true;
			if (methodInformation.isObjChange()) {
				//����Ϊ��Ա����,super.fun
				changeHandle(-2, node);
			}
			//��ı���������ֵisParaChange>0;
			if (methodInformation.getIsParaChange()>0) {
				int parameters = methodInformation.getIsParaChange();
				//��ȡ�����б�
				List<?> paraList = node.arguments();
				int i = 0;
				while(parameters>0){
					if ((parameters&1)==1) {   //indexΪi�Ĳ��������˸ı�
						int x = parameterVar(paraList.get(i));
						changeHandle(x, node);
					}
					i++;
					parameters = parameters>>1;
				}
			}
		}	
		return super.visit(node);
	}
	
	
	public HashMap<String, MethodInformation> getChangeMethods() {
		return changeMethods;
	}
	
	public void setChangeMethods(HashMap<String, MethodInformation> changeMethods) {
		this.changeMethods = changeMethods;
	}
	
	public boolean isMethodInfoChange() {
		return isMethodInfoChange;
	}

	public void setMethodInfoChange(boolean isMethodInfoChange) {
		this.isMethodInfoChange = isMethodInfoChange;
	}
	
	public void traverse(ArrayList<CompileUnit> compileUnits) {
		System.out.println("Traverse...............................");
		isMethodInfoChange = false;
		for (CompileUnit compileUnit : compileUnits) {
			this.filePath = compileUnit.getFilePath();
			this.compilationUnit = compileUnit.getCompilationUnit();
			compilationUnit.accept(this);
		}
	}
}


