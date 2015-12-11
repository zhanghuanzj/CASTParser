package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * “Ï≥£ΩÿªÒµ„
 * 
 * @author Administrator
 *
 */
public class StExceptionInterceptionNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;

	public StExceptionInterceptionNode(long ID, String Name, long MethodID, int Startline) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
	}
	public StExceptionInterceptionNode(String Name, long MethodID, int Startline) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
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
		node.addLabel(NEO4JAccess.ExceptionInterceptionNode);
	}

	public StExceptionInterceptionNode getDGNode(Node node) {
		return new StExceptionInterceptionNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline);
	}

}
