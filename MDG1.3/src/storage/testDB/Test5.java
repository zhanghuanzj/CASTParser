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
 * How to access nodes in DB
 * !! the id of nodes is set while stored 
 * @author think
 *
 */
public class Test5 {
	private static enum RelTypes implements RelationshipType {
		KNOWS
	};

	public static void main(String[] args) throws ClassNotFoundException {
		boolean[] b = { false, true };
		List<String> p = new ArrayList<String>();
		p.add("java.util.ArrayList");
		p.add("storage.testDB.TestElmts");
		Class ret = Class.forName("java.util.ArrayList");
		StAbstractMethodNode a = new StAbstractMethodNode(-1, "Test", 1, 1, 1, b, p, ret.getName());
		NEO4JAccess.setDBPath("C:/graph.db");
		int id = NEO4JAccess.store(a);
		GraphDatabaseService graphDb = NEO4JAccess.getDB();
		try (Transaction ts = graphDb.beginTx()) {
			Node n = graphDb.getNodeById(id);
			StAbstractMethodNode get = a.getDGNode(n);
			System.out.println(get.id);
		}
	}

}
