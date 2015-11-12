package storage;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.Elements.*;

/**
 * ÀàÈë¿Ú
 * 
 * @author Administrator
 *
 */
public class StClassNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementPath path;
	ElementPackageID packageID;
	ElementModifier modifier;

	public StClassNode(long ID, String Name, File Path, long PackageID, boolean[] Modifier) {
		id = new ElementID(ID);
		name = new ElementName(Name);
		path = new ElementPath(Path);
		packageID = new ElementPackageID(PackageID);
		modifier = new ElementModifier(Modifier);
	}
	public StClassNode(String Name, File Path, long PackageID, boolean[] Modifier) {
		id = new ElementID(-1);
		name = new ElementName(Name);
		path = new ElementPath(Path);
		packageID = new ElementPackageID(PackageID);
		modifier = new ElementModifier(Modifier);
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
		packageID.setProperty(node);
		modifier.setProperty(node);
		node.addLabel(NEO4JAccess.ClassNode);
	}

	public StClassNode getDGNode(Node node) {
		return new StClassNode(id.getElement(node).ID, name.getElement(node).Name, path.getElement(node).Path,
				packageID.getElement(node).PackageID, modifier.getElement(node).Modifier);
	}

}
