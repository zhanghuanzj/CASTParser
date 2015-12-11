package storage;

import java.io.File;

import java.util.Iterator;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import storage.StCxNode;

import storage.Elements.*;

public class NEO4JAccess {
	private static String DB_PATH = "E:/graph.db";
	public static GraphDatabaseService graphDb;

	public static final String ElementClassID = "ClassID";
	public static final String ElementID = "ID";
	public static final String ElementMethodID = "MethodID";
	public static final String ElementModifier = "Modifier";
	public static final String ElementName = "Name";
	public static final String ElementPackageID = "PackageID";
	public static final String ElementParameter = "Parameter";
	public static final String ElementPath = "FilePath";
	public static final String ElementReturnType = "ReturnType";
	public static final String ElementStartline = "Startline";
	public static final String ElementStopline = "Stopline";
	public static final String ElementType = "Type";
	public static final String ElementMark = "Mark";
	public static final String ElementStartID = "StartID";
	public static final String ElementEndID = "EndID";

	public static final Label AbstractMethodNode = DynamicLabel.label("AbstractMethodNode");
	public static final Label ActualParaInNode = DynamicLabel.label("ActualParaInNode");
	public static final Label ActualParaOutNode = DynamicLabel.label("ActualParaOutNode");
	public static final Label BlockNode = DynamicLabel.label("BlockNode");
	public static final Label BlockThreadendNode = DynamicLabel.label("BlockThreadendNode");
	public static final Label ClassHideNode = DynamicLabel.label("ClassHideNode");
	public static final Label ClassNode = DynamicLabel.label("ClassNode");
	public static final Label ClassReflectNode = DynamicLabel.label("ClassReflectNode");
	public static final Label CNode = DynamicLabel.label("CNode");
	public static final Label ConnectionReceiveNode = DynamicLabel.label("ConnectionReceiveNode");
	public static final Label ConnectionRequestNode = DynamicLabel.label("ConnectionRequestNode");
	public static final Label CriticalEntryNode = DynamicLabel.label("CriticalEntryNode");
	public static final Label CriticalExitNode = DynamicLabel.label("CriticalExitNode");
	public static final Label CxNode = DynamicLabel.label("CxNode");
	public static final Label ExceptionInterceptionNode = DynamicLabel.label("ExceptionInterceptionNode");
	public static final Label ExceptionObjectNode = DynamicLabel.label("ExceptionObjectNode");
	public static final Label ExceptionThrownHideNode = DynamicLabel.label("ExceptionThrownHideNode");
	public static final Label ExceptionThrownNode = DynamicLabel.label("ExceptionThrownNode");
	public static final Label ExitExceptionNode = DynamicLabel.label("ExitExceptionNode");
	public static final Label ExitNormalNode = DynamicLabel.label("ExitNormalNode");
	public static final Label FormalParaInNode = DynamicLabel.label("FormalParaInNode");
	public static final Label FormalParaOutNode = DynamicLabel.label("FormalParaOutNode");
	public static final Label InstanceMethodNode = DynamicLabel.label("InstanceMethodNode");
	public static final Label InterfaceNode = DynamicLabel.label("InterfaceNode");
	public static final Label LockObjectNode = DynamicLabel.label("LockObjectNode");
	public static final Label MCommunReceiveNode = DynamicLabel.label("MCommunReceiveNode");
	public static final Label MCommunRequestNode = DynamicLabel.label("MCommunRequestNode");
	public static final Label MemberVariableNode = DynamicLabel.label("MemberVariableNode");
	public static final Label MoCommunReceiveNode = DynamicLabel.label("MoCommunReceiveNode");
	public static final Label MoCommunRequestNode = DynamicLabel.label("MoCommunRequestNode");
	public static final Label ObjectNode = DynamicLabel.label("ObjectNode");
	public static final Label PackageNode = DynamicLabel.label("PackageNode");
	public static final Label PolyObjectExtensionNode = DynamicLabel.label("PolyObjectExtensionNode");
	public static final Label PolyObjectNode = DynamicLabel.label("PolyObjectNode");
	public static final Label ProtectAreaNode = DynamicLabel.label("ProtectAreaNode");
	public static final Label ReturnExceptionNode = DynamicLabel.label("ReturnExceptionNode");
	public static final Label ReturnNormalNode = DynamicLabel.label("ReturnNormalNode");
	public static final Label StatementNode = DynamicLabel.label("StatementNode");
	public static final Label ThreadEntryNode = DynamicLabel.label("ThreadEntryNode");
	public static final Label ThreadExitNode = DynamicLabel.label("ThreadExitNode");
	public static final Label WakeNode = DynamicLabel.label("WakeNode");
	public static final Label TriggerNode = DynamicLabel.label("TriggerNode");
	public static final Label InterruptNotify = DynamicLabel.label("InterruptNotify");
	public static final Label InterruptAccept = DynamicLabel.label("InterruptAccept");
	public static final Label LoopNode = DynamicLabel.label("LoopNode");

	public static void setProperty(Node node, Elmts element) {

	}

	/**
	 * ���ÿ�дĿ¼�������ݿ�
	 * 
	 * @param s
	 * @return
	 */
	public static boolean setDBPath(String s) {
		DB_PATH = s.toString();
		return true;
	}

	public static GraphDatabaseService getDB() {
		if (graphDb == null) {
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
			registerShutdownHook(graphDb);
		}
		return graphDb;
	}

	/**
	 * store DGNode
	 * 
	 * @param node
	 * @return node id
	 */
	// public Node find2(int sl)
	// {
	// final ExecutionEngine engine = new ExecutionEngine(graphDb,
	// StringLogger.SYSTEM);
	// String cName = null;
	// boolean success = false;
	//
	// try (Transaction tx = graphDb.beginTx()) {
	// while (!success) {
	// try {
	// Map<String, Object> params = new HashMap<String, Object>();
	// org.neo4j.cypher.javacompat.ExecutionResult result =
	// engine.execute("match (n:Parent)-[:PARENT_CHILD]->(m:Child) where
	// m.sl={sl} return m.i", params);
	// for (Map<String, Object> row : result) {
	// cName = (String) row.get("m.name");
	// break;
	// }
	// success = true;
	// } catch (org.neo4j.graphdb.NotFoundException e) {
	//// LOG.info(">>>> RETRY QUERY ON NotFoundException: " + count);
	// try {
	// Thread.sleep((long) Math.random() * 100);
	// } catch (InterruptedException e1) {
	// e1.printStackTrace();
	// }
	// }
	// }
	// }

	public long CXstore(long id) {
		int i = 0;
		try (Transaction ts = graphDb.beginTx()) {
			// Iterator<Node> al_nodes = graphDb.getAllNodes().iterator();
			Iterator<Node> al_nodes = graphDb.findNodes(DynamicLabel.label("n27"));

			for (i = 0; i < 10000; i++) {
				try {
					if (!al_nodes.hasNext())
						break;
					Node n = al_nodes.next();
					long x = n.getId();
					Node node = graphDb.getNodeById(x);
					Object temp = node.getId();
					int t = Integer.parseInt(String.valueOf(temp));
					// Object f =search.getProperty(ElementPath);
					if (t == id) {
						// Node node =graphDb.getNodeById(id);
						StCxNode cx = new StCxNode(-1, (String) node.getProperty(ElementName),
								Integer.parseInt(String.valueOf(node.getProperty(ElementMethodID))),
								Integer.parseInt((String) node.getProperty(ElementStartline)),
								new File(String.valueOf(node.getProperty(ElementPath))));
						NEO4JAccess.storeBare(cx);
						ts.success();
						ts.close();
						return cx.getid();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 1;
	}

	public static boolean hasNode(int id) {
		Iterator<Node> all_nodes = null;
		try (Transaction ts = graphDb.beginTx()) {
			all_nodes = graphDb.getAllNodes().iterator();
			ts.success();
			ts.close();
		}
		while (all_nodes.hasNext())
			if (all_nodes.next().getId() == id)
				return true;
		return false;
	}

	public long search(String label, String line) {
		int i = 0;
		try (Transaction ts = graphDb.beginTx()) {
			// Iterator<Node> al_nodes = graphDb.getAllNodes().iterator();
			Iterator<Node> al_nodes = graphDb.findNodes(DynamicLabel.label(label));

			for (i = 0; i < 10000; i++) {
				try {
					if (!al_nodes.hasNext())
						break;
					Node n = al_nodes.next();
					long id = n.getId();
					Node search = graphDb.getNodeById(id);
					Object temp = search.getProperty(ElementStartline);
					// Object f =search.getProperty(ElementPath);
					if (temp.equals(line)) {
						return search.getId();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 1;
	}

	// public void CNstore ( ){
	// int i = 0;
	// try (Transaction ts = graphDb.beginTx()) {
	// // Iterator<Node> al_nodes = graphDb.getAllNodes().iterator();
	// Iterator<Node> al_nodes = graphDb.findNodes(DynamicLabel.label("n26"));
	//
	// for (i = 0; i < 10000; i++) {
	// try {
	// if (!al_nodes.hasNext())
	// break;
	// Node n = al_nodes.next();
	// StCNode a=new StCNode(-1,(String)
	// n.getProperty(ElementName),Integer.parseInt((String)n.getProperty(ElementMethodID)),Integer.parseInt((String)n.getProperty(ElementStartline)
	// ),new File((String)n.getProperty(ElementPath)));
	// store(a);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	public static int store(StDGNode node) {
		getDB();
		int ret = -1;
		try (Transaction ts = graphDb.beginTx()) {
			Node firstNode = graphDb.createNode();
			ret = (int) firstNode.getId();
			if (node instanceof StNodeID) {
				((StNodeID) node).setID(ret);
			}
			node.setDGNode(firstNode);
			ts.success();
			ts.close();
		}
		return ret;
	}

	public static Node find(String labelName, String propertyName, Object propertyValue) {
		try (Transaction ts = graphDb.beginTx()) {
			if (propertyValue != null) {
				Label label = DynamicLabel.label(labelName);
				@SuppressWarnings("deprecation")
				ResourceIterable<Node> ri = graphDb.findNodesByLabelAndProperty(label, propertyName, propertyValue);
				if (ri != null) {
					try {

						ResourceIterator<Node> iter = ri.iterator();

						try {
							if (iter != null && iter.hasNext()) {
								return iter.next();

							}
						} finally {
							iter.close();
						}
					} catch (Exception e) {
						// LOG.error("ERROR WHILE FINDING ID: " + propertyValue
						// + " , LABEL: " + labelName + " , PROPERTY: " +
						// propertyName, e);
					}
				}
			}
		}
		System.out.println("can not find");
		return null;
	}

	public static int storeBare(StDGNode node) {
		int ret = -1;

		Node firstNode = graphDb.createNode();
		ret = (int) firstNode.getId();
		if (node instanceof StNodeID) {
			((StNodeID) node).setID(ret);
		}
		node.setDGNode(firstNode);
		if (ret == 54) {
			System.out.println("stored node : " + ret);
		}
		return ret;
	}

	/**
	 * find nodes by id and add edge
	 * 
	 * @param src
	 *            source node of the edge
	 * @param tag
	 *            taget node of the edge
	 * @param edgeType
	 *            type in DGEdge
	 * @return false if nodes not found
	 */
	public static boolean store(long src, long tag, DGEdge edgeType) {
		getDB();
		try (Transaction ts = graphDb.beginTx()) {
			Node srcNode = graphDb.getNodeById(src);
			Node tagNode = graphDb.getNodeById(tag);
			if ((srcNode == null) || (tagNode == null)) {
				return false;
			}
			srcNode.createRelationshipTo(tagNode, edgeType);
//			tagNode.createRelationshipTo(srcNode, edgeType.getReverse());
			ts.success();
			ts.close();
		}
		return true;
	}

	public static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
