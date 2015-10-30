package storage;

import java.util.Arrays;

import org.neo4j.graphdb.RelationshipType;

/**
 * ö������������
 * 
 * @author think
 *
 */
public enum DGEdge implements RelationshipType {
	classMember, // ���Ա(���������)
	controlDepd, // ��������
	instantiation, // ʵ����
	objMember, // �����Ա����
	parameter, // ����
	typeAnalysis, // ���ͷ���
	dataDepd, // ��������
	inputParameter, // ��������
	outputParameter, // �������
	methodInvocation, // ��������
	methodImplment, // ���󷽷�ʵ��
	methodOverwrite, // ��������
	interfaceImplement, // �ӿ�ʵ��
	inherit, // �̳�
	abstractMember, // �����Ա
	pkgMember, // ����Ա
	pkgDepd, // ������
	threadStart, // �߳�����
	notify, // ͬ������(notify)
	s_communicate, // S-ͨ������
	FutureGet, countDown, // (��λ���)
	theadSecurity, // �̰߳�ȫ����(�߳�ʹ�õ��߳���ı���,�Ƿ����ֶ�д)
	competenceAcquire, // Ȩ�޻�ȡ(acquireָ��semp)
	competenceRelease, // Ȩ���ͷ�(sempָ��release)
	competenceDepd, // Ȩ��������(�����ָ��semp)
	m_communicate, // Mͨ��������
	mo_communicate, // Moͨ��������
	connection, // connection
	c_communicate, // Cͨ�ű�
	interClassMessage, // �����Ϣ����
	anonymousClassDeclare, // ����������
	CFGedge, // ��������
	summaryEdge,// summary��
	
	classMember_reverse,
	controlDepd_reverse, 
	instantiation_reverse, 
	objMember_reverse, 
	parameter_reverse, 
	typeAnalysis_reverse, 
	dataDepd_reverse, 
	inputParameter_reverse, 
	outputParameter_reverse, 
	methodInvocation_reverse,
	methodImplment_reverse, 
	methodOverwrite_reverse,
	interfaceImplement_reverse,
	inherit_reverse, 
	abstractMember_reverse, 
	pkgMember_reverse,
	pkgDepd_reverse,
	threadStart_reverse, 
	notify_reverse, 
	s_communicate_reverse,
	FutureGet_reverse, 
	countDown_reverse, 
	theadSecurity_reverse,
	competenceAcquire_reverse, 
	competenceRelease_reverse, 
	competenceDepd_reverse, 
	m_communicate_reverse, 
	mo_communicate_reverse,
	connection_reverse, 
	c_communicate_reverse,
	interClassMessage_reverse, 
	anonymousClassDeclare_reverse,
	CFGedge_reverse, 
	summaryEdge_reverse;
	public DGEdge getReverse(){
		if(this.equals(classMember))return classMember_reverse;
		if(this.equals(controlDepd))return controlDepd_reverse;
		if(this.equals(instantiation))return instantiation_reverse;
		if(this.equals(objMember))return objMember_reverse;
		if(this.equals(parameter))return parameter_reverse;
		if(this.equals(typeAnalysis))return typeAnalysis_reverse;
		if(this.equals(dataDepd))return dataDepd_reverse;
		if(this.equals(inputParameter))return inputParameter_reverse;
		if(this.equals(outputParameter))return outputParameter_reverse;
		if(this.equals(methodInvocation))return methodInvocation_reverse;
		if(this.equals(methodImplment))return methodImplment_reverse;
		if(this.equals(methodOverwrite))return methodOverwrite_reverse;
		if(this.equals(interfaceImplement))return interfaceImplement_reverse;
		if(this.equals(inherit))return inherit_reverse;
		if(this.equals(abstractMember))return abstractMember_reverse;
		if(this.equals(pkgMember))return pkgMember_reverse;
		if(this.equals(pkgDepd))return pkgDepd_reverse;
		if(this.equals(threadStart))return threadStart_reverse;
		if(this.equals(notify))return notify_reverse;
		if(this.equals(s_communicate))return s_communicate_reverse;
		if(this.equals(FutureGet))return FutureGet_reverse;
		if(this.equals(countDown))return countDown_reverse;
		if(this.equals(theadSecurity))return theadSecurity_reverse;
		if(this.equals(competenceAcquire))return competenceAcquire_reverse;
		if(this.equals(competenceRelease))return competenceRelease_reverse;
		if(this.equals(competenceDepd))return competenceDepd_reverse;
		if(this.equals(m_communicate))return m_communicate_reverse;
		if(this.equals(mo_communicate))return mo_communicate_reverse;
		if(this.equals(connection))return connection_reverse;
		if(this.equals(c_communicate))return c_communicate_reverse;
		if(this.equals(interClassMessage))return interClassMessage_reverse;
		if(this.equals(anonymousClassDeclare))return anonymousClassDeclare_reverse;
		if(this.equals(CFGedge))return CFGedge_reverse;
		if(this.equals(summaryEdge))return summaryEdge_reverse;
	
		if(this.equals(classMember_reverse))return classMember;
		if(this.equals(controlDepd_reverse))return controlDepd;
		if(this.equals(instantiation_reverse))return instantiation;
		if(this.equals(objMember_reverse))return objMember;
		if(this.equals(parameter_reverse))return parameter;
		if(this.equals(typeAnalysis_reverse))return typeAnalysis;
		if(this.equals(dataDepd_reverse))return dataDepd;
		if(this.equals(inputParameter_reverse))return inputParameter;
		if(this.equals(outputParameter_reverse))return outputParameter;
		if(this.equals(methodInvocation_reverse))return methodInvocation;
		if(this.equals(methodImplment_reverse))return methodImplment;
		if(this.equals(methodOverwrite_reverse))return methodOverwrite;
		if(this.equals(interfaceImplement_reverse))return interfaceImplement;
		if(this.equals(inherit_reverse))return inherit;
		if(this.equals(abstractMember_reverse))return abstractMember;
		if(this.equals(pkgMember_reverse))return pkgMember;
		if(this.equals(pkgDepd_reverse))return pkgDepd;
		if(this.equals(threadStart_reverse))return threadStart;
		if(this.equals(notify_reverse))return notify;
		if(this.equals(s_communicate_reverse))return s_communicate;
		if(this.equals(FutureGet_reverse))return FutureGet;
		if(this.equals(countDown_reverse))return countDown;
		if(this.equals(theadSecurity_reverse))return theadSecurity;
		if(this.equals(competenceAcquire_reverse))return competenceAcquire;
		if(this.equals(competenceRelease_reverse))return competenceRelease;
		if(this.equals(competenceDepd_reverse))return competenceDepd;
		if(this.equals(m_communicate_reverse))return m_communicate;
		if(this.equals(mo_communicate_reverse))return mo_communicate;
		if(this.equals(connection_reverse))return connection;
		if(this.equals(c_communicate_reverse))return c_communicate;
		if(this.equals(interClassMessage_reverse))return interClassMessage;
		if(this.equals(anonymousClassDeclare_reverse))return anonymousClassDeclare;
		if(this.equals(CFGedge_reverse))return CFGedge;
		if(this.equals(summaryEdge_reverse))return summaryEdge;
		
		throw new RuntimeException("Unexpected edge type error! @ storage.DGEdge");
	}
	public boolean isReverse(){
		DGEdge[] reverse_edges = {classMember_reverse,
					controlDepd_reverse, 
					instantiation_reverse, 
					objMember_reverse, 
					parameter_reverse, 
					typeAnalysis_reverse, 
					dataDepd_reverse, 
					inputParameter_reverse, 
					outputParameter_reverse, 
					methodInvocation_reverse,
					methodImplment_reverse, 
					methodOverwrite_reverse,
					interfaceImplement_reverse,
					inherit_reverse, 
					abstractMember_reverse, 
					pkgMember_reverse,
					pkgDepd_reverse,
					threadStart_reverse, 
					notify_reverse, 
					s_communicate_reverse,
					FutureGet_reverse, 
					countDown_reverse, 
					theadSecurity_reverse,
					competenceAcquire_reverse, 
					competenceRelease_reverse, 
					competenceDepd_reverse, 
					m_communicate_reverse, 
					mo_communicate_reverse,
					connection_reverse, 
					c_communicate_reverse,
					interClassMessage_reverse, 
					anonymousClassDeclare_reverse,
					CFGedge_reverse, 
					summaryEdge_reverse};
		if(Arrays.asList(reverse_edges).contains(this))return true;
		return false;
	}
}
