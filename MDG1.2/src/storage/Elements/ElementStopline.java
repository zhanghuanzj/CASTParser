package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ÷’÷π––∫≈£®int£©
 * 
 * @author Administrator
 *
 */
public class ElementStopline implements Elmts {
	public int Stopline;

	public ElementStopline(int stopline) {
		Stopline = stopline;
	}

	public String toString() {
		return new Integer(Stopline).toString();
	}

	public ElementStopline parse(String s) {
		return new ElementStopline(Integer.parseInt(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementStopline, toString());
	}

	public ElementStopline getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementStopline);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
