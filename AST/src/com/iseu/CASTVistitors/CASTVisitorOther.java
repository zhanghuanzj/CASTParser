package com.iseu.CASTVistitors;


import java.util.HashMap;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;

import org.eclipse.jdt.core.dom.*;

import com.iseu.CASTParser.CompileUnit;


public class CASTVisitorOther extends ASTVisitor {

	CompilationUnit compilationUnit;
	HashMap<String,String> hashmap;
	boolean flag=true;

	String QTHREAD="java.lang.Thread";
	String QRUNNABLE="java.lang.Runnable";
	String QCONCURRENT="java.util.concurrent";
	String THREAD="Thread";
	String RUNNABLE="Runnable";
	String CALLABLE="Callable";
	String RUN="run";
	String CALL="call";
	String JOIN="join";
	String GET="get";
	String COMPUTE="compute";	
	String MAIN="main";
	String FUTURE="Future";
	String FUTURETASK="FutureTask";
	String RECURSIVETASK="RecursiveTask";
	String FORKJOINTASK="ForkJoinTask";
	String SYN="synchronized";
	String REENTRANTLOCK="ReentrantLock";
	String SEMAPHORE="Semaphore";
	String LOCK="lock";
	String UNLOCK="unlock";
	String ACQUIRE="acquire";
	String RELEASE="release";
	String RENLOCK="renLock";
	String SEMLOCK="semLock";
	String SYNLOCK="synLock";
	String RENRANGE="renRange";
	String SEMRANGE="semRange";
	String NEWTHREAD="new Thread";
	String MD="MethodDeclaration";
	String TD="TypeDeclaration";
	String SS="SynchronizedStatement";
	String BLOCK="Block";
	String WAIT="wait";
	String NOTIFY="notify";
	String NOTIFYALL="notifyAll";	
	String LAST="LastLineNumber";
	String PUBLIC="Public";
	String SUBMIT="submit";
	String CLASS="Class";
	String LOWCLASS="class";
	String C="<class>";
	String DOTTHIS=".this";
	String LOWTHIS="this";
	String CEND="<class>End";
	String END="End";
	String DAEMONTHREAD="<DaemonThread>";
	String VARIABLE="variable";
	String EXCEPTION="Exception";
	String ARRAYACCESS="ArrayAccess";
	String RETURNRANGE="ReturnRange";
	String TRYSTATEMENT="TryStatement";
	String IFSTATEMENT="IfStatement";


	public CASTVisitorOther(HashMap<String,String> hashmap, boolean flag) {
		super();
		this.hashmap = hashmap;
		this.flag=flag;
	}

	/*TypeDeclaration
	 * */
	@Override
	public boolean visit(TypeDeclaration node) {
		if(flag){
			try{
				boolean isNeed=true;
				/*Thread*/
				try{//TestJoin extends Thread
					if(isNeed && node.resolveBinding().getSuperclass().getName().toString().trim().equals(THREAD))
						isNeed=false;
				}
				catch(Exception e){

				}
				try{//TestJoin extends Thread; Test extends TestJoin
					if(isNeed && node.getSuperclassType().resolveBinding().getSuperclass().getName().toString().trim().equals(THREAD))
						isNeed=false;
				}
				catch(Exception e){

				}

				/*Runnable*/
				try{//TestJoin2 implements Runnable
					if(isNeed && ((node.superInterfaceTypes()).toString().trim().contains(RUNNABLE)))
						isNeed=false;
				}
				catch(Exception e){

				}

				/*Callable*/
				try{
					if(isNeed && (node.superInterfaceTypes()).toString().trim().contains(CALLABLE))
						isNeed=false;
				}
				catch(Exception e){

				}

				/*RecursiveTask*/
				try{
					if(isNeed && node.resolveBinding().getSuperclass().getName().toString().trim().contains(RECURSIVETASK))
						isNeed=false;
				}
				catch(Exception e){

				}
				if(!isNeed){//·ÅÈëÏß³Ì³ö¿ÚÐÐºÅ				
					MethodDeclaration[] method=node.getMethods();
					for(MethodDeclaration m:method){
						if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){
							boolean isReturnTypeEqual=false;
							boolean isParameterEmpty=false;				
							try{//TestForkJoin -> class TestForkJoin extends RecursiveTask<Integer> {;public Integer compute() {ÖÐµÄInteger
								ITypeBinding []iTypeBindings=node.getSuperclassType().resolveBinding().getTypeArguments();
								if(m.getReturnType2().toString().trim().equals(iTypeBindings[0].getName().toString().trim()))
									isReturnTypeEqual=true;
							}
							catch(Exception e){
								isReturnTypeEqual=true;
							}	
							if(isReturnTypeEqual && m.parameters().size() == 0){//compute()¶ø²»ÊÇcompute(size)
								isParameterEmpty=true;		
							}
							if(isParameterEmpty){
								getEnd(m,node.resolveBinding().getQualifiedName().toString().trim(),node.resolveBinding().getQualifiedName().toString().trim()+C);
								//return true;//¿ÉÄÜimplements Runnable, Callable
								return true;
							}

						}
					}
					//Ã»ÓÐ·ûºÏÌõ¼þµÄRun..,ÕâÑùÎªÊØ»¤Ïß³Ì
					hashmap.put(node.resolveBinding().getQualifiedName().toString().trim()+CEND, DAEMONTHREAD+compilationUnit.getLineNumber(node.getStartPosition()));

				}
			}
			catch(Exception e){

			}
		}
		else{
			try{//Synchronized(this) -> lock object
				if(hashmap.get(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWTHIS) != null){
					hashmap.put(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWTHIS, "synLock: ("+node.resolveBinding().getQualifiedName().toString().trim()+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}
			try{//synchronized(Configuration.class) -> lock object
				if(hashmap.get(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWCLASS) != null){
					hashmap.put(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWCLASS, "synLock: ("+node.resolveBinding().getQualifiedName().toString().trim()+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}
		}
		return true;
	}	

	/*SimpleName
	 * */
	@Override
	public 	boolean visit(SimpleName node) {
		if(!flag){
			try{
				if(node.resolveTypeBinding().getName().toString().trim().contains(EXCEPTION))//TestSinkQueue ->  catch (InterruptedException ex) {
					return true;
			}
			catch(Exception e){

			}
			String className="";
			String line="";
			line=hashmap.get(hashmap.get(CLASS)+node.toString().trim()+VARIABLE);
			if(line !=null && !line.equals("")){//""±íÊ¾TestJoin -> mainÖÐÖØÐÂ¶¨Òåj
				className=hashmap.get(CLASS);
			}
			else if(line ==null){
				try{//Server -> ÍâÀà¶¨Òåprivate ConnectionManager connectionManager;ÄÚÀàSynÖÐÊ¹ÓÃconnectionManager.stopIdleScan();
					ASTNode bNode=findIndirectType(node.getParent());
					line=hashmap.get(((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim()+node.toString().trim()+VARIABLE);
					if(line !=null){
						className=((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim();
					}
				}
				catch(Exception e){

				}
			}
			if(line !=null && !line.equals("")){
				ASTNode returnNode=isInSpeicalArea(node.getParent(), compilationUnit.getLineNumber(node.getStartPosition()));
				if(returnNode != null){
					try{
						MethodDeclaration mNode=(MethodDeclaration)returnNode;
						if(!(mNode.getName().toString().trim().equals(MAIN)) && mNode.parameters().size() > 0){//Configuration -> public synchronized void setIfUnset(String name, String value) {
							List<SingleVariableDeclaration> single=mNode.parameters();
							for(SingleVariableDeclaration s : single){
								if(s.getName().toString().trim().equals(node.toString().trim()))//Ê¹ÓÃµÄÊÇpublic synchronized void setIfUnset(String name, String value) {ÖÐµÄname»òvalue
									return true;
							}
						}
					}
					catch(Exception e){

					}					

					try{
						if(!(((MethodInvocation)node.getParent()).getName().toString().trim().equals(WAIT) || ((MethodInvocation)node.getParent()).getName().toString().trim().equals(NOTIFY) || ((MethodInvocation)node.getParent()).getName().toString().trim().equals(NOTIFYALL))){
							System.out.println("threadSafetyRisk: ("+className+")"+line+"--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						}
					}
					catch(Exception e){//²»ÊÇMethodInvocationÒý·¢µÄÒì³££¬´ËÊ±nodeÒ²Âú×ãÒªÇó
						System.out.println("threadSafetyRisk: ("+className+")"+line+"--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
					}
				}
			}
		}
		return true;
	}

	/*VariableDeclarationFragment
	 * */
	@Override
	public 	boolean visit(VariableDeclarationFragment node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){
			/*±äÁ¿¶¨Òå·ÖÎö*/
			try{
				String qualifiedName="";
				String superQualifiedName="";
				String interfaceQualifiedName="";
				String name="";
				ITypeBinding []iTypeBindings=null;

				try{//get the direct qualifiedName
					qualifiedName=((FieldDeclaration)node.getParent()).getType().resolveBinding().getQualifiedName().toString().trim();
				}
				catch(Exception e){

				}
				try{
					qualifiedName=((VariableDeclarationStatement)node.getParent()).getType().resolveBinding().getQualifiedName().toString().trim();
				}
				catch(Exception e){

				}
				try{//get the qualifiedName of superClass
					superQualifiedName=((FieldDeclaration)node.getParent()).getType().resolveBinding().getSuperclass().getQualifiedName().toString().trim();
				}
				catch(Exception e){

				}
				try{
					superQualifiedName=((VariableDeclarationStatement)node.getParent()).getType().resolveBinding().getSuperclass().getQualifiedName().toString().trim();
				}
				catch(Exception e){

				}
				try{//get the qualifiedName of interface
					iTypeBindings=((FieldDeclaration)node.getParent()).getType().resolveBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						interfaceQualifiedName+=i.getQualifiedName().toString().trim();
					}
				}
				catch(Exception e){

				}
				try{
					iTypeBindings=((VariableDeclarationStatement)node.getParent()).getType().resolveBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						interfaceQualifiedName+=i.getQualifiedName().toString().trim();
					}
				}
				catch(Exception e){

				}
				name=qualifiedName+","+superQualifiedName+","+interfaceQualifiedName;
				if(!(name.contains(QTHREAD) || name.contains(QRUNNABLE) || name.contains(QCONCURRENT))){
					if(isInSpeicalArea(node.getParent(), compilationUnit.getLineNumber(node.getStartPosition())) == null){
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim()+VARIABLE,compilationUnit.getLineNumber(node.getStartPosition())+"");					
					}
					else//ÔÚÀïÃæÖØÐÂ¶¨Òå
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim()+VARIABLE,"");
				}
			}
			catch(Exception e){

			}

			/*new Thread(*)*/
			try{
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					ClassInstanceCreation c=(ClassInstanceCreation)node.getInitializer();
					if(c.arguments().size()>0){
						try{//Thread thread2 = new Thread(new FutureTask<Integer>(task));
							ClassInstanceCreation cs=(ClassInstanceCreation)c.arguments().get(0);
							if(cs.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){
								hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+cs.arguments().get(0).toString().trim()));	
								return true;
							}
						}
						catch(Exception e){

						}

						SimpleName s=(SimpleName)c.arguments().get(0);				
						try{//(1)Thread thread7=new Thread(thread2);(2)Thread thread = new Thread(futureTask);	
							if(s.resolveTypeBinding().getName().toString().trim().equals(RUNNABLE) || s.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){
								hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+s.toString().trim()));	
								return true;
							}
						}
						catch(Exception e){

						}
						
						try{//Thread thread8=new Thread(thread);	
							if(!s.resolveTypeBinding().getName().toString().trim().equals(THREAD)){
								ITypeBinding []iTypeBindings=s.resolveTypeBinding().getInterfaces();
								for(ITypeBinding i:iTypeBindings){
									if(i.getName().toString().trim().contains(RUNNABLE)){	
										hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), s.resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));									
										return true;
									}
								}
							}		
						}
						catch(Exception e){

						}
					}
				}
			}
			catch(Exception e){

			}

			/*Thread*/
			try{//(1)TestJoin thread = new TestJoin(); (2)Thread thread2=thread;
				if(node.getInitializer().resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD) || node.getInitializer().resolveTypeBinding().getSuperclass().getSuperclass().getName().toString().trim().equals(THREAD)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
					return true;
				}
			}
			catch(Exception e){

			}

			try{//Thread thread5=thread2;
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getInitializer().toString().trim()));	
					return true;
				}
			}
			catch(Exception e){

			}

			/*Runnable*/
			/*TestJoin thread = new TestJoin(); 
			Thread thread2=thread;
			Thread thread5=thread2;
			Name=Thread;Interfaces:Runnable*/
			try{//(1) TestJoin2 thread = new TestJoin2(); (2) Runnable thread2=thread;		
				if(!node.getInitializer().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					ITypeBinding []iTypeBindings=node.getInitializer().resolveTypeBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						if(i.getName().toString().trim().contains(RUNNABLE)){	
							hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
							return true;
						}
					}
				}		
			}
			catch(Exception e){

			}

			try{//Runnable thread5=thread2;	
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().equals(RUNNABLE)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getInitializer().toString().trim()));	
					return true;
				}
			}
			catch(Exception e){

			}

			/*Callable*/
			try{//(1) Task task = new Task(); (2) Callable task2=task;
				ITypeBinding []iTypeBindings=node.getInitializer().resolveTypeBinding().getInterfaces();
				for(ITypeBinding i:iTypeBindings){
					if(i.getName().toString().trim().contains(CALLABLE)){	
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
						return true;
					}
				}				
			}
			catch(Exception e){

			}

			try{//Callable task5=task2;
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().contains(CALLABLE)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getInitializer().toString().trim()));	
					return true;
				}
			}
			catch(Exception e){

			}

			/*RecursiveTask*/
			try{//TestForkJoin extends RecursiveTask<Integer>(1) TestForkJoin fjt=new TestForkJoin(40); (2) ForkJoinTask<Integer> fjt2 =fjt ; (3) RecursiveTask<Integer> fjt3=fjt;
				if(node.getInitializer().resolveTypeBinding().getSuperclass().getName().toString().trim().contains(RECURSIVETASK)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
					return true;
				}
			}
			catch(Exception e){

			}

			try{//ForkJoinTask<Integer> fjt4 =fjt2 ;
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().contains(FORKJOINTASK) || node.getInitializer().resolveTypeBinding().getName().toString().trim().contains(RECURSIVETASK)){
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getInitializer().toString().trim()));	
					return true;
				}
			}
			catch(Exception e){

			}

			/*get lock object only in VariableDeclarationStatement*/
			try{
				if(hashmap.get(RENLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//¼ÓrenLockµÄÅÐ¶ÏÊÇ·ñÎª¿Õ
					hashmap.put(RENLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), "renLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
					return true;
				}
			}
			catch(Exception e){

			}

			try{
				if(hashmap.get(SEMLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){
					hashmap.put(SEMLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), "semLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));		
					return true;
				}
			}
			catch(Exception e){

			}

			try{
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){
					hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), "synLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}
		}
		return true;
	}

	/*ClassInstanceCreation
	 * */
	@Override
	public 	boolean visit(ClassInstanceCreation node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){			
			try{
				if(node.getType().toString().trim().equals(RUNNABLE) && node.getAnonymousClassDeclaration() !=null){
					List<BodyDeclaration> bodyList=node.getAnonymousClassDeclaration().bodyDeclarations();
					MethodDeclaration m=null;
					boolean isRun=false;	
					for(int i=0;i<bodyList.size();i++){
						if(bodyList.get(i).getClass().toString().trim().contains(MD)){
							m=(MethodDeclaration)bodyList.get(i);
							if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){	
								isRun=true;
								break;
							}										
						}
					}
					boolean isParameterEmpty=false;				
					if(m.parameters().size() == 0){
						isParameterEmpty=true;		
					}
					if(isParameterEmpty && isRun){
						String name="";
						try{//Runnable reader = new Runnable() {
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}

						try{
							name=hashmap.get(CLASS)+((ArrayAccess)((Assignment)node.getParent()).getLeftHandSide()).getArray().toString().trim();	
						}
						catch(Exception e){

						}

						try{
							name=hashmap.get(CLASS)+((MethodInvocation)node.getParent()).getExpression().toString().trim();	
						}
						catch(Exception e){

						}

						try{//Client -> Future<?> senderFuture = sendParamsExecutor.submit(new Runnable() {
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)((MethodInvocation)node.getParent()).getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}
						if(!name.equals(""))
							getEnd(m, hashmap.get(CLASS),name);

						String endLineNumber=hashmap.get(hashmap.get(CLASS)+END);
						hashmap.put(name, endLineNumber);
						return true;
					}
				}
			}
			catch(Exception e){

			}

			try{//new Thread() {
				if(node.getType().toString().trim().equals(THREAD) && node.getAnonymousClassDeclaration() !=null){
					List<BodyDeclaration> bodyList=node.getAnonymousClassDeclaration().bodyDeclarations();
					MethodDeclaration m=null;
					boolean isRun=false;	
					for(int i=0;i<bodyList.size();i++){
						if(bodyList.get(i).getClass().toString().trim().contains(MD)){
							m=(MethodDeclaration)bodyList.get(i);
							if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){	
								isRun=true;
								break;
							}										
						}
					}
					boolean isParameterEmpty=false;				
					if(m.parameters().size() == 0){
						isParameterEmpty=true;		
					}
					if(isParameterEmpty && isRun){
						String name="";													
						try{//TestNativeIO ->  Thread statter = new Thread() {
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}

						try{//TestIPC -> connectors[i] = new Thread() {
							name=hashmap.get(CLASS)+((ArrayAccess)((Assignment)node.getParent()).getLeftHandSide()).getArray().toString().trim();	
						}
						catch(Exception e){

						}

						try{//TestGroupsCaching ->  threads.add(new Thread() {
							name=hashmap.get(CLASS)+((MethodInvocation)node.getParent()).getExpression().toString().trim();	
						}
						catch(Exception e){

						}
						if(!name.equals(""))
							getEnd(m, hashmap.get(CLASS),name);

						String endLineNumber=hashmap.get(hashmap.get(CLASS)+END);
						hashmap.put(name, endLineNumber);
						return true;
					}
				}
			}
			catch(Exception e){

			}

			try{
				if(node.getType().toString().trim().equals(THREAD) && node.arguments().size()>0){
					ClassInstanceCreation c=(ClassInstanceCreation)node.arguments().get(0);				
					List<BodyDeclaration> bodyList=c.getAnonymousClassDeclaration().bodyDeclarations();					
					MethodDeclaration m=null;
					boolean isRun=false;
					for(int i=0;i<bodyList.size();i++){
						if(bodyList.get(i).getClass().toString().trim().contains(MD)){
							m=(MethodDeclaration)bodyList.get(i);
							if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){	
								isRun=true;
								break;
							}										
						}
					}

					boolean isParameterEmpty=false;				
					if(m.parameters().size() == 0){
						isParameterEmpty=true;		
					}

					if(isParameterEmpty && isRun){
						String name="";
						try{//TestRPC ->  Thread rpcThread = new Thread(new Runnable() {
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}

						try{//TestIPC -> threads[i] = new Thread(new Runnable() {   
							//name=threads
							name=hashmap.get(CLASS)+((ArrayAccess)((Assignment)node.getParent()).getLeftHandSide()).getArray().toString().trim();	
						}
						catch(Exception e){

						}
						if(!name.equals(""))
							getEnd(m, hashmap.get(CLASS),name);
						String endLineNumber=hashmap.get(hashmap.get(CLASS)+END);
						hashmap.put(name, endLineNumber);						
						return true;
					}
				}
			}
			catch(Exception e){

			}

			try{
				if(node.getType().toString().trim().contains(CALLABLE) && node.getAnonymousClassDeclaration() !=null){
					List<BodyDeclaration> bodyList=node.getAnonymousClassDeclaration().bodyDeclarations();
					MethodDeclaration m=null;
					boolean isRun=false;	
					for(int i=0;i<bodyList.size();i++){
						if(bodyList.get(i).getClass().toString().trim().contains(MD)){
							m=(MethodDeclaration)bodyList.get(i);
							if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){	
								isRun=true;
								break;
							}										
						}
					}
					boolean isParameterEmpty=false;				
					if(m.parameters().size() == 0){
						isParameterEmpty=true;		
					}
					if(isParameterEmpty && isRun){
						String name="";
						try{//TestDomainSocket -> Callable<Void> clientCallable = new Callable<Void>() {
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}

						try{
							name=hashmap.get(CLASS)+((ArrayAccess)((Assignment)node.getParent()).getLeftHandSide()).getArray().toString().trim();	
						}
						catch(Exception e){

						}

						try{
							name=hashmap.get(CLASS)+((MethodInvocation)node.getParent()).getExpression().toString().trim();	
						}
						catch(Exception e){

						}

						try{//TestRPC -> res.add(executorService.submit( new Callable<Void>() {				           
							name=hashmap.get(CLASS)+((MethodInvocation)((MethodInvocation)node.getParent()).getParent()).getExpression().toString().trim();	
						}
						catch(Exception e){

						}

						try{//TestRetryProxy -> Future<Throwable> future = exec.submit(new Callable<Throwable>(){
							name=hashmap.get(CLASS)+((VariableDeclarationFragment)((MethodInvocation)node.getParent()).getParent()).getName().toString().trim();	
						}
						catch(Exception e){

						}
						if(!name.equals(""))
							getEnd(m, hashmap.get(CLASS),name);

						String endLineNumber=hashmap.get(hashmap.get(CLASS)+END);
						hashmap.put(name, endLineNumber);
						return true;
					}
				}
			}
			catch(Exception e){

			}

			try{//futureTask = new FutureTask<Integer>(task);
				if(node.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){		
					try{//define
						hashmap.put(hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
						return true;
					}
					catch(Exception e){

					}

					try{//this.*=
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)((Assignment)node.getParent()).getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
						return true;
					}
					catch(Exception e){

					}

					try{//*=
						hashmap.put(hashmap.get(CLASS)+((SimpleName)((Assignment)node.getParent()).getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
					}
					catch(Exception e){

					}					
					return true;
				}
			}
			catch(Exception e){

			}		
		}
		return true;
	}

	/*MethodInvocation
	 * */
	@Override
	public 	boolean visit(MethodInvocation node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(flag){
			try{//release(name)µÈ»áÒý·¢Òì³£
				String renRange="";
				String semRange="";
				if(node.getName().toString().trim().equals(LOCK) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){			
					hashmap.put(RENLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim(),"");
					try{//RetryCache -> this.lock.lock();
						hashmap.put(RENLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim(),"");
					}
					catch(Exception e){

					}
					try{
						hashmap.put(RENLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim(),"");
					}
					catch(Exception e){

					}
					renRange=hashmap.get(hashmap.get(CLASS)+RENRANGE);
					renRange=(renRange==null?"":renRange)+compilationUnit.getLineNumber(node.getStartPosition())+",";
				}
				else if(node.getName().toString().trim().equals(UNLOCK) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){
					renRange=hashmap.get(hashmap.get(CLASS)+RENRANGE);
					renRange=(renRange==null?"":renRange)+compilationUnit.getLineNumber(node.getStartPosition())+";";
				}
				else if(node.getName().toString().trim().equals(ACQUIRE) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
					hashmap.put(SEMLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim(),"");
					try{
						hashmap.put(SEMLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim(),"");
					}
					catch(Exception e){

					}
					try{//TestFileSystemCaching -> InitializeForeverFileSystem.sem.acquire();
						hashmap.put(SEMLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim(),"");
					}
					catch(Exception e){

					}
					semRange=hashmap.get(hashmap.get(CLASS)+SEMRANGE);
					semRange=(semRange==null?"":semRange)+compilationUnit.getLineNumber(node.getStartPosition())+",";
				}
				else if(node.getName().toString().trim().equals(RELEASE) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
					semRange=hashmap.get(hashmap.get(CLASS)+SEMRANGE);
					semRange=(semRange==null?"":semRange)+compilationUnit.getLineNumber(node.getStartPosition())+";";
				}
				if(!renRange.equals(""))
					hashmap.put(hashmap.get(CLASS)+RENRANGE, renRange);
				if(!semRange.equals(""))
					hashmap.put(hashmap.get(CLASS)+SEMRANGE, semRange);
			}
			catch(Exception e){

			}
		}
		else{	
			try{//Shell -> Thread errThread = new Thread() {;joinThread(errThread);
				List<SimpleName> arguments=node.arguments();
				for(SimpleName s : arguments){
					if(s.resolveTypeBinding().getName().toString().trim().equals(THREAD))
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+s.toString().trim()));
				}				
			}
			catch(Exception e){

			}
			try{//TestDomainSocket -> Callable<Void> clientCallable = new Callable<Void>(); Future<Void> clientFuture = exeServ.submit(clientCallable);
				if(node.getName().toString().trim().equals(SUBMIT) && ((SimpleName)node.arguments().get(0)).resolveTypeBinding().getName().toString().trim().contains(CALLABLE)){		
					try{//define
						hashmap.put(hashmap.get(CLASS)+((VariableDeclarationFragment)node.getParent()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
						return true;
					}
					catch(Exception e){

					}

					try{//this.*=
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)((Assignment)node.getParent()).getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
						return true;
					}
					catch(Exception e){

					}

					try{//*=
						hashmap.put(hashmap.get(CLASS)+((SimpleName)((Assignment)node.getParent()).getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.arguments().get(0).toString().trim()));
					}
					catch(Exception e){

					}					
					return true;
				}
			}
			catch(Exception e){

			}	

			try{	
				boolean isYes=false;
				SimpleName s=(SimpleName)node.arguments().get(0);
				if(!isYes && s.resolveTypeBinding().getName().toString().trim().equals(THREAD))
					isYes=true;
				if(!isYes && s.resolveTypeBinding().getSuperclass()!=null && s.resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD))
					isYes=true;
				if(!isYes && s.resolveTypeBinding().getName().toString().trim().equals(RUNNABLE))
					isYes=true;
				if(!isYes && s.resolveTypeBinding().getName().toString().trim().contains(FUTURE))
					isYes=true;//TestRetryCache -> worker = new Callable<Integer>() {; submit = executorService.submit(worker);list.add(submit);
				if(!isYes){	
					ITypeBinding []iTypeBindings=s.resolveTypeBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						if(i.getName().toString().trim().contains(RUNNABLE)){//TestCallQueueManager -> Putter implements Runnable;Putter p = new Putter(manager, -1, -1);producers.add(p)
							isYes=true;				
							break;
						}
					}
				}
				if(isYes){
					String name=null;
					try{//t=*;threads.add(t)
						name=hashmap.get(hashmap.get(CLASS)+s.toString().trim());
					}
					catch (Exception e) {

					}
					try{//MultithreadedTestUtil -> TestThread extends Thread;(TestThread t);threads.add(t)
						if(name == null)
							name=hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND);
					}
					catch (Exception e) {

					}
					hashmap.put(hashmap.get(CLASS)+node.getExpression().toString().trim(),name);
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(),name);
					return true;
				}								
			}
			catch(Exception e){

			}

			try{
				//if(node.getName().toString().trim().equals(JOIN) && node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().equals(THREAD)){
				if(node.getName().toString().trim().equals(JOIN)){//.join   .join(time)
					String name="";
					String joinClass="";
					String endLineNumber="";
					String value="";
					if(node.getExpression().toString().trim().contains(GET)){// TestCallQueueManager-> Taker implements Runnable;Taker t : consumers;threads.get(t).join();
						name=hashmap.get(CLASS)+((SimpleName)((MethodInvocation)node.getExpression()).arguments().get(0)).toString().trim();
					}
					else if(node.getExpression().toString().trim().contains(NEWTHREAD)){//new Thread(thread).join();
						name=hashmap.get(CLASS)+((SimpleName)((ClassInstanceCreation)node.getExpression()).arguments().get(0)).toString().trim();
					}
					else if(node.getExpression().getClass().getSimpleName().toString().trim().contains(ARRAYACCESS)){//thread[i].join -> thread
						name=hashmap.get(CLASS)+((ArrayAccess)node.getExpression()).getArray().toString().trim();
					}
					else{
						name=hashmap.get(CLASS)+node.getExpression().toString().trim();					
					}
					value=hashmap.get(name);	
					if(value.contains(";")){
						String []par=value.split(";");
						joinClass=par[0];
						endLineNumber=par[1];
					}
					else{//AnonymousClass
						joinClass=hashmap.get(CLASS);
						endLineNumber=value;
					}				
					String []par2=endLineNumber.split(",");
					for(String number:par2)
						System.out.println("join: ("+joinClass+")"+number+"--->end--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
					return true;
				}
				else if(node.getName().toString().trim().equals(GET) && (node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FUTURE) || node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FUTURETASK) || node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FORKJOINTASK))){				
					String name="";
					String getClass="";
					String endLineNumber="";
					String value="";
					name=hashmap.get(CLASS)+node.getExpression().toString().trim();				
					value=hashmap.get(name);	
					if(value.contains(";")){
						String []par=value.split(";");
						getClass=par[0];
						endLineNumber=par[1];
					}
					else{//AnonymousClass
						getClass=hashmap.get(CLASS);
						endLineNumber=value;
					}				
					String []par2=endLineNumber.split(",");
					for(String number:par2)
						System.out.println("get: ("+getClass+")"+number+"--->end--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));

					return true;
				}
			}
			catch(Exception e){

			}

			try{
				String result="";
				if(node.getName().toString().trim().equals(LOCK) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){
					result=hashmap.get(RENLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim());
					try{
						if(result.equals("")){//RetryCache -> this.lock.lock();
							result=hashmap.get(RENLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim());
						}
					}
					catch(Exception e){

					}
					try{
						if(result.equals("")){
							result=hashmap.get(RENLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim());
						}
					}
					catch(Exception e){

					}
					System.out.println(result);
					//System.out.println(hashmap.get(RENLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim()));
					System.out.println("entryLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
				else if(node.getName().toString().trim().equals(UNLOCK) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){
					System.out.println("exitLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
				}
				else if(node.getName().toString().trim().equals(ACQUIRE) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
					result=hashmap.get(SEMLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim());
					try{
						if(result.equals("")){
							result=hashmap.get(SEMLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim());
						}
					}
					catch(Exception e){

					}
					try{
						if(result.equals("")){//TestFileSystemCaching -> InitializeForeverFileSystem.sem.acquire();
							result=hashmap.get(SEMLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim());
						}
					}
					catch(Exception e){

					}
					System.out.println(result);
					//System.out.println(hashmap.get(SEMLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim()));
					System.out.println("entrySemaphore: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
				}
				else if(node.getName().toString().trim().equals(RELEASE) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
					System.out.println("exitSemaphore: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}
		}
		return true;	
	}



	/*EnhancedForStatement
	 * */
	@Override
	public 	boolean visit(EnhancedForStatement node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){			
			boolean isYes=false;
			try{					
				if(!isYes && node.getParameter().getType().resolveBinding().getName().toString().trim().equals(THREAD))//TestNativeIO ->for (Thread t : statters) {;t.join()
					isYes=true;		
				if(!isYes && node.getParameter().getType().resolveBinding().getSuperclass()!=null && node.getParameter().getType().resolveBinding().getSuperclass().getName().toString().trim().equals(THREAD))
					isYes=true;
				if(!isYes && node.getParameter().getType().resolveBinding().getName().toString().trim().equals(RUNNABLE))
					isYes=true;
				if(!isYes && node.getParameter().getType().resolveBinding().getName().toString().trim().contains(FUTURE))//TestRPC -> for (Future<Void> f : res) {
					isYes=true;
				if(!isYes){
					ITypeBinding []iTypeBindings=node.getParameter().getType().resolveBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						if(i.getName().toString().trim().contains(RUNNABLE)){
							isYes=true;				
							break;
						}
					}
				}
				if(isYes)
					hashmap.put(hashmap.get(CLASS)+node.getParameter().getName().toString().trim(),hashmap.get(hashmap.get(CLASS)+node.getExpression().toString().trim()));

			}
			catch(Exception e){

			}
		}
		return true;
	}

	/*SynchronizedStatement
	 * */
	@Override
	public 	boolean visit(SynchronizedStatement node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}
		ASTNode bNode=null;
		try{
			bNode=findIndirectType(node.getParent());
			//hashmap.put(CLASS,((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(flag){			
			//synchronized (list)
			try{
				hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim(),"");
			}
			catch(Exception e){

			}
			try{//synchronized (list) Í¬Ê±listÊÇÔÚÍâÀà¶¨Òå£¬ÔÚÄÚÀàÊ¹ÓÃ£¬ËùÒÔ±äÁ¿ÊôÓÚÀàµÄÀà	¼´ÍâÀà	
				hashmap.put(SYNLOCK+((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim()+node.getExpression().toString().trim(),"");
			}
			catch(Exception e){

			}

			try{//ReconfigurableBase -> synchronized (this.parent.reconfigLock) {
				hashmap.put(SYNLOCK+((FieldAccess)node.getExpression()).resolveFieldBinding().getDeclaringClass().getQualifiedName().toString().trim()+((FieldAccess)node.getExpression()).resolveFieldBinding().getName().toString().trim(),"");
			}
			catch(Exception e){

			}
			try{//ReconfigurableBase -> synchronized(getConf()) {
				hashmap.put(SYNLOCK+((MethodInvocation)node.getExpression()).resolveMethodBinding().getDeclaringClass().getQualifiedName().toString().trim()+((MethodInvocation)node.getExpression()).resolveMethodBinding().getName().toString().trim(),"");
			}
			catch(Exception e){

			}

			try{//Configuration -> synchronized(Configuration.class) { -> the same meaning as synchronized(this) {
				hashmap.put(SYNLOCK+((SimpleType)((TypeLiteral)node.getExpression()).getType()).resolveBinding().getQualifiedName().toString().trim()+LOWCLASS,"");			
			}
			catch(Exception e){

			}
			try{//DU -> synchronized(DU.this) { -> the same meaning as synchronized(this) {
				if(node.getExpression().toString().trim().contains(DOTTHIS))
					hashmap.put(SYNLOCK+((ThisExpression)node.getExpression()).resolveTypeBinding().getQualifiedName().toString().trim()+LOWTHIS,"");		
			}
			catch(Exception e){

			}
			try{//ShutdownHookManager ->  synchronized (MGR.hooks) {	
				hashmap.put(SYNLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim(),"");		
			}
			catch(Exception e){

			}
		}
		else{
			String result="";
			try{
				result=hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim());
			}
			catch(Exception e){

			}
			try{
				if(result.equals("")){
					result=hashmap.get(SYNLOCK+((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim()+node.getExpression().toString().trim());
				}
			}
			catch(Exception e){

			}

			try{
				if(result.equals("")){
					result=hashmap.get(SYNLOCK+((FieldAccess)node.getExpression()).resolveFieldBinding().getDeclaringClass().getQualifiedName().toString().trim()+((FieldAccess)node.getExpression()).resolveFieldBinding().getName().toString().trim());
				}
			}
			catch(Exception e){

			}
			try{
				if(result.equals("")){
					result=hashmap.get(SYNLOCK+((MethodInvocation)node.getExpression()).resolveMethodBinding().getDeclaringClass().getQualifiedName().toString().trim()+((MethodInvocation)node.getExpression()).resolveMethodBinding().getName().toString().trim());
				}
			}
			catch(Exception e){

			}
			try{
				if(result.equals("")){
					result=hashmap.get(SYNLOCK+((SimpleType)((TypeLiteral)node.getExpression()).getType()).resolveBinding().getQualifiedName().toString().trim()+LOWCLASS);				
				}
			}
			catch(Exception e){

			}
			try{
				if(result.equals(""))
					if(node.getExpression().toString().trim().contains(DOTTHIS)){
						result=hashmap.get(SYNLOCK+((ThisExpression)node.getExpression()).resolveTypeBinding().getQualifiedName().toString().trim()+LOWTHIS);	
					}
			}
			catch(Exception e){

			}
			try{
				if(result.equals("")){
					result=hashmap.get(SYNLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim());				
				}
			}
			catch(Exception e){

			}

			if(!result.equals("")){
				System.out.println(result);
			}
			System.out.println("entrySynchronized: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
			getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()));//The second parameter is unique
			String endLineNumber=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);
			String []par=endLineNumber.split(",");
			for(String number:par)
				System.out.println("exitSynchronized: ("+hashmap.get(CLASS)+")"+number);	

		}
		return true;
	}

	/*MethodDeclaration
	 * */
	@Override
	public 	boolean visit(MethodDeclaration node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){	
			try{//ReconfigurableBase ->  synchronized(getConf()) {; public Configuration getConf() {
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//¼ÓsynLockµÄÅÐ¶ÏÊÇ·ñÎª¿Õ
					hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), "synLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}		
			try{//Client -> private synchronized void sendPing() throws IOException { Ëø¶ÔÏó¾ÍÊÇ·½·¨
				List<Modifier> modifiers=node.modifiers();
				for(Modifier m : modifiers){
					if(m.getKeyword().toString().trim().equals(SYN)){
						System.out.println("synLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						System.out.println("entrySynchronized: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()),"");//The second parameter is unique
						String endLineNumber=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);
						String []par=endLineNumber.split(",");
						for(String number:par)
							System.out.println("exitSynchronized: ("+hashmap.get(CLASS)+")"+number);
						return true;
					}
				}				
			}
			catch(Exception e){

			}
		}
		return true;
	}

	/*SingleVariableDeclaration
	 * */
	@Override
	public 	boolean visit(SingleVariableDeclaration node) {	
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){	
			try{//Configuration -> public Configuration(Configuration other) {;  synchronized(other) {
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//¼ÓsynLockµÄÅÐ¶ÏÊÇ·ñÎª¿Õ
					hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), "synLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}	

			try{//Shell -> joinThread(errThread);private static void joinThread(Thread t) {;t.join();
				if(hashmap.get(hashmap.get(CLASS)+((MethodDeclaration)node.getParent()).getName().toString().trim()) != null){		
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+((MethodDeclaration)node.getParent()).getName().toString().trim()));	
				}
			}
			catch(Exception e){

			}	
		}
		return true;
	}

	/*ReturnStatement
	 * */
	@Override
	public	boolean visit(ReturnStatement node) {	
		/*Á½´Î¶¼Ö´ÐÐ£º
		µÚÒ»´Î£ºtypeµÄreturn
		µÚ¶þ´Î£ºÄÚ²¿ÀàµÄreturn*/
		try{
			String range=hashmap.get(hashmap.get(CLASS)+RETURNRANGE);
			String []par=range.split(",");
			String endLineNumber="";
			if(par[2].contains(C))
				par[2]=par[2]+END;
			if(compilationUnit.getLineNumber(node.getStartPosition()) >=Integer.parseInt(par[0]) && compilationUnit.getLineNumber(node.getStartPosition()) <=Integer.parseInt(par[1])){
				endLineNumber=hashmap.get(par[2]);
				if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(compilationUnit.getLineNumber(node.getStartPosition())+","))){
					endLineNumber=(endLineNumber==null?"":endLineNumber)+compilationUnit.getLineNumber(node.getStartPosition())+",";
					hashmap.put(par[2], endLineNumber);
				}
			}
		}
		catch(Exception e){

		}
		return true;
	}

	/*Assignment
	 * */
	@Override
	public 	boolean visit(Assignment node) {
		try{
			ASTNode aNode=findType(node.getParent());
			hashmap.put(CLASS,((TypeDeclaration)aNode).resolveBinding().getQualifiedName().toString().trim());
		}
		catch(Exception e){

		}

		if(!flag){	
			/*new Thread(*)*/
			try{		
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					ClassInstanceCreation c=(ClassInstanceCreation)node.getRightHandSide();
					if(c.arguments().size()>0){
						try{//thread2 = new Thread(new FutureTask<Integer>(task));
							ClassInstanceCreation cs=(ClassInstanceCreation)c.arguments().get(0);
							if(cs.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){
								try{//this.*=
									hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(),hashmap.get(hashmap.get(CLASS)+cs.arguments().get(0).toString().trim()) );
									return true;
								}
								catch(Exception e){

								}
								try{//*=
									hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(),hashmap.get(hashmap.get(CLASS)+cs.arguments().get(0).toString().trim()) );
									return true;
								}
								catch(Exception e){

								}	
								try{
									hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+cs.arguments().get(0).toString().trim()) );
									return true;
								}
								catch(Exception e){

								}		
							}
						}
						catch(Exception e){

						}

						SimpleName s=(SimpleName)c.arguments().get(0);												
						try{//(1)thread9=new Thread(thread2);(2)thread = new Thread(futureTask);	
							if(s.resolveTypeBinding().getName().toString().trim().equals(RUNNABLE) || s.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){
								try{//this.*=
									hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(),hashmap.get(hashmap.get(CLASS)+s.toString().trim()) );
									return true;
								}
								catch(Exception e){

								}
								try{//*=
									hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(),hashmap.get(hashmap.get(CLASS)+s.toString().trim()) );
									return true;
								}
								catch(Exception e){

								}	
								try{
									hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+s.toString().trim()) );
									return true;
								}
								catch(Exception e){

								}		
							}
						}
						catch(Exception e){

						}
						
						try{//thread10=new Thread(thread);
							if(!s.resolveTypeBinding().getName().toString().trim().equals(THREAD)){
								ITypeBinding []iTypeBindings=s.resolveTypeBinding().getInterfaces();
								for(ITypeBinding i:iTypeBindings){
									if(i.getName().toString().trim().contains(RUNNABLE)){	
										try{//this.*=
											hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), s.resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}
										try{//*=
											hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), s.resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}	
										try{//TestRPC -> Transactions implements Runnable;Transactions trans = new Transactions(proxy, datasize);threadId[i] = new Thread(trans, "TransactionThread-" + i);
											hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), s.resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}								
									}
								}
							}		
						}
						catch(Exception e){

						}
					}
				}
			}
			catch(Exception e){

			}

			/*Thread*/
			try{//(1) thread4=new TestJoin(); (2)TestJoin thread = new TestJoin(); thread3=thread;			
				if(node.getRightHandSide().resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD) || node.getRightHandSide().resolveTypeBinding().getSuperclass().getSuperclass().getName().toString().trim().equals(THREAD)){
					try{//this.*=
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{//*=						
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}	
				}
			}
			catch(Exception e){

			}	

			try{//(1) Thread thread2=thread; thread3=thread2;
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					try{
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
				}
			}
			catch(Exception e){

			}	

			/*Runnable*/
			try{//(1) thread4=new TestJoin2(); (2) TestJoin2 thread = new TestJoin2();thread6=thread;		
				if(!node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(THREAD)){
					ITypeBinding []iTypeBindings=node.getRightHandSide().resolveTypeBinding().getInterfaces();
					for(ITypeBinding i:iTypeBindings){
						if(i.getName().toString().trim().contains(RUNNABLE)){	
							try{//this.*=
								hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
								return true;
							}
							catch(Exception e){

							}
							try{//*=
								hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
								return true;
							}
							catch(Exception e){

							}	
							try{
								hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
								return true;
							}
							catch(Exception e){

							}	
						}
					}
				}		
			}
			catch(Exception e){

			}

			try{//Runnable thread2=thread;thread3=thread2;
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(RUNNABLE)){
					try{
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
				}
			}
			catch(Exception e){

			}

			/*Callable*/
			try{//(1) task4=new Task(); (2) Task task = new Task(); task6=task;					
				ITypeBinding []iTypeBindings=node.getRightHandSide().resolveTypeBinding().getInterfaces();
				for(ITypeBinding i:iTypeBindings){
					if(i.getName().toString().trim().contains(CALLABLE)){	
						try{//this.*=
							hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
							return true;
						}
						catch(Exception e){

						}
						try{//*=
							hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
							return true;
						}
						catch(Exception e){

						}	
						try{
							hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
							return true;
						}
						catch(Exception e){

						}	
					}
				}	
			}
			catch(Exception e){

			}

			try{//Callable task2=task; task3=task2;
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(CALLABLE)){
					try{
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
				}
			}
			catch(Exception e){

			}

			/*RecursiveTask*/
			try{
				if(node.getRightHandSide().resolveTypeBinding().getSuperclass().getName().toString().trim().contains(RECURSIVETASK)){
					try{//this.*=
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{//*=
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+";"+hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}	
				}
			}
			catch(Exception e){

			}	

			try{
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().contains(FORKJOINTASK) || node.getRightHandSide().resolveTypeBinding().getName().toString().trim().contains(RECURSIVETASK)){
					try{
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(hashmap.get(CLASS)+node.getRightHandSide().toString().trim()));
						return true;
					}
					catch(Exception e){

					}
				}
			}
			catch(Exception e){

			}				
		}
		return true;
	}

	/*ÔÚMethodDeclarationÖÐÕÒ½áÊø½Úµã
	 * */
	public void getEnd(MethodDeclaration node, String key,String name) {
		if(!name.equals(""))
			hashmap.put(key+RETURNRANGE, (compilationUnit.getLineNumber(node.getStartPosition()))+","+(compilationUnit.getLineNumber(node.getStartPosition()+node.getLength()))+","+name);	

		if(name.contains(C)){
			/*ÕÒendµÄÇé¿ö£º(1)Ïß³ÌÀà (2)ÄÚ²¿Àà(3)Synchronized
			¿¼ÂÇ(2)(3)Á¢¼´»ñÈ¡×îºóÐÐ²¢´æÈ¡£¬¼´keyÓÀÔ¶ÊÇ×îÐÂµÄ(Ò²¾ÍÊÇ×Ô¼º¶¨ÒåµÄ),ËùÒÔ²»ÓÃÇø±ð
			(1)²»ÊÇÁ¢¼´»ñÈ¡¶øÊÇÓÃµ½µÄÊ±ºòÔÙ»ñÈ¡,(1)ºÍ(2)Ê¹ÓÃ°üÀà×÷Îªkey,(3)Ê¹ÓÃÐÐºÅ×÷Îªkey,ÕâÑù
			(2)»áºÍÏß³ÌÀàÊ¹ÓÃÍ¬Ò»¸ö°ü+Àà×÷Îªkey¸²¸ÇÏß³ÌÀàµÄkey,ËùÒÔÏß³ÌÀà¼ÓÉÏ<class>×÷ÎªÇø±ð*/
			key=name;
		}
		hashmap.put(key+END,"");

		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",");
	}

	/*ÔÚSynchronizedStatementÖÐÕÒ½áÊø½Úµã
	 * */
	public void getEnd(SynchronizedStatement node, String key) {
		hashmap.put(key+END,"");

		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",");

	}

	/*ÔÚCatchClauseÖÐÕÒ½áÊø½Úµã
	 * */
	public void visitCatch(CatchClause node,String key) {
		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",");		
	}

	/*ÔÚStatementsÖÐÕÒ½áÊø½Úµã
	 * */
	public void getStatementEnd(List<Statement> listStatements, String key, String nodeLine) {
		List<Statement> statements=listStatements;
		Statement statement=null;
		String line="";	
		String endLineNumber="";	
		try{
			if(statements.size() == 0){
				line=nodeLine;
			}
			else{
				statement=statements.get(statements.size()-1);
				Statement tempStatement=statement;
				if(tempStatement.getClass().getSimpleName().toString().trim().equals(TRYSTATEMENT)){	
					statements=((Block)((TryStatement)tempStatement).getBody()).statements();	
					if(statements.size() == 0){
						line=compilationUnit.getLineNumber(tempStatement.getStartPosition())+",";
					}
					if(((TryStatement)tempStatement).getFinally()!=null){
						statements=((TryStatement)tempStatement).getFinally().statements();	
						if(statements.size() == 0){
							line=compilationUnit.getLineNumber(((TryStatement)tempStatement).getFinally().getStartPosition())+",";
						}
					}
					else{	
						CatchClause catchClause;
						for(int i=0;i<((TryStatement)tempStatement).catchClauses().size();i++){
							catchClause=(CatchClause)((TryStatement)tempStatement).catchClauses().get(i);
							visitCatch(catchClause,key);
						}
					}
					if(statements.size() > 0){
						statement=statements.get(statements.size()-1);
					}
				}
			}
			if(line.equals("")){//statements.size() != 0
				if(statement.getClass().getSimpleName().toString().trim().equals(TRYSTATEMENT))
					getStatementEnd(statements, key, "");
				else if(statement.getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
					getIfEnd((IfStatement)statement, key);
				else{
					endLineNumber=hashmap.get(key+END);
					if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(compilationUnit.getLineNumber(statement.getStartPosition())+","))){
						endLineNumber=(endLineNumber==null?"":endLineNumber)+compilationUnit.getLineNumber(statement.getStartPosition())+",";
						hashmap.put(key+END, endLineNumber);
					}
				}
			}
			else{
				endLineNumber=hashmap.get(key+END);
				if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(line))){
					endLineNumber=(endLineNumber==null?"":endLineNumber)+line;
					hashmap.put(key+END, endLineNumber);
				}
			}
		}
		catch(Exception e){

		}
	}

	/*ÔÚIfStatementÖÐÕÒ½áÊø½Úµã
	 * */
	public void getIfEnd(IfStatement node, String key) {
		List<Statement> statements=((Block)node.getThenStatement()).statements();//Block -> ÒªÇóif/elseÖÐ´úÂëÔÚ{}ÖÐ
		Statement statement=null;
		String endLineNumber="";
		String ifLine="";
		if(statements.size() == 0){
			ifLine=compilationUnit.getLineNumber(node.getThenStatement().getStartPosition())+",";
			endLineNumber = hashmap.get(key+END);
			if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(ifLine))){
				endLineNumber=(endLineNumber==null?"":endLineNumber)+ifLine;
				hashmap.put(key+END, endLineNumber);
			}
		}
		else{
			statement=statements.get(statements.size()-1);
			if(statement.getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
				getIfEnd((IfStatement)statement, key);
			else {
				getStatementEnd(statements, key, "");//È·¶¨statements.size() != 0
			}
		}

		if(node.getElseStatement()!=null ){
			if(node.getElseStatement().getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
				getIfEnd((IfStatement)node.getElseStatement(), key);
			else {
				statements=((Block)node.getElseStatement()).statements();
				getStatementEnd(statements, key, compilationUnit.getLineNumber(node.getElseStatement().getStartPosition())+",");
			}
		}
	}

	/*²éÕÒËùÊôÀà
	 * */
	public ASTNode findType(ASTNode node) {	
		while(true){
			if(node.getClass().getSimpleName().toString().trim().equals(TD))
				return node;
			node=node.getParent();
		}	

	}

	/*²éÕÒËùÊôÀàµÄËùÊôÀà
	 * */
	public ASTNode findIndirectType(ASTNode node) {	
		boolean isDirect=true;
		while(true){
			if(isDirect && node.getClass().getSimpleName().toString().trim().equals(TD))
				isDirect=false;
			else if(!isDirect && node.getClass().getSimpleName().toString().trim().equals(TD))
				return node;
			node=node.getParent();
		}	
	}

	/*²é¿´½ÚµãÊÇ·ñÔÚrun/call/compute/main/synchronizedµÄblock/synchronized·½·¨/lock unlock/acquire releaseÀï
	 * */
	public ASTNode isInSpeicalArea(ASTNode node, int line) {
		ASTNode tempNode=node;
		try{
			boolean isInBlock=false;		
			while(true){
				if(node.getClass().getSimpleName().toString().trim().equals(BLOCK)){
					isInBlock=true;
				}
				else if(node.getClass().getSimpleName().toString().trim().equals(SS)){//isInBlock=trueÅÅ³ýsynchronized (list)
					if(isInBlock)
						return node;
					break;//Client -> private synchronized void close() {ÀïÓÐsynchronized (connections) {
				}
				else if(node.getClass().getSimpleName().toString().trim().equals(MD)){//Ã»ÓÐÇ¶Ì×·½·¨£¬ÕÒµ½±ØÍË³ö					
					try{
						List<Modifier> modifiers=((MethodDeclaration)node).modifiers();//Client -> synchronized·½·¨ÖÐ¶¨ÒåºÍÊ¹ÓÃ±äÁ¿
						for(Modifier m : modifiers){
							if(m.getKeyword().toString().trim().equals(SYN))
								return node;
						}
					}
					catch(Exception e){

					}
					if(((MethodDeclaration)node).getName().toString().trim().equals(RUN) || ((MethodDeclaration)node).getName().toString().trim().equals(CALL) || ((MethodDeclaration)node).getName().toString().trim().equals(COMPUTE)){
						if(((MethodDeclaration)node).parameters().size() ==0)//run()¶¼ÊÇÃ»ÓÐ²ÎÊýµÄ
							return node;
					}
					if(((MethodDeclaration)node).getName().toString().trim().equals(MAIN))
						return node;

					break;//±ØÍË³ö
				}
				node=node.getParent();
			}
		}
		catch(Exception e){

		}

		String renRange=hashmap.get(hashmap.get(CLASS)+RENRANGE);
		String semRange=hashmap.get(hashmap.get(CLASS)+SEMRANGE);
		try{
			String []renRanges=renRange.split(";");	
			for(String range:renRanges){//ÔÚlockºÍunlockÖ®¼ä
				String par[]=range.split(",");
				if(line >= Integer.parseInt(par[0]) && line <= Integer.parseInt(par[1]))
					return tempNode;
			}
		}
		catch(Exception e){

		}
		try{
			String []semRanges=semRange.split(";");
			for(String range:semRanges){//ÔÚacquireºÍreleaseÖ®¼ä
				String par[]=range.split(",");
				if(line >= Integer.parseInt(par[0]) && line <= Integer.parseInt(par[1]))
					return tempNode;
			}
		}
		catch(Exception e){

		}
		return null;
	}

	public void traverse(List<CompileUnit> unitElements) {
		for (CompileUnit unitElement: unitElements) {
			CompilationUnit unit = unitElement.getCompilationUnit();
			this.compilationUnit=unit;
			unit.accept(this);
		}
	}

}
