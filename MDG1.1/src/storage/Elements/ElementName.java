package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ÃüÃû£¨String£©
 * 
 * @author Administrator
 *
 */
public class ElementName implements Elmts {
	public String Name;

	public ElementName(String name) {
		Name = name;
	}

	public String toString() {
		return Name.toString();
	}

	public ElementName parse(String s) {
		return new ElementName(s.toString());
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementName, toString());
	}

	public ElementName getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementName);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
