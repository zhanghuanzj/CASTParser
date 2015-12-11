package storage.Elements;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;

import storage.NEO4JAccess;

/**
 * ²ÎÊý£¨List<Class>£©
 * 
 * @author Administrator
 *
 */
public class ElementParameter implements Elmts {
	public List<String> Parameter;

	public ElementParameter(List<String> parameter) {
		Parameter = parameter;
	}

	/**
	 * String[] can be directly stored into DB
	 * 
	 * @return
	 */
	public String[] toListString() {
		if (Parameter == null)
			return new String[] { "" };
		if (Parameter.size() == 0)
			return new String[] { "" };
		String[] clss = new String[Parameter.size()];
		for (int i = 0; i < clss.length; i++)
			clss[i] = Parameter.get(i);
		return clss;
	}

	public ElementParameter parse(String[] s) {
		if (s == null)
			return new ElementParameter(null);
		if ((s.length == 1) && (s[0]).equals(""))
			return new ElementParameter(null);
		List<String> pst = new ArrayList<String>();
		for (String cls : s)
				pst.add(cls);
	
		return new ElementParameter(pst);
	}

	public void setProperty(Node node) {
		node.setProperty(NEO4JAccess.ElementParameter, toListString());
	}

	public ElementParameter getElement(Node node) {
		Object obj = node.getProperty(NEO4JAccess.ElementParameter);
		if (obj == null)
			return null;
		return parse((String[]) obj);
	}
}
