package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * Òì³£ÍË³ö£¨ÐéÄâ£©
 * 
 * @author Administrator
 *
 */
public class StAbnormalExitNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;

	public StAbnormalExitNode(long ID, String Name, long MethodID) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
	}
	public StAbnormalExitNode(String Name, long MethodID) {
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
		node.addLabel(NEO4JAccess.ExitExceptionNode);
	}

	public StAbnormalExitNode getDGNode(Node node) {
		return new StAbnormalExitNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID);
	}

}