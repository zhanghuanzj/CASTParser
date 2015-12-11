package storage;

import storage.Elements.ElementID;

public abstract class StNodeID {

	public ElementID id;
	
	public void setID(long ID){
		id = new ElementID(ID);
	}
	public long getid()
	{
		return this.id.getid();
	}

}