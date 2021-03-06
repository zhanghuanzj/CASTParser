package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 多态对象扩展类型（虚拟）
 * 
 * @author Administrator
 *
 */
public class StPolyObjTypeNode extends StNodeID implements StDGNode {
	ElementName name;

	public StPolyObjTypeNode(int ID, String Name) {
		id = new ElementID(ID);
		name = new ElementName(Name);
	}

	/**
	 * store properties into DB node
	 * 
	 * @param node
	 */
	public void setDGNode(Node node) {
		id.setProperty(node);
		name.setProperty(node);
		node.addLabel(NEO4JAccess.PolyObjectExtensionNode);
	}

	public StPolyObjTypeNode getDGNode(Node node) {
		return new StPolyObjTypeNode(id.getElement(node).ID, name.getElement(node).Name);
	}

}