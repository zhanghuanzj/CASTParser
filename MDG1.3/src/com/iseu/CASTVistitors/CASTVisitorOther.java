package com.iseu.CASTVistitors;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.*;
import storage.DGEdge;
import com.iseu.CASTParser.CompileUnit;
import com.iseu.MDGHandle.Edges.Edge;
import com.iseu.MDGHandle.Nodes.Node;


public class CASTVisitorOther extends ASTVisitor {

	CompilationUnit compilationUnit;
	String filePath;
	HashMap<String,String> hashmap;
	boolean flag=true;
	ArrayList<Edge> edges; 

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
	String ENTRYREN="entryRen";
	String RENLOCK="renLock";
	String ENTRYSEM="entrySem";
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
	String VARIABLE="variable";
	String EXCEPTION="Exception";
	String ARRAYACCESS="ArrayAccess";
	String RETURNRANGE="ReturnRange";
	String TRYSTATEMENT="TryStatement";
	String IFSTATEMENT="IfStatement";
	String MEMBERVARIABLE="MemberVariable";


	public CASTVisitorOther(HashMap<String,String> hashmap, boolean flag, ArrayList<Edge> edges) {
		super();
		this.hashmap = hashmap;
		this.flag=flag;
		this.edges=edges;
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
				if(!isNeed){//放入线程出口行号				
					MethodDeclaration[] method=node.getMethods();
					for(MethodDeclaration m:method){
						if(m.getName().toString().trim().equals(RUN) || m.getName().toString().trim().equals(CALL) || m.getName().toString().trim().equals(COMPUTE)){
							boolean isReturnTypeEqual=false;
							boolean isParameterEmpty=false;				
							try{//TestForkJoin -> class TestForkJoin extends RecursiveTask<Integer> {;public Integer compute() {中的Integer
								ITypeBinding []iTypeBindings=node.getSuperclassType().resolveBinding().getTypeArguments();
								if(m.getReturnType2().toString().trim().equals(iTypeBindings[0].getName().toString().trim()))
									isReturnTypeEqual=true;
							}
							catch(Exception e){
								isReturnTypeEqual=true;
							}	
							if(isReturnTypeEqual && m.parameters().size() == 0){//compute()而不是compute(size)
								isParameterEmpty=true;		
							}
							if(isParameterEmpty){
								getEnd(m,node.resolveBinding().getQualifiedName().toString().trim(),node.resolveBinding().getQualifiedName().toString().trim()+C,filePath,m.getName().toString().trim());
								//return true;//可能implements Runnable, Callable
								return true;
							}

						}
					}
					//					//没有符合条件的Run..,这样为守护线程
					//					hashmap.put(node.resolveBinding().getQualifiedName().toString().trim()+CEND, node.resolveBinding().getQualifiedName().toString().trim()+"&"+DAEMONTHREAD+"&"+DAEMONTHREAD);

				}
			}
			catch(Exception e){

			}
		}
		else{
			try{//Synchronized(this) -> lock object
				if(hashmap.get(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWTHIS) != null){			
					hashmap.put(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWTHIS, filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));			
				}
			}
			catch(Exception e){

			}
			try{//synchronized(Configuration.class) -> lock object
				if(hashmap.get(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWCLASS) != null){
					hashmap.put(SYNLOCK+node.resolveBinding().getQualifiedName().toString().trim()+LOWCLASS, filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));	
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
			//			String className="";
			String line="";
			line=hashmap.get(hashmap.get(CLASS)+node.toString().trim()+VARIABLE);
			//			if(line !=null && !line.equals("")){//""表示TestJoin -> main中重新定义j
			//				className=hashmap.get(CLASS);
			//			}
			//			else if(line ==null){
			//				try{//Server -> 外类定义private ConnectionManager connectionManager;内类Syn中使用connectionManager.stopIdleScan();
			//					ASTNode bNode=findIndirectType(node.getParent());
			//					line=hashmap.get(((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim()+node.toString().trim()+VARIABLE);
			//					if(line !=null){
			//						className=((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim();
			//					}
			//				}
			//				catch(Exception e){
			//
			//				}
			//			}

			if(line ==null){
				try{//Server -> 外类定义private ConnectionManager connectionManager;内类Syn中使用connectionManager.stopIdleScan();
					ASTNode bNode=findIndirectType(node.getParent());
					line=hashmap.get(((TypeDeclaration)bNode).resolveBinding().getQualifiedName().toString().trim()+node.toString().trim()+VARIABLE);				
				}
				catch(Exception e){

				}
			}
			if(line !=null && !line.equals("")){//line.equals("")表示TestJoin -> main中重新定义j
				ASTNode returnNode=isInSpeicalArea(node.getParent(), compilationUnit.getLineNumber(node.getStartPosition()));
				if(returnNode != null){
					try{
						MethodDeclaration mNode=(MethodDeclaration)returnNode;
						if(!(mNode.getName().toString().trim().equals(MAIN)) && mNode.parameters().size() > 0){//Configuration -> public synchronized void setIfUnset(String name, String value) {
							List<SingleVariableDeclaration> single=mNode.parameters();
							for(SingleVariableDeclaration s : single){
								if(s.getName().toString().trim().equals(node.toString().trim()))//使用的是public synchronized void setIfUnset(String name, String value) {中的name或value
									return true;
							}
						}
					}
					catch(Exception e){

					}					

					try{
						if(!(((MethodInvocation)node.getParent()).getName().toString().trim().equals(WAIT) || ((MethodInvocation)node.getParent()).getName().toString().trim().equals(NOTIFY) || ((MethodInvocation)node.getParent()).getName().toString().trim().equals(NOTIFYALL))){
							String []fromPar=line.split("&");
							Node from=new Node(fromPar[0],fromPar[1],fromPar[2]);						
							Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
							Edge threadRiskEdge=new Edge(from, to, DGEdge.threadRisk);
							edges.add(threadRiskEdge);
							//							System.out.println("threadSafetyRisk: ("+className+")"+line+"--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						}
					}
					catch(Exception e){//不是MethodInvocation引发的异常，此时node也满足要求
						String []fromPar=line.split("&");
						Node from=new Node(fromPar[0],fromPar[1],fromPar[2]);
						Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge threadRiskEdge=new Edge(from, to, DGEdge.threadRisk);
						edges.add(threadRiskEdge);
						//						System.out.println("threadSafetyRisk: ("+className+")"+line+"--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
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
			/*变量定义分析*/
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
						String returnName=findMethod(node);
						if(returnName.equals(MEMBERVARIABLE))
							hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim()+VARIABLE,hashmap.get(CLASS)+"&"+node.getName().toString().trim()+"&"+MEMBERVARIABLE);					
						else {
							hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim()+VARIABLE,filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));					
						}			
					}
					else//在里面重新定义
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim()+VARIABLE,"");
				}
			}
			catch(Exception e){

			}

			/*new Thread(*)*/
			try{
				if(node.getInitializer().resolveTypeBinding().getName().toString().trim().equals(THREAD) || node.getInitializer().resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD)){
					ClassInstanceCreation c=(ClassInstanceCreation)node.getInitializer();
					if(c.arguments().size()>0){
						try{//Thread thread2 = new Thread(new FutureTask<Integer>(task));
							ClassInstanceCreation cs=(ClassInstanceCreation)c.arguments().get(0);
							if(cs.resolveTypeBinding().getName().toString().trim().contains(FUTURETASK)){
								hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(hashmap.get(CLASS)+cs.arguments().get(0).toString().trim()));	
								return true;
							}
							
							if(!cs.resolveTypeBinding().getName().toString().trim().equals(THREAD)){//Thread默认实现Runnable
								ITypeBinding []iTypeBindings=cs.resolveTypeBinding().getInterfaces();
								for(ITypeBinding i:iTypeBindings){
									if(i.getName().toString().trim().contains(RUNNABLE)){	
										hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));									
										return true;
									}
								}
							}	

							if(cs.resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD)){
								hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim().replaceAll("\\<\\w*\\>","")+CEND));									
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
										hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));									
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
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
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
							hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
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
						hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
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
					hashmap.put(hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(node.getInitializer().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));	
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
				if(hashmap.get(RENLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//加renLock的判断是否为空
					String returnName=findMethod(node);
					if(returnName.equals(MEMBERVARIABLE))
						hashmap.put(RENLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(CLASS)+"&"+node.getName().toString().trim()+"&"+MEMBERVARIABLE);	
					else {
						hashmap.put(RENLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));	
					}
					return true;
				}
			}
			catch(Exception e){

			}

			try{
				if(hashmap.get(SEMLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){
					String returnName=findMethod(node);
					if(returnName.equals(MEMBERVARIABLE))
						hashmap.put(SEMLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(CLASS)+"&"+node.getName().toString().trim()+"&"+MEMBERVARIABLE);		
					else {
						hashmap.put(SEMLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));		
					}
					return true;
				}
			}
			catch(Exception e){

			}

			try{
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){
					String returnName=findMethod(node);
					if(returnName.equals(MEMBERVARIABLE))
						hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), hashmap.get(CLASS)+"&"+node.getName().toString().trim()+"&"+MEMBERVARIABLE);	
					else {
						hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));	
					}
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
							getEnd(m, hashmap.get(CLASS),name,filePath,m.getName().toString().trim());

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
							getEnd(m, hashmap.get(CLASS),name,filePath,m.getName().toString().trim());

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
							getEnd(m, hashmap.get(CLASS),name,filePath,m.getName().toString().trim());
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
							getEnd(m, hashmap.get(CLASS),name,filePath,m.getName().toString().trim());

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
			try{//release(name)等会引发异常
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
					//					String joinClass="";
					//					String endLineNumber="";
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
					//					if(value.contains(";")){
					//						String []par=value.split(";");
					//						joinClass=par[0];
					//						endLineNumber=par[1];
					//					}
					//					else{//AnonymousClass
					//						joinClass=hashmap.get(CLASS);
					//						endLineNumber=value;
					//					}	
					String []fromPar=value.split("&");							
					Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");			
					String []par2=fromPar[2].split(",");
					for(String number:par2){//多个线程出口
						Node from=new Node(fromPar[0],fromPar[1],number);		
						Edge futureGetEdge=new Edge(from, to, DGEdge.FutureGet);
						edges.add(futureGetEdge);
						//System.out.println("join: ("+joinClass+")"+number+"--->end--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
					}			
					return true;
				}
				else if(node.getName().toString().trim().equals(GET) && (node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FUTURE) || node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FUTURETASK) || node.resolveMethodBinding().getDeclaringClass().getName().toString().trim().contains(FORKJOINTASK))){				
					String name="";
					//					String getClass="";
					//					String endLineNumber="";
					String value="";
					name=hashmap.get(CLASS)+node.getExpression().toString().trim();				
					value=hashmap.get(name);	
					String []fromPar=value.split("&");	
					Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");			
					String []par2=fromPar[2].split(",");
					for(String number:par2){//多个线程出口
						Node from=new Node(fromPar[0],fromPar[1],number);
						Edge futureGetEdge=new Edge(from, to, DGEdge.FutureGet);
						edges.add(futureGetEdge);
						//System.out.println("join: ("+joinClass+")"+number+"--->end--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
					}
					//					if(value.contains(";")){
					//						String []par=value.split(";");
					//						getClass=par[0];
					//						endLineNumber=par[1];
					//					}
					//					else{//AnonymousClass
					//						getClass=hashmap.get(CLASS);
					//						endLineNumber=value;
					//					}				
					//					String []par2=endLineNumber.split(",");
					//					for(String number:par2)
					//						System.out.println("get: ("+getClass+")"+number+"--->end--->"+"("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));

					return true;
				}
			}
			catch(Exception e){

			}

			try{
				String result="";
				String name="";
				if((node.getName().toString().trim().equals(LOCK) || node.getName().toString().trim().equals(UNLOCK)) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){
					result=hashmap.get(RENLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim());
					name=hashmap.get(CLASS)+node.getExpression().toString().trim();
					try{
						if(result.equals("")){//RetryCache -> this.lock.lock();
							result=hashmap.get(RENLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim());
							name=hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim();
						}
					}
					catch(Exception e){

					}
					try{
						if(result.equals("")){
							result=hashmap.get(RENLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim());
							name=((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim();
						}
					}
					catch(Exception e){

					}
					//					System.out.println(result);
					if(node.getName().toString().trim().equals(LOCK)){
						String []fromPar=result.split("&");
						Node from=new Node(fromPar[0],fromPar[1],fromPar[2]);						
						Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge authorityAcquireEdge=new Edge(from, to, DGEdge.authorityAcquire);
						edges.add(authorityAcquireEdge);
						/*保护代码段构造步骤（1）
						hashmap.put(ENTRYREN+name, filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));
						 */
					}
					else {
						String []toPar=result.split("&");
						Node to=new Node(toPar[0],toPar[1],toPar[2]);						
						Node from=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge authorityReleaseEdge=new Edge(from, to, DGEdge.authorityRelease);
						edges.add(authorityReleaseEdge);
						/*保护代码段构造步骤（2）
						String entryRen=hashmap.get(ENTRYREN+name);
						if(entryRen !=null){		
						       保护代码段构造代码				
							hashmap.put(ENTRYREN+name,null);//解锁后生命周期结束
						}*/
					}


					//					System.out.println("entryLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
				//				else if(node.getName().toString().trim().equals(UNLOCK) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(REENTRANTLOCK)){
				//					System.out.println("exitLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
				//				}
				else if((node.getName().toString().trim().equals(ACQUIRE) || node.getName().toString().trim().equals(RELEASE)) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
					result=hashmap.get(SEMLOCK+hashmap.get(CLASS)+node.getExpression().toString().trim());
					name=hashmap.get(CLASS)+node.getExpression().toString().trim();
					try{
						if(result.equals("")){
							result=hashmap.get(SEMLOCK+hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim());
							name=hashmap.get(CLASS)+((FieldAccess)node.getExpression()).getName().toString().trim();
						}
					}
					catch(Exception e){

					}
					try{
						if(result.equals("")){//TestFileSystemCaching -> InitializeForeverFileSystem.sem.acquire();
							result=hashmap.get(SEMLOCK+((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim());
							name=((QualifiedName)node.getExpression()).getQualifier().resolveTypeBinding().getQualifiedName().toString().trim()+((QualifiedName)node.getExpression()).getName().toString().trim();
						}
					}
					catch(Exception e){

					}
					//					System.out.println(result);
					if(node.getName().toString().trim().equals(ACQUIRE)){
						String []fromPar=result.split("&");
						Node from=new Node(fromPar[0],fromPar[1],fromPar[2]);						
						Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge authorityAcquireEdge=new Edge(from, to, DGEdge.authorityAcquire);
						edges.add(authorityAcquireEdge);
						/*hashmap.put(ENTRYSEM+name, filePath+"&"+findMethod(node)+"&"+compilationUnit.getLineNumber(node.getStartPosition()));*/
					}
					else {
						String []toPar=result.split("&");
						Node to=new Node(toPar[0],toPar[1],toPar[2]);						
						Node from=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge authorityReleaseEdge=new Edge(from, to, DGEdge.authorityRelease);
						edges.add(authorityReleaseEdge);
						/*String entrySem=hashmap.get(ENTRYSEM+name);
						if(entrySem !=null){

							hashmap.put(ENTRYSEM+name,null);//解锁后生命周期结束
						}*/
					}

					//					System.out.println("entrySemaphore: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
				}
				//				else if(node.getName().toString().trim().equals(RELEASE) && node.getExpression().resolveTypeBinding().getName().toString().trim().equals(SEMAPHORE)){
				//					System.out.println("exitSemaphore: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));	
				//				}
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
			try{//synchronized (list) 同时list是在外类定义，在内类使用，所以变量属于类的类	即外类	
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
				//				System.out.println(result);
				String []fromPar=result.split("&");
				Node from=new Node(fromPar[0],fromPar[1],fromPar[2]);						
				Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
				Edge authorityAcquireEdge=new Edge(from, to, DGEdge.authorityAcquire);
				edges.add(authorityAcquireEdge);

				to=from;
				getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()),filePath,findMethod(node));//The second parameter is unique
				String value=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);
				fromPar=value.split("&");	

				String []par2=fromPar[2].split(",");
				for(String number:par2){//多个出口
					from=new Node(fromPar[0],fromPar[1],number);				
					Edge authorityReleaseEdge=new Edge(from, to, DGEdge.authorityRelease);
					edges.add(authorityReleaseEdge);
				}
			}
			/*	System.out.println("entrySynchronized: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
			getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()),filePath,findMethod(node));//The second parameter is unique
			String endLineNumber=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);				
			String []par=endLineNumber.split(",");
			for(String number:par)
				System.out.println("exitSynchronized: ("+hashmap.get(CLASS)+")"+number);*/	
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
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//加synLock的判断是否为空
					hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), filePath+"&"+MD+"&"+compilationUnit.getLineNumber(node.getStartPosition()));	
				}
			}
			catch(Exception e){

			}		
			try{//Client -> private synchronized void sendPing() throws IOException { 锁对象就是方法
				List<Modifier> modifiers=node.modifiers();
				for(Modifier m : modifiers){
					if(m.getKeyword().toString().trim().equals(SYN)){
						Node from=new Node(hashmap.get(CLASS),TD,TD);						
						Node to=new Node(filePath, findMethod(node), compilationUnit.getLineNumber(node.getStartPosition())+"");							
						Edge authorityAcquireEdge=new Edge(from, to, DGEdge.authorityAcquire);
						edges.add(authorityAcquireEdge);

						to=from;
						getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()),"",filePath,node.getName().toString().trim());//The second parameter is unique
						String value=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);
						String []fromPar=value.split("&");		
						String []par2=fromPar[2].split(",");
						for(String number:par2){//多个出口
							from=new Node(fromPar[0],fromPar[1],number);				
							Edge authorityReleaseEdge=new Edge(from, to, DGEdge.authorityRelease);
							edges.add(authorityReleaseEdge);
						}

						//						System.out.println("synLock: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						//						System.out.println("entrySynchronized: ("+hashmap.get(CLASS)+")"+compilationUnit.getLineNumber(node.getStartPosition()));
						//						getEnd(node, hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition()),"",filePath,node.getName().toString().trim());//The second parameter is unique
						//						String endLineNumber=hashmap.get(hashmap.get(CLASS)+compilationUnit.getLineNumber(node.getStartPosition())+END);
						//						String []par=endLineNumber.split(",");
						//						for(String number:par)
						//							System.out.println("exitSynchronized: ("+hashmap.get(CLASS)+")"+number);
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
				if(hashmap.get(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim()) != null){//加synLock的判断是否为空
					hashmap.put(SYNLOCK+hashmap.get(CLASS)+node.getName().toString().trim(), filePath+"&"+MD+"&"+compilationUnit.getLineNumber(node.getStartPosition()));
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
		/*两次都执行：
		第一次：type的return
		第二次：内部类的return*/
		try{
			String range=hashmap.get(hashmap.get(CLASS)+RETURNRANGE);
			String []par=range.split(",");
			String endLineNumber="";
			if(par[2].contains(C))
				par[2]=par[2]+END;
			if(compilationUnit.getLineNumber(node.getStartPosition()) >=Integer.parseInt(par[0]) && compilationUnit.getLineNumber(node.getStartPosition()) <=Integer.parseInt(par[1])){
				endLineNumber=hashmap.get(par[2]);
				if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(compilationUnit.getLineNumber(node.getStartPosition())+","))){
					endLineNumber=((endLineNumber==null || endLineNumber.equals(""))?par[3]+"&"+par[4]+"&":endLineNumber)+compilationUnit.getLineNumber(node.getStartPosition())+",";
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
				if(node.getRightHandSide().resolveTypeBinding().getName().toString().trim().equals(THREAD) || node.getRightHandSide().resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD)){
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
							
							//JvmPauseMonitor -> Monitor implements Runnable {; monitorThread = new Daemon(new Monitor()); monitorThread.join();
							if(!cs.resolveTypeBinding().getName().toString().trim().equals(THREAD)){
								ITypeBinding []iTypeBindings=cs.resolveTypeBinding().getInterfaces();
								for(ITypeBinding i:iTypeBindings){
									if(i.getName().toString().trim().contains(RUNNABLE)){	
										try{//this.*=
											hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}
										try{//*=
											hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}	
										try{
											hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}								
									}
								}
							}	

							//AbstractDelegationTokenSecretManager -> class ExpiredTokenRemover extends Thread {;tokenRemoverThread = new Daemon(new ExpiredTokenRemover());tokenRemoverThread.join();
							if(cs.resolveTypeBinding().getSuperclass().getName().toString().trim().equals(THREAD)){
								try{//this.*=
									hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim().replaceAll("\\<\\w*\\>","")+CEND));
									return true;
								}
								catch(Exception e){

								}
								try{//*=   正则表达式去除<>内容，因为存的时候解绑QulifiedName没有<>
									hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim().replaceAll("\\<\\w*\\>","")+CEND));
									return true;
								}
								catch(Exception e){

								}	
								try{
									hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), hashmap.get(cs.resolveTypeBinding().getQualifiedName().toString().trim().replaceAll("\\<\\w*\\>","")+CEND));
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
											hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}
										try{//*=
											hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
											return true;
										}
										catch(Exception e){

										}	
										try{//TestRPC -> Transactions implements Runnable;Transactions trans = new Transactions(proxy, datasize);threadId[i] = new Thread(trans, "TransactionThread-" + i);
											hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), hashmap.get(s.resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
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
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{//*=						
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
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
								hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
								return true;
							}
							catch(Exception e){

							}
							try{//*=
								hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
								return true;
							}
							catch(Exception e){

							}	
							try{
								hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(),hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
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
							hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
							return true;
						}
						catch(Exception e){

						}
						try{//*=
							hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
							return true;
						}
						catch(Exception e){

						}	
						try{
							hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
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
						hashmap.put(hashmap.get(CLASS)+((FieldAccess)node.getLeftHandSide()).getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{//*=
						hashmap.put(hashmap.get(CLASS)+((SimpleName)node.getLeftHandSide()).resolveBinding().getName().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
						return true;
					}
					catch(Exception e){

					}
					try{
						hashmap.put(hashmap.get(CLASS)+((ArrayAccess)node.getLeftHandSide()).getArray().toString().trim(), hashmap.get(node.getRightHandSide().resolveTypeBinding().getQualifiedName().toString().trim()+CEND));
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

	/*在MethodDeclaration中找结束节点
	 * */
	public void getEnd(MethodDeclaration node, String key,String name,String filePath,String methodName) {
		if(!name.equals(""))
			hashmap.put(key+RETURNRANGE, (compilationUnit.getLineNumber(node.getStartPosition()))+","+(compilationUnit.getLineNumber(node.getStartPosition()+node.getLength()))+","+name+","+filePath+","+methodName);	

		if(name.contains(C)){
			/*找end的情况：(1)线程类 (2)内部类(3)Synchronized
			考虑(2)(3)立即获取最后行并存取，即key永远是最新的(也就是自己定义的),所以不用区别
			(1)不是立即获取而是用到的时候再获取,(1)和(2)使用包类作为key,(3)使用行号作为key,这样
			(2)会和线程类使用同一个包+类作为key覆盖线程类的key,所以线程类加上<class>作为区别*/
			key=name;
		}
		hashmap.put(key+END,"");

		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",",filePath,methodName);
	}

	/*在SynchronizedStatement中找结束节点
	 * */
	public void getEnd(SynchronizedStatement node, String key,String filePath,String methodName) {
		hashmap.put(key+END,"");

		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",",filePath,methodName);

	}

	/*在CatchClause中找结束节点
	 * */
	public void visitCatch(CatchClause node,String key,String filePath,String methodName) {
		getStatementEnd(node.getBody().statements(), key, compilationUnit.getLineNumber(node.getStartPosition())+",",filePath,methodName);		
	}

	/*在Statements中找结束节点
	 * */
	public void getStatementEnd(List<Statement> listStatements, String key, String nodeLine,String filePath,String methodName) {
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
							visitCatch(catchClause,key,filePath,methodName);
						}
					}
					if(statements.size() > 0){
						statement=statements.get(statements.size()-1);
					}
				}
			}
			if(line.equals("")){//statements.size() != 0
				if(statement.getClass().getSimpleName().toString().trim().equals(TRYSTATEMENT))
					getStatementEnd(statements, key, "",filePath,methodName);
				else if(statement.getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
					getIfEnd((IfStatement)statement, key,filePath,methodName);
				else{
					endLineNumber=hashmap.get(key+END);
					if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(compilationUnit.getLineNumber(statement.getStartPosition())+","))){
						endLineNumber=((endLineNumber==null || endLineNumber.equals(""))?filePath+"&"+methodName+"&":endLineNumber)+compilationUnit.getLineNumber(statement.getStartPosition())+",";
						hashmap.put(key+END, endLineNumber);
					}
				}
			}
			else{
				endLineNumber=hashmap.get(key+END);
				if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(line))){
					endLineNumber=((endLineNumber==null || endLineNumber.equals(""))?filePath+"&"+methodName+"&":endLineNumber)+line;			
					hashmap.put(key+END, endLineNumber);
				}
			}
		}
		catch(Exception e){

		}
	}

	/*在IfStatement中找结束节点
	 * */
	public void getIfEnd(IfStatement node, String key,String filePath,String methodName) {
		List<Statement> statements=((Block)node.getThenStatement()).statements();//Block -> 要求if/else中代码在{}中
		Statement statement=null;
		String endLineNumber="";
		String ifLine="";
		if(statements.size() == 0){
			ifLine=compilationUnit.getLineNumber(node.getThenStatement().getStartPosition())+",";
			endLineNumber = hashmap.get(key+END);
			if(endLineNumber == null || (endLineNumber !=null && !endLineNumber.contains(ifLine))){
				endLineNumber=((endLineNumber==null || endLineNumber.equals(""))?filePath+"&"+methodName+"&":endLineNumber)+ifLine;
				hashmap.put(key+END, endLineNumber);
			}
		}
		else{
			statement=statements.get(statements.size()-1);
			if(statement.getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
				getIfEnd((IfStatement)statement, key,filePath,methodName);
			else {
				getStatementEnd(statements, key, "",filePath,methodName);//确定statements.size() != 0
			}
		}

		if(node.getElseStatement()!=null ){
			if(node.getElseStatement().getClass().getSimpleName().toString().trim().equals(IFSTATEMENT))
				getIfEnd((IfStatement)node.getElseStatement(), key,filePath,methodName);
			else {
				statements=((Block)node.getElseStatement()).statements();
				getStatementEnd(statements, key, compilationUnit.getLineNumber(node.getElseStatement().getStartPosition())+",",filePath,methodName);
			}
		}
	}

	/*查找所属类
	 * */
	public ASTNode findType(ASTNode node) {	
		while(true){
			if(node.getClass().getSimpleName().toString().trim().equals(TD))
				return node;
			node=node.getParent();
		}	
	}

	/*查找所属类的所属类
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

	/*查看节点是否在run/call/compute/main/synchronized的block/synchronized方法/lock unlock/acquire release里
	 * */
	public ASTNode isInSpeicalArea(ASTNode node, int line) {
		ASTNode tempNode=node;
		try{
			boolean isInBlock=false;		
			while(true){
				if(node.getClass().getSimpleName().toString().trim().equals(BLOCK)){
					isInBlock=true;
				}
				else if(node.getClass().getSimpleName().toString().trim().equals(SS)){//isInBlock=true排除synchronized (list)
					if(isInBlock)
						return node;
					break;//Client -> private synchronized void close() {里有synchronized (connections) {
				}
				else if(node.getClass().getSimpleName().toString().trim().equals(MD)){//没有嵌套方法，找到必退出					
					try{
						List<Modifier> modifiers=((MethodDeclaration)node).modifiers();//Client -> synchronized方法中定义和使用变量
						for(Modifier m : modifiers){
							if(m.getKeyword().toString().trim().equals(SYN))
								return node;
						}
					}
					catch(Exception e){

					}
					if(((MethodDeclaration)node).getName().toString().trim().equals(RUN) || ((MethodDeclaration)node).getName().toString().trim().equals(CALL) || ((MethodDeclaration)node).getName().toString().trim().equals(COMPUTE)){
						if(((MethodDeclaration)node).parameters().size() ==0)//run()都是没有参数的
							return node;
					}
					if(((MethodDeclaration)node).getName().toString().trim().equals(MAIN))
						return node;

					break;//必退出
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
			for(String range:renRanges){//在lock和unlock之间
				String par[]=range.split(",");
				if(line >= Integer.parseInt(par[0]) && line <= Integer.parseInt(par[1]))
					return tempNode;
			}
		}
		catch(Exception e){

		}
		try{
			String []semRanges=semRange.split(";");
			for(String range:semRanges){//在acquire和release之间
				String par[]=range.split(",");
				if(line >= Integer.parseInt(par[0]) && line <= Integer.parseInt(par[1]))
					return tempNode;
			}
		}
		catch(Exception e){

		}
		return null;
	}

	/*查找所属方法
	 * */
	public String findMethod(ASTNode node) {
		try{
			while(true){		
				if(node.getClass().getSimpleName().toString().trim().equals(MD))
					return ((MethodDeclaration)node).getName().toString().trim();	
				node=node.getParent();
			}
		}
		catch(Exception e){

		}
		return MEMBERVARIABLE;
	}

	public void traverse(List<CompileUnit> unitElements) {
		for (CompileUnit unitElement: unitElements) {
			CompilationUnit unit = unitElement.getCompilationUnit();
			this.compilationUnit=unit;
			this.filePath=unitElement.getFilePath();
			unit.accept(this);
		}
	}

}
