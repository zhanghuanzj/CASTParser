package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 与反射相关的对象节点
 * 
 * @author Administrator
 *
 */
public class StReflectedNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementType type;
	ElementModifier modifier;

	public StReflectedNode(long ID, String Name, long ClassID, String Type, boolean[] Modifier) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		modifier = new ElementModifier(Modifier);
		type = new ElementType(Type);
	}
	public StReflectedNode(String Name, long ClassID, String Type, boolean[] Modifier) {
		id = new ElementID(-1);
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
		node.addLabel(NEO4JAccess.ClassReflectNode);
	}

	public StReflectedNode getDGNode(Node node) {
		return new StReflectedNode(id.getElement(node).ID, name.getElement(node).Name,
				classID.getElement(node).ClassID, type.getElement(node).Type, modifier.getElement(node).Modifier);
	}

}
