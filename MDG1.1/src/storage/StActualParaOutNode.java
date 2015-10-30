package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * Êµ²ÎÊä³ö£¨ÐéÄâ£©
 * 
 * @author Administrator
 *
 */
public class StActualParaOutNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;
	ElementType type;

	public StActualParaOutNode(int ID, String Name, int MethodID, int Startline, Class Type) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
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
		startline.setProperty(node);
		type.setProperty(node);
		node.addLabel(NEO4JAccess.ActualParaOutNode);
	}

	public StActualParaOutNode getDGNode(Node node) {
		return new StActualParaOutNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline, type.getElement(node).Type);
	}

}
