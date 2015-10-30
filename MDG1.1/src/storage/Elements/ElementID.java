package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * �����ID��int��
 * 
 * @author Administrator
 *
 */
public class ElementID implements Elmts {
	public int ID;

	public ElementID(int ID) {
		this.ID = ID;
	}
	
	public int getid()
	{
		return this.ID;
	}

	public String toString() {
		return new Integer(ID).toString();
	}

	public ElementID parse(String s) {
		return new ElementID(Integer.parseInt(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementID, toString());
	}

	public ElementID getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementID);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
