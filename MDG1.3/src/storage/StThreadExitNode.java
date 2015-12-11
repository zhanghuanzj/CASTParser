package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;
/**
 * 线程出口
 * @author Administrator
 *
 */
public class StThreadExitNode extends StNodeID implements StDGNode{
	ElementName name;
	ElementMethodID methodID;
	
	public StThreadExitNode(long ID,String Name,long MethodID){
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
	}
	public StThreadExitNode(String Name,long MethodID){
		id = new ElementID(-1);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
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
		node.addLabel(NEO4JAccess.ThreadExitNode);
	}

	public StThreadExitNode getDGNode(Node node) {
		return new StThreadExitNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID);
	}

}
