package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * Ëø¶ÔÏó
 * 
 * @author Administrator
 *
 */
public class StLockObjectNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementType type;
	ElementModifier modifier;

	public StLockObjectNode(int ID, String Name, int ClassID, Class Type, boolean[] Modifier) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		modifier = new ElementModifier(Modifier);
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
		classID.setProperty(node);
		type.setProperty(node);
		modifier.setProperty(node);
		node.addLabel(NEO4JAccess.LockObjectNode);
	}

	public StLockObjectNode getDGNode(Node node) {
		return new StLockObjectNode(id.getElement(node).ID, name.getElement(node).Name, classID.getElement(node).ClassID,
				type.getElement(node).Type, modifier.getElement(node).Modifier);
	}

}
