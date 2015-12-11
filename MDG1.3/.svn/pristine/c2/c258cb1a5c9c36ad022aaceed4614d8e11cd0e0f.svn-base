package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 匿名类
 * 
 * @author Administrator
 *
 */
public class StAnnoymousClassNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;// 所属类的id

	public StAnnoymousClassNode(long ID, String Name, long ClassID) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
	}
	public StAnnoymousClassNode(String Name, long ClassID) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
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
		node.addLabel(NEO4JAccess.ClassHideNode);
	}

	public StAnnoymousClassNode getDGNode(Node node) {
		return new StAnnoymousClassNode(id.getElement(node).ID, name.getElement(node).Name, classID.getElement(node).ClassID);
	}

}
