package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 线程入口结点
 * 
 * @author Administrator
 *
 */
public class StThreadEntryNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;

	public StThreadEntryNode(long ID, String Name, long MethodID) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
	}
	public StThreadEntryNode(String Name, long MethodID) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
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
		node.addLabel(NEO4JAccess.ThreadEntryNode);
	}

	public StThreadEntryNode getDGNode(Node node) {
		return new StThreadEntryNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID);
	}

}
