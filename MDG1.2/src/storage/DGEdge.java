package storage;

import java.util.Arrays;

import org.neo4j.graphdb.RelationshipType;

/**
 * 枚锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
 * 
 * @author think
 *
 */
public enum DGEdge implements RelationshipType {
	classMember, // 锟斤拷锟皆�(锟斤拷锟斤拷锟斤拷锟斤拷锟�)
	controlDepd, // 锟斤拷锟斤拷锟斤拷锟斤拷
	instantiation, // 实锟斤拷锟斤拷
	objMember, // 锟斤拷锟斤拷锟皆憋拷锟斤拷锟�
	parameter, // 锟斤拷锟斤拷
	typeAnalysis, // 锟斤拷锟酵凤拷锟斤拷
	dataDepd, // 锟斤拷锟斤拷锟斤拷锟斤拷
	inputParameter, // 锟斤拷锟斤拷锟斤拷锟斤拷
	outputParameter, // 锟斤拷锟斤拷锟斤拷锟�
	methodInvocation, // 锟斤拷锟斤拷锟斤拷锟斤拷
	methodImplment, // 锟斤拷锟襟方凤拷实锟斤拷
	methodOverwrite, // 锟斤拷锟斤拷锟斤拷锟斤拷
	interfaceImplement, // 锟接匡拷实锟斤拷
	inherit, // 锟教筹拷
	abstractMember, // 锟斤拷锟斤拷锟皆�
	pkgMember, // 锟斤拷锟斤拷员
	pkgDepd, // 锟斤拷锟斤拷锟斤拷
	threadStart, // 锟竭筹拷锟斤拷锟斤拷
	notify, // 同锟斤拷锟斤拷锟斤拷(notify)
	s_communicate, // S-通锟斤拷锟斤拷锟斤拷
	FutureGet, countDown, // (锟斤拷位锟斤拷锟�)
	theadSecurity, // 锟竭程帮拷全锟斤拷锟斤拷(锟竭筹拷使锟矫碉拷锟竭筹拷锟斤拷谋锟斤拷锟�,锟角凤拷锟斤拷锟街讹拷写)
	competenceAcquire, // 权锟睫伙拷取(acquire指锟斤拷semp)
	competenceRelease, // 权锟斤拷锟酵凤拷(semp指锟斤拷release)
	competenceDepd, // 权锟斤拷锟斤拷锟斤拷锟斤拷(锟斤拷锟斤拷锟街革拷锟絪emp)
	m_communicate, // M通锟斤拷锟斤拷锟斤拷锟斤拷
	mo_communicate, // Mo通锟斤拷锟斤拷锟斤拷锟斤拷
	connection, // connection
	c_communicate, // C通锟脚憋拷
	interClassMessage, // 锟斤拷锟斤拷锟较拷锟斤拷锟�
	anonymousClassDeclare, // 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
	CFGedge, // 锟斤拷锟斤拷锟斤拷锟斤拷
	summaryEdge,// summary锟斤拷
	dataMember,
	interrupt,
	threadRisk,
	authorityAcquire,
	authorityRelease,
	
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
	summaryEdge_reverse,
	dataMember_reverse,

	interrupt_reverse,
	threadRisk_reverse,
	authorityAcquire_reverse,
	authorityRelease_reverse;
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
		if(this.equals(dataMember))return dataMember_reverse;

		if(this.equals(interrupt))return DGEdge.interrupt_reverse;
		if(this.equals(threadRisk))return threadRisk_reverse;
		if(this.equals(authorityAcquire))return authorityAcquire_reverse;
		if(this.equals(authorityRelease))return authorityRelease_reverse;
	
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
		if(this.equals(dataMember_reverse))return dataMember;

		if(this.equals(interrupt_reverse))return interrupt;
		if(this.equals(threadRisk_reverse))return threadRisk;
		if(this.equals(authorityAcquire_reverse))return authorityAcquire;
		if(this.equals(authorityRelease_reverse))return authorityRelease;
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
					summaryEdge_reverse,
					dataMember_reverse,
					interrupt_reverse,
					threadRisk_reverse,
					authorityAcquire_reverse,
					authorityRelease_reverse
					};
		if(Arrays.asList(reverse_edges).contains(this))return true;
		return false;
	}
}
