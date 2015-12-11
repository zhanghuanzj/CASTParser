package storage;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * c(x)(��c-nodeͨ�ŵĽڵ�)
 * 
 * @author Administrator
 *
 */
public class StCxNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;
	ElementPath path;

	public StCxNode(long ID, String Name, long MethodID, int Startline,File Path) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		path =new ElementPath(Path);
	}
	public StCxNode(String Name, long MethodID, int Startline,File Path) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		path =new ElementPath(Path);
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
		node.addLabel(NEO4JAccess.CxNode);
	}

	public StCxNode getDGNode(Node node) {
		return new StCxNode(id.getElement(node).ID, name.getElement(node).Name, methodID.getElement(node).MethodID,
				startline.getElement(node).Startline,path.getElement(node).Path);
	}

}
