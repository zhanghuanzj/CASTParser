package storage.Elements;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * À˘ Ù¿‡µƒID£®int£©
 * 
 * @author Administrator
 *
 */
public class ElementClassID implements Elmts {
	public long ClassID;

	public ElementClassID(long classID) {
		ClassID = classID;
	}
	public String toString() {
		return new Long(ClassID).toString();
	}

	public ElementClassID parse(String s) {
		return new ElementClassID(Long.parseLong(s));
	}
	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementClassID, toString());
	}
	public ElementClassID getElement(Node node){
		Object obj = node.getProperty(NEO4JAccess.ElementClassID);
		if(obj==null)return null;
		return parse((String)obj);
	}
}
