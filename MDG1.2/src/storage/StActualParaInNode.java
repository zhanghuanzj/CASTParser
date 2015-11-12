package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 *  µ≤Œ ‰»Î£®–Èƒ‚£©
 * 
 * @author Administrator
 *
 */
public class StActualParaInNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;
	ElementType type;

	public StActualParaInNode(long ID, String Name, long MethodID, int Startline, String Type) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		type = new ElementType(Type);
	}
	public StActualParaInNode(String Name, long MethodID, int Startline, String Type) {
		id = new ElementID(-1);
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
		node.addLabel(NEO4JAccess.ActualParaInNode);
	}

	public StActualParaInNode getDGNode(Node node) {
		return new StActualParaInNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline, type.getElement(node).Type);
	}

}
