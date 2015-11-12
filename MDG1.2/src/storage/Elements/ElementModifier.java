package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ÐÞÊÎ·û£¨boolean[]£©
 * 
 * @author Administrator
 *
 */
public class ElementModifier implements Elmts {
	public boolean[] Modifier;

	public ElementModifier(boolean[] modifier) {
		Modifier = modifier;
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementModifier, Modifier);
	}

	public ElementModifier getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementModifier);
		if (obj == null)
			return null;
		return new ElementModifier((boolean[])obj);
	}
}
