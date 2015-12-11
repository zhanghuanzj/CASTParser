package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * “Ï≥£∂‘œÛ
 * 
 * @author Administrator
 *
 */
public class StExceptionObjectNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementType type;

	public StExceptionObjectNode(long ID, String Name, long MethodID, String Type) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		type = new ElementType(Type);
	}
	public StExceptionObjectNode(String Name, long MethodID, String Type) {
		id = new ElementID(-1);
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
		node.addLabel(NEO4JAccess.ExceptionObjectNode);
	}

	public StExceptionObjectNode getDGNode(Node node) {
		return new StExceptionObjectNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, type.getElement(node).Type);
	}

}
