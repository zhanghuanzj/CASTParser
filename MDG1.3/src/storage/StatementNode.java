package storage;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * Óï¾ä½áµã
 * 
 * @author Administrator
 *
 */
public class StatementNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementMethodID methodID;
	ElementStartline startline;
	ElementPath path;
	boolean isLoop;

	public StatementNode(long ID, File Path, String Name, long MethodID, int Startline,boolean loop) {
		id = new ElementID(ID);
		path = new ElementPath(Path);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		isLoop = loop;
	}
	public StatementNode(String Name, File Path, long MethodID, int Startline,boolean loop) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		path = new ElementPath(Path);
		methodID = new ElementMethodID(MethodID);
		startline = new ElementStartline(Startline);
		isLoop = loop;
	}
	/**
	 * store properties into DB node
	 * 
	 * @param node
	 */
	public void setDGNode(Node node) {
		id.setProperty(node);
		path.setProperty(node);
		name.setProperty(node);
		methodID.setProperty(node);
		startline.setProperty(node);
		node.addLabel(NEO4JAccess.StatementNode);
	}

	public StatementNode getDGNode(Node node) {
		return new StatementNode(id.getElement(node).ID, path.getElement(node).Path, name.getElement(node).Name, methodID.getElement(node).MethodID,
				startline.getElement(node).Startline,isLoop);
	}

}
