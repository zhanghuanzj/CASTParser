package storage.Elements;

import java.io.File;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * Â·¾¶£¨File£©
 * 
 * @author Administrator
 *
 */
public class ElementPath implements Elmts {
	public File Path;

	public ElementPath(File path) {
		Path = path;
	}

	public String toString() {
		return Path.getAbsolutePath();
	}

	public ElementPath parse(String s) {
		return new ElementPath(new File(s));
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementPath, toString());
	}

	public ElementPath getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementPath);
		if (obj == null)
			return null;
		return parse((String) obj);
	}
}
