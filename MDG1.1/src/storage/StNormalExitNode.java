package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * �����˳������⣩
 * 
 * @author Administrator
 *
 */
public class StNormalExitNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;

	public StNormalExitNode(int ID, String Name, int MethodID) {
		id = new ElementID(ID);
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
		node.addLabel(NEO4JAccess.ExitNormalNode);
	}

	public StNormalExitNode getDGNode(Node node) {
		return new StNormalExitNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID);
	}

}