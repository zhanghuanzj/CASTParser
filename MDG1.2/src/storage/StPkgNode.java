package storage;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * °üÈë¿Ú
 * 
 * @author Administrator
 *
 */
public class StPkgNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementPath path;

	public StPkgNode(long ID, String Name, File Path) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		path = new ElementPath(Path);
	}

	/**
	 * store properties into DB node
	 * 
	 * @param node
	 */
	public void setDGNode(Node node) {
		id.setProperty(node);
		name.setProperty(node);
		path.setProperty(node);
		node.addLabel(NEO4JAccess.PackageNode);
	}

	public StPkgNode getDGNode(Node node) {
		return new StPkgNode(id.getElement(node).ID, name.getElement(node).Name, path.getElement(node).Path);
	}

}
