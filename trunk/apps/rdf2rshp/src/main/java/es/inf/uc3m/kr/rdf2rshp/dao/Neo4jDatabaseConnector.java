package es.inf.uc3m.kr.rdf2rshp.dao;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;


public class Neo4jDatabaseConnector {


	private static final String RSHP_DB = "target/rshp-test-nc-db-2"; //FIXME: Extract
	private static GraphDatabaseService graphDb;
	private static long rshpNodeId;

	private Neo4jDatabaseConnector(){

	}

	public static GraphDatabaseService getGraphDatabaseService(boolean fromNew){
		if(graphDb == null){
			if(fromNew) {
				try {
					FileUtils.deleteRecursively( new File( RSHP_DB ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( RSHP_DB );
			createInitialNode();
			registerShutdownHook();
		}
		return graphDb;
	}

	public static long getInitialNode(){
		return rshpNodeId;
	}
	
	private static void createInitialNode() {
		try ( Transaction tx = graphDb.beginTx() ){
			Node skrNodeId = graphDb.createNode();
			rshpNodeId = skrNodeId.getId();
			tx.success();
		}

	}

	public static GraphDatabaseService getGraphDatabaseService(){
		if(graphDb == null){
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( RSHP_DB );
			createInitialNode();
			registerShutdownHook();
		}
		return graphDb;
	}

	public static void returnGraphDatabaseService(GraphDatabaseService service){
		service.shutdown();
	}
	private  static void registerShutdownHook()	{
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime()
		.addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}




}
