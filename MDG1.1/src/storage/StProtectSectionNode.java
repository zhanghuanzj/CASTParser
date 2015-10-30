package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * ±£»¤´úÂë¶Î
 * 
 * @author Administrator
 *
 */
public class StProtectSectionNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementStartline startline;
	ElementStopline stopline;

	public StProtectSectionNode(int ID, String Name, int ClassID, int Startline, int Stopline) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		startline = new ElementStartline(Startline);
		stopline = new ElementStopline(Stopline);
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
		stopline.setProperty(node);
		node.addLabel(NEO4JAccess.ProtectAreaNode);
	}

	public StProtectSectionNode getDGNode(Node node) {
		return new StProtectSectionNode(id.getElement(node).ID, name.getElement(node).Name, classID.getElement(node).ClassID,
				startline.getElement(node).Startline, stopline.getElement(node).Stopline);
	}

}
