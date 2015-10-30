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
	public Class ReturnType;

	public ElementReturnType(Class returnType) {
		ReturnType = returnType;
	}

	public String toString() {
		return ReturnType.getName();
	}

	public ElementReturnType parse(String s) {
		try {
			return new ElementReturnType(Class.forName(s));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
