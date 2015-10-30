package storage;

import java.util.List;

import org.neo4j.graphdb.Node;

import storage.Elements.*;
/**
 * 实例方法入口
 * @author Administrator
 *
 */
public class StMethodNode extends StNodeID implements StDGNode {
	ElementName name;
	ElementClassID classID;
	ElementStartline startline;
	ElementStopline stopline;
	ElementModifier modifier;
	ElementParameter parameter;
	ElementReturnType returnType;
	
	public StMethodNode(int ID,String Name,int ClassID,int Startline,int Stopline,boolean[] Modifier,List<Class> Parameter,Class ReturnType){
		id = new ElementID(ID);
		name = new ElementName(Name);
		classID = new ElementClassID(ClassID);
		startline = new ElementStartline(Startline);
		stopline = new ElementStopline(Stopline);
		modifier = new ElementModifier(Modifier);
		parameter = new ElementParameter(Parameter);
		returnType = new ElementReturnType (ReturnType);
	}
	/**
	 * store properties into DB node
	 * 
	 * @param node
	 */
	public void setDGNode(Node node) {
		id.setProperty(node);
		name.setProperty(node);
		classID.setProperty(node);
		startline.setProperty(node);
		stopline.setProperty(node);
		modifier.setProperty(node);
		parameter.setProperty(node);
		returnType.setProperty(node);
		node.addLabel(NEO4JAccess.InstanceMethodNode);
	}

	public StMethodNode getDGNode(Node node) {
		return new StMethodNode(id.getElement(node).ID, name.getElement(node).Name,
				classID.getElement(node).ClassID, startline.getElement(node).Startline,
				stopline.getElement(node).Stopline, modifier.getElement(node).Modifier,
				parameter.getElement(node).Parameter, returnType.getElement(node).ReturnType);
	}

}
