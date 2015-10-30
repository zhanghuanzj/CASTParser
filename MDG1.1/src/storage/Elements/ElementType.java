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
	public Class Type;

	public ElementType(Class type) {
		Type = type;
	}

	public String toString() {
		return Type.getName();
	}

	public ElementType parse(String s) {
		try {
			return new ElementType(Class.forName(s));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
