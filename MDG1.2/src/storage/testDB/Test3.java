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
/**
 * DB support "String[]"
 * @author think
 *
 */
public class Test3 {
	private static final String DB_PATH = "D:/Program Files/Neo4j Community/data/graph.db";

	private static enum RelTypes implements RelationshipType {
		KNOWS
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// GraphDatabaseService graphDb = new
		// GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(DB_PATH)
				.setConfig(GraphDatabaseSettings.nodestore_mapped_memory_size, "10000M").newGraphDatabase();
		registerShutdownHook(graphDb);
		long id = -1;
		try (Transaction ts = graphDb.beginTx()) {
			Node firstNode = graphDb.createNode();
			id = firstNode.getId();
			List<Class> Parameter = new ArrayList<Class>();
			Parameter.add(Class.forName("java.util.ArrayList"));
			Parameter.add(Class.forName("create.TestElmts"));
			List<String> Parameter2 = new ArrayList<String>();
			Parameter2.add("java.util.ArrayList");
			Parameter2.add("create.TestElmts");
			String[] Parameter3 = new String[2];
			Parameter3[0]="java.util.ArrayList";
			Parameter3[1]="create.TestElmts";
			firstNode.setProperty("para1", Parameter3);
			Class[] Parameter4 = new Class[2];
			Parameter4[0]=Class.forName("java.util.ArrayList");
			Parameter4[1]=Class.forName("create.TestElmts");
			
			boolean[] Parameter5 = new boolean[2];			
			firstNode.setProperty("para2", Parameter5);
			// System.out.println(firstNode.getId());
			ts.success();
			ts.close();
		}
		try (Transaction ts = graphDb.beginTx()) {
			Node n = graphDb.getNodeById(id);
			boolean[] a = (boolean[]) n.getProperty("para2");
			System.out.println(a[0]);
			ts.success();
			ts.close();
		}
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

}
