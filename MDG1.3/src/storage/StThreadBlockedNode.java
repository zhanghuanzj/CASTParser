package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 线程结束自动触发的阻塞结点(join-get)
 * 
 * @author Administrator
 *
 */
public class StThreadBlockedNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;

	public StThreadBlockedNode(long ID, String Name, long MethodID, int Startline) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
	}
	public StThreadBlockedNode(String Name, long MethodID, int Startline) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
	}

	/**
	 * store properties into DB node
	 * 
	 * @param node
	 */
	public void setDGNode(Node node) {
		id.setProperty(node);
		name.setProperty(node);
		methodID.setProperty(node);
		startline.setProperty(node);
		node.addLabel(NEO4JAccess.BlockThreadendNode);
	}

	public StThreadBlockedNode getDGNode(Node node) {
		return new StThreadBlockedNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline);
	}

}
