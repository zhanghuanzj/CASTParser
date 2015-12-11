package storage.testDB;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
/**
 * How to run database
 * @author think
 *
 */
public class Test1 {
	private static final String DB_PATH = "D:/Program Files/Neo4j Community/data/graph.db";
	private static enum RelTypes implements RelationshipType {
		KNOWS   };
	public static void main(String[] args) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		registerShutdownHook(graphDb);
		try(Transaction ts = graphDb.beginTx()){
			Node firstNode = graphDb.createNode();
			Node secondNode = graphDb.createNode();
			Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);

			firstNode = graphDb.createNode();
			firstNode.setProperty("message", "Hello, ");
			secondNode = graphDb.createNode();   
			secondNode.setProperty("message", "World!"); // Ò²ÓÐgetProperty
			relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
			relationship.setProperty("message", "hurry up");

			System.out.println(firstNode.getId());
			System.out.println(secondNode.getId());
			
			firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING ).delete();
			firstNode.delete();
			secondNode.delete();
		};
		//System.out.println(graphDb.toString());
		
		graphDb.shutdown();

	}
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
{
    // Registers a shutdown hook for the Neo4j instance so that it
    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
    // running application).
    Runtime.getRuntime().addShutdownHook( new Thread()
    {
        @Override
        public void run()
        {
            graphDb.shutdown();
        }
    } );
}

}
