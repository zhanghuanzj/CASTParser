package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * ∂‡Ã¨∂‘œÛ
 * 
 * @author Administrator
 *
 */
public class StPolyObNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementType type;

	public StPolyObNode(int ID, String Name, int MethodID, Class Type) {
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
		node.addLabel(NEO4JAccess.PolyObjectNode);
	}

	public StPolyObNode getDGNode(Node node) {
		return new StPolyObNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, type.getElement(node).Type);
	}

}
