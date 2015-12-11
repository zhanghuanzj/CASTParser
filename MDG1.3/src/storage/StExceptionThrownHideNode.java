package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * 匿名异常抛出点
 * 
 * @author Administrator
 *
 */
public class StExceptionThrownHideNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;

	public StExceptionThrownHideNode(long ID, String Name, long MethodID, int Startline) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
	}
	public StExceptionThrownHideNode(String Name, long MethodID, int Startline) {
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
		node.addLabel(NEO4JAccess.ExceptionThrownHideNode);
	}

	public StExceptionThrownHideNode getDGNode(Node node) {
		return new StExceptionThrownHideNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline);
	}

}
