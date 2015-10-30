package storage;

import org.neo4j.graphdb.Node;

import storage.Elements.*;
/**
 * –Œ≤Œ ‰»Î£®–Èƒ‚£©
 * @author Administrator
 *
 */
public class StFormalParaInNode extends StNodeID implements StDGNode{
	ElementName name;
	ElementMethodID methodID;
	ElementType type;
	
	public StFormalParaInNode(int ID,String Name,int MethodID,Class Type){
		id = new ElementID(ID);
		name = new ElementName(Name);
		methodID = new ElementMethodID(MethodID);
		type = new ElementType(Type);
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
		type.setProperty(node);
		node.addLabel(NEO4JAccess.FormalParaInNode);
	}

	public StFormalParaInNode getDGNode(Node node) {
		return new StFormalParaInNode(id.getElement(node).ID, name.getElement(node).Name,
				methodID.getElement(node).MethodID, type.getElement(node).Type);
	}

}