package main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer.PROPERTY_LABELS;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;

public class BatchInsertMain {

	public static void main(String[] args) {

		Map<String, String> config = new HashMap<String, String>();
		config.put( "neostore.nodestore.db.mapped_memory", "90M" );
		FileSystemAbstraction fileSystem = new DefaultFileSystemAbstraction();
		try {
			FileUtils.deleteRecursively( new File( "target/batchdb-example-config" ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GraphDatabaseService batchDb =
				BatchInserters.batchDatabase( "target/batchdb-example-config", fileSystem, config );
		// Insert data here ... and then shut down:
		
		Node keNode = batchDb.createNode();
		keNode.addLabel(DynamicLabel.label("LABEL"));
		keNode.setProperty(PROPERTY_LABELS.URI.name(), "URI");
		batchDb.shutdown();
		// END SNIPPET: configuredBatchDb

	}

}
