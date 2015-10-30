package storage;

import storage.Elements.ElementID;

public abstract class StNodeID {

	public ElementID id;
	
	public void setID(int ID){
		id = new ElementID(ID);
	}
	public int getid()
	{
		return this.id.getid();
	}

}