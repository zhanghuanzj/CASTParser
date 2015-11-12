package create;

import java.util.Iterator;
import java.util.Random;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class SearhNodes {
	private static final String DB_PATH = "D:/Program Files/Neo4j Community/data/graph.db";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

		int search_times = 10000000;
		int i = 0;
		Random r = new Random();
		long t2 = System.currentTimeMillis();
		long t3;
		try (Transaction ts = graphDb.beginTx()) {

			Node firstNode = graphDb.createNode();
			firstNode.setProperty("message", "Hello".concat(new Integer(251).toString()));
			firstNode.setProperty("ID", new Integer(250));
			firstNode.addLabel(DynamicLabel.label("node"));
			
			firstNode = graphDb.createNode();
			firstNode.setProperty("message", "Hello".concat(new Integer(252).toString()));
			firstNode.setProperty("ID", new Integer(345));
			firstNode.addLabel(DynamicLabel.label("node"));
			
			Iterator<Node> al_nodes = graphDb.getAllNodes().iterator();
			//Iterator<Node> al_nodes = graphDb.findNodes(DynamicLabel.label("node"));
			t3 = System.currentTimeMillis();
			//System.out.println("Get all nodes. @" + ((t3 - t2) / 1000) + " seconds.");
			
			for (i = 0; i < search_times; i++) {
				try {
					if (!al_nodes.hasNext())
						break;
					Node n = al_nodes.next();
					long id = n.getId();
					//System.out.println("Searching node :" + id + " actual ID:" + n.getProperty("ID"));

					Node search = graphDb.getNodeById(id);
					//System.out.println("Found node :" + search.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		long t4 = System.currentTimeMillis();
		System.out.println("Search for nodes. @" + i + " costs" + ((t4 - t3) / 1000) + " seconds.");

	}

}
