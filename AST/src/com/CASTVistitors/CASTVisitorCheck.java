package com.CASTVistitors;


import java.util.ArrayList;
import java.util.HashMap;
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

import com.CASTParser.CompileUnit;
import com.Information.MethodInformation;
import com.MDGHandle.Nodes.NotifyType;
import com.MDGHandle.Nodes.ThreadNotifyNode;
import com.MDGHandle.Nodes.ThreadWaitNode;
import com.MDGHandle.Nodes.WaitType;

public class CASTVisitorCheck extends ASTVisitor {
	private CompilationUnit compilationUnit ;
	private String filePath;

	private HashMap<String, MethodInformation> changeMethods;

	public CASTVisitorCheck(HashMap<String, MethodInformation> changeMethods) {
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
	public String methodKey(ExpressionStatement node) {	
		//��ȡ����         < ������+�к�>   ��Ϊ���������������
		ASTNode pNode = node.getParent() ;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer) {   //���ڳ�ʼ������ֱ�ӷ���
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//����ǹ��캯����������
		if (methodDeclaration.resolveBinding().isConstructor()) {
			return null;
		}
		String methodName = methodDeclaration.getName().toString();                                   //������
		int decLine = compilationUnit.getLineNumber(methodDeclaration.getName().getStartPosition());  //�����к�
		
		//��ȡ���ڵ�   <��+��>  �����������������
		String className ="";
		ASTNode classNode = methodDeclaration.getParent();
		if (classNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
			if (typeDeclaration.resolveBinding()!=null) {
				className = typeDeclaration.resolveBinding().getQualifiedName();
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
		return className+"_"+methodName+decLine;
	}	
	//������������ö���ı�
	public void methodRegisterOfObj(String key,boolean isChange) {
		if (key == null) {       //KEYΪ��
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
	//��������������ı�
	public void methodRegisterOfParameters(String key,int index) {
		if (key == null) {     //KEYΪ��
			return ;
		}
		if (changeMethods.containsKey(key)) {                      //�Ѿ�������Ӧ�ĺ�����¼
			MethodInformation methodInformation = changeMethods.get(key);
			int parameters = methodInformation.getIsParaChange();
			int checkTable = methodInformation.getCheckTable();
			parameters = parameters|(1<<index)&checkTable;          //�ı��index �������޸����
			methodInformation.setIsParaChange(parameters);
		}
		else {                                                     //û����Ӧ�ĺ�����¼
			MethodInformation methodInformation = new MethodInformation();
			int parameters = methodInformation.getIsParaChange();
			methodInformation.setIsParaChange(parameters|(1<<index));
			changeMethods.put(key, methodInformation);
		}
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
	
	public String getMethodKey(MethodInvocation node) {
		if (node.resolveMethodBinding()!=null) {
			String methodName = node.resolveMethodBinding().getMethodDeclaration().getName();
			String className = node.resolveMethodBinding().getDeclaringClass().getQualifiedName();
			if (className.equals("")) {
				className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
			}
			ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
			for (ITypeBinding iTypeBinding : typeBinding) {
				methodName = methodName+"_"+iTypeBinding.getName();
			}
			return className+"_"+methodName;
		}
		return null;
	}
	
	public int indexOfVarChange(ASTNode decNode) {
		if (decNode instanceof VariableDeclarationFragment) {
			if (decNode.getParent() instanceof FieldDeclaration) {      //��Ա����
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		else if (decNode instanceof SingleVariableDeclaration) {        //��������
			return getParaIndex((SingleVariableDeclaration)decNode);    //��index���������޸���
		}
		else if (decNode instanceof VariableDeclarationFragment) {   //this.��Ա����
			if (decNode.getParent() instanceof FieldDeclaration) {
				return -2;       //-2��ʾ��Ա���������˸���
			}
		}
		return -1;
	}
	//-2��ʾ��Ա���������˸��ģ�-1��ʾû�и��ģ������ʾ����index����
	public int memberVar(Expression expression){
		if (expression==null) {
			return -2;               //-2��ʾ��Ա���������˸���
		}
		else if(expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)expression;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		else if(expression instanceof QualifiedName) {                      //obj.a.fun();
			QualifiedName qualifiedName = (QualifiedName) expression;
			//�ҵ���������Ǹ�����xx.x.i�е�xx
			while(qualifiedName.getQualifier() instanceof QualifiedName){
				qualifiedName = (QualifiedName)qualifiedName.getQualifier();
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return indexOfVarChange(decNode);
		}
		else if (expression instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return indexOfVarChange(decNode);
		}
		return -1;
	}
	@Override
	public boolean visit(MethodInvocation node) {
		String key = getMethodKey(node);
		int isParaChange = 0;
		boolean isObjChange = false;
//		System.out.println("CHECK");
//		System.out.println(key);
		//key ��ȡʧ��
		if (key==null) {
			return super.visit(node);
		}
		//�����ú�����ı�ֵ
		if (changeMethods.containsKey(key)) {           
			MethodInformation methodInformation = changeMethods.get(key);
			//��ı���ö����ֵ
			if (methodInformation.isObjChange()) {
				//����Ϊ��Ա����
				int result = memberVar(node.getExpression());
				System.out.println(filePath);
				System.out.println(node);
				System.out.println(result);
			}
//			a.visit();
//			System.out.println(key);
//			visit();
//			this.changeMethods.get(key);
		}
		
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


