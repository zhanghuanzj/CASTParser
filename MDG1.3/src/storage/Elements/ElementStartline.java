package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ÆðÊ¼ÐÐºÅ£¨int£©
 * 
 * @author Administrator
 *
 */
public class ElementStartline implements Elmts {
	public int Startline;

	public ElementStartline(int startline) {
		Startline = startline;
	}

	public String toString() {
		return new Integer(Startline).toString();
	}

	public ElementStartline parse(String s) {
		return new ElementStartline(Integer.parseInt(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementStartline, toString());
	}

	public ElementStartline getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementStartline);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
