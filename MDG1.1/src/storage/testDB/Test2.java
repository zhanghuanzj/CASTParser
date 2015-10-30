package storage.testDB;

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
 * test DB loads
 * @author think
 *
 */
public class Test2 {
	private static final String DB_PATH = "D:/Program Files/Neo4j Community/data/graph.db";

	private static enum RelTypes implements RelationshipType {
		KNOWS
	};

	public static void main(String[] args) {
		// GraphDatabaseService graphDb = new
		// GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(DB_PATH)
				.setConfig(GraphDatabaseSettings.nodestore_mapped_memory_size, "10000M").newGraphDatabase();
		registerShutdownHook(graphDb);
		long t1 = System.currentTimeMillis();
		int nodes_num = 50000;
		Random r = new Random();
		System.out.println("NEO4J started. @" + nodes_num + " nodes.");

		for (int iter = 0; iter < 1000; iter++) {
			try (Transaction ts = graphDb.beginTx()) {

				for (int i = 0; i < nodes_num; i++) {
					Node firstNode = graphDb.createNode();
					firstNode.setProperty("message", "Hello".concat(new Integer(r.nextInt(1000)).toString()));
					firstNode.setProperty("ID", new Integer(i));

					Label label = DynamicLabel.label("node");
					firstNode.addLabel(label);
					// System.out.println(firstNode.getId());
				}
				ts.success();
				ts.close();
			}
		}
		long t2 = System.currentTimeMillis();
		System.out.println("All nodes generated. @" + ((t2 - t1) / 1000) + " seconds.");
		/**
		 * int search_times = 10000; Random r = new Random(); try (Transaction
		 * ts = graphDb.beginTx()) { for (int i = 0; i < search_times; i++) {
		 * try { Node search = graphDb.getNodeById(r.nextInt(nodes_num * 2));
		 * System.out.println("Found node :" + search.getId()); } catch
		 * (NotFoundException e) { }
		 * 
		 * } } long t3 = System.currentTimeMillis(); System.out.println(
		 * "Search for nodes. @" + search_times + " costs" + ((t3 - t2) / 1000)
		 * + " seconds.");
		 **/
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

}
