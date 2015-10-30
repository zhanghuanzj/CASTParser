package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 对象结点
 * 
 * @author Administrator
 *
 */
public class StObjNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementType type;

	public StObjNode(int ID, String Name, int MethodID, Class Type) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		type = new ElementType(Type);
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
		type.setProperty(node);
		node.addLabel(NEO4JAccess.ObjectNode);
	}

	public StObjNode getDGNode(Node node) {
		return new StObjNode(id.getElement(node).ID, name.getElement(node).Name, methodID.getElement(node).MethodID,
				type.getElement(node).Type);
	}

}
