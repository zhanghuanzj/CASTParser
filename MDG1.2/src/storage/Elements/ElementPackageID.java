package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ËùÊô°üµÄID£¨int£©
 * 
 * @author Administrator
 *
 */
public class ElementPackageID implements Elmts {
	public long PackageID;

	public ElementPackageID(long packageID) {
		PackageID = packageID;
	}

	public String toString() {
		return new Long(PackageID).toString();
	}

	public ElementPackageID parse(String s) {
		return new ElementPackageID(Long.parseLong(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementPackageID, toString());
	}

	public ElementPackageID getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementPackageID);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
