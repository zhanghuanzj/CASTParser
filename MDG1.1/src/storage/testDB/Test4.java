package storage.testDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import storage.StAbstractMethodNode;
import storage.NEO4JAccess;
/**
 * How to store nodes
 * @author think
 *
 */
public class Test4 {
	private static enum RelTypes implements RelationshipType {
		KNOWS
	};

	public static void main(String[] args) throws ClassNotFoundException {
		boolean[] b = {false, true};
		List<Class> p = new ArrayList<Class>();
		p.add(Class.forName("java.util.ArrayList"));
		p.add(Class.forName("storage.testDB.TestElmts"));
		Class ret = Class.forName("java.util.ArrayList");
		StAbstractMethodNode a = new StAbstractMethodNode(1,"Test",1,1,1,b,p,ret);
		NEO4JAccess.setDBPath("C:/graph.db");
		NEO4JAccess.store(a);
	}

}
