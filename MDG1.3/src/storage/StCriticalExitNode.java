package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 临界区（保护程序段）出口
 * 
 * @author Administrator
 *
 */
public class StCriticalExitNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementStartline startline;

	public StCriticalExitNode(long ID, String Name, long ClassID, int Startline) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		startline = new ElementStartline(Startline);
	}
	public StCriticalExitNode(String Name, long ClassID, int Startline) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
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
		classID.setProperty(node);
		startline.setProperty(node);
		node.addLabel(NEO4JAccess.CriticalExitNode);
	}

	public StCriticalExitNode getDGNode(Node node) {
		return new StCriticalExitNode(id.getElement(node).ID, name.getElement(node).Name,
				classID.getElement(node).ClassID, startline.getElement(node).Startline);
	}

}
