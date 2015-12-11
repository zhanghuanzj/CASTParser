package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ¿‡–Õ£®Class£©
 * 
 * @author Administrator
 *
 */
public class ElementType implements Elmts{
	public String Type;

	public ElementType(String type) {
		Type = type;
	}

	public String toString() {
		return Type.toString();
	}

	public ElementType parse(String s) {
		return new ElementType(s);
	}
	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementType, toString());
	}

	public ElementType getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementType);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
