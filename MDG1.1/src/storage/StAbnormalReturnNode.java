package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * “Ï≥£∑µªÿ£®–Èƒ‚£©
 * 
 * @author Administrator
 *
 */
public class StAbnormalReturnNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;

	public StAbnormalReturnNode(int ID, String Name, int MethodID) {
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
		node.addLabel(NEO4JAccess.ReturnExceptionNode);
	}

	public StAbnormalReturnNode getDGNode(Node node) {
		return new StAbnormalReturnNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID);
	}

}