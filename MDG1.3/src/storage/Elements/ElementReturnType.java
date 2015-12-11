package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ∑µªÿ¿‡–Õ£®Class£©
 * 
 * @author Administrator
 *
 */
public class ElementReturnType implements Elmts {
	public String ReturnType;

	public ElementReturnType(String returnType) {
		ReturnType = returnType;
	}

	public String toString() {
		return ReturnType.toString();
	}

	public ElementReturnType parse(String s) {
		return new ElementReturnType(s);
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementReturnType, toString());
	}

	public ElementReturnType getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementReturnType);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
