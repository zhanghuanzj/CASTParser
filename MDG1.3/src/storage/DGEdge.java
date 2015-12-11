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
	classMember, // 类成员(含组合依赖)
	controlDepd, // 控制依赖
	instantiation, // 实例化
	objMember, // 对象成员变量
	parameter, // 参数
	typeAnalysis, // 类型分析
	dataDepd, // 数据依赖
	inputParameter, // 参数输入
	outputParameter, // 参数输出
	methodInvocation, // 方法调用
	methodImplment, // 抽象方法实现
	methodOverwrite, // 方法覆盖
	interfaceImplement, // 接口实现
	inherit, // 继承
	abstractMember, // 抽象成员
	pkgMember, // 包成员
	pkgDepd, // 包依赖
	threadStart, // 线程启动
	notify, // 同步依赖(notify)
	s_communicate, // S-通信依赖
	FutureGet, countDown, // (多次唤醒)
	theadSecurity, // 线程安全风险(线程使用到线程外的变量,是否区分读写)
	competenceAcquire, // 权限获取(acquire指向semp)
	competenceRelease, // 权限释放(semp指向release)
	competenceDepd, // 权限依赖边(代码段指向semp)
	m_communicate, // M通信依赖边
	mo_communicate, // Mo通信依赖边
	connection, // connection
	c_communicate, // C通信边
	interClassMessage, // 类间消息依赖
	anonymousClassDeclare, // 匿名类申明
	CFGedge, // 控制流边
	summaryEdge,// summary边
	dataMember,
	toObject,
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
	toObject_reverse,
	
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
		if(this.equals(toObject))return toObject_reverse;
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
		if(this.equals(toObject_reverse))return toObject;
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
					toObject_reverse,
					interrupt_reverse,
					threadRisk_reverse,
					authorityAcquire_reverse,
					authorityRelease_reverse
					};
		if(Arrays.asList(reverse_edges).contains(this))return true;
		return false;
	}
}
