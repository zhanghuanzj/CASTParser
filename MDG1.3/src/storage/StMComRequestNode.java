package storage;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * M��ͨ������
 * 
 * @author Administrator
 *
 */
public class StMComRequestNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;
	ElementPath path;

	public StMComRequestNode(long ID, String Name, long MethodID, int Startline,File Path) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		path =new ElementPath(Path);
	}
	public StMComRequestNode(String Name, long MethodID, int Startline,File Path) {
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
		path.setProperty(node);
		node.addLabel(NEO4JAccess.MCommunRequestNode);
	}

	public StMComRequestNode getDGNode(Node node) {
		return new StMComRequestNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, startline.getElement(node).Startline,path.getElement(node).Path);
	}

}