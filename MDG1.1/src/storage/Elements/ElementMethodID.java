package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * 所属方法的ID（int）
 * 
 * @author Administrator
 *
 */
public class ElementMethodID implements Elmts {
	public int MethodID;

	public ElementMethodID(int methodID) {
		MethodID = methodID;
	}

	public String toString() {
		return new Integer(MethodID).toString();
	}

	public ElementMethodID parse(String s) {
		return new ElementMethodID(Integer.parseInt(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementMethodID, toString());
	}

	public ElementMethodID getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementMethodID);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
