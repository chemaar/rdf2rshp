package es.inf.uc3m.kr.rdf2rshp.dao;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserters;


public class Neo4jDatabaseBatchConnector {

	private static final String RSHP_DB = "target/cpv-2008-batch"; 
	//private static final String RSHP_DB = "target/min-cpv-2008-batch"; 
	//private static final String RSHP_DB = "target/mountain-bike-batch";
	private static GraphDatabaseService graphDb;
	private static long rshpNodeId;

	private Neo4jDatabaseBatchConnector(){

	}

	public static GraphDatabaseService getGraphDatabaseService(boolean fromNew){
		Map<String, String> config = new HashMap<String, String>();
		config.put( "neostore.nodestore.db.mapped_memory", "90M" );
//		use_memory_mapped_buffers=true
//				neostore.nodestore.db.mapped_memory=100M
//				neostore.relationshipstore.db.mapped_memory=500M
//				neostore.propertystore.db.mapped_memory=1G
//				neostore.propertystore.db.strings.mapped_memory=200M
//				neostore.propertystore.db.arrays.mapped_memory=0M
//				neostore.propertystore.db.index.keys.mapped_memory=15M
//				neostore.propertystore.db.index.mapped_memory=15M
		FileSystemAbstraction fileSystem = new DefaultFileSystemAbstraction();
		if(graphDb == null){
			if(fromNew) {
				try {
					FileUtils.deleteRecursively( new File( RSHP_DB ) );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			graphDb =
					BatchInserters.batchDatabase( RSHP_DB, 
							fileSystem, config );
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
