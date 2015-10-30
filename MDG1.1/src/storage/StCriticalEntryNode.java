package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 临界区（保护程序段）入口
 * 
 * @author Administrator
 *
 */
public class StCriticalEntryNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementStartline startline;

	public StCriticalEntryNode(int ID, String Name, int ClassID, int Startline) {
		id = new ElementID(ID);
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
		node.addLabel(NEO4JAccess.CriticalEntryNode);
	}

	public StCriticalEntryNode getDGNode(Node node) {
		return new StCriticalEntryNode(id.getElement(node).ID, name.getElement(node).Name,
				classID.getElement(node).ClassID, startline.getElement(node).Startline);
	}

}
