package storage.Elements;

import org.neo4j.graphdb.Node;

public interface Elmts {
	public void setProperty(Node node);
	public Elmts getElement(Node node);
}
