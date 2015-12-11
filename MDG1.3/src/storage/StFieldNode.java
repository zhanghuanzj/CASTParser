package storage;

import java.util.List;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 成员变量
 * 
 * @author Administrator
 *
 */
public class StFieldNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementType type;
	ElementModifier modifier;
	ElementParameter parameter;

	public StFieldNode(long ID, String Name, long ClassID, String Type, boolean[] Modifier, List<String> Parameter) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		type = new ElementType(Type);
		modifier = new ElementModifier(Modifier);
		parameter = new ElementParameter(Parameter);
	}
	public StFieldNode(String Name, long ClassID, String Type, boolean[] Modifier, List<String> Parameter) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		type = new ElementType(Type);
		modifier = new ElementModifier(Modifier);
		parameter = new ElementParameter(Parameter);
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
		parameter.setProperty(node);
		node.addLabel(NEO4JAccess.MemberVariableNode);
	}

	public StFieldNode getDGNode(Node node) {
		return new StFieldNode(id.getElement(node).ID, name.getElement(node).Name,
				classID.getElement(node).ClassID, type.getElement(node).Type, modifier.getElement(node).Modifier,
				parameter.getElement(node).Parameter);
	}

}
