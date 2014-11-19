package es.inf.uc3m.kr.rdf2rshp.dao;



import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;

import es.inf.uc3m.kr.rdf2rshp.to.KENew;
import es.inf.uc3m.kr.rdf2rshp.to.LinkTO;
import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class Neo4jSerializer {

	public static final String PART_OF = "PART_OF";
	private static final int MAX = 1000;
	protected static Logger logger = Logger.getLogger(Neo4jSerializer.class);
	protected static Map<String,List<Long>> cacheKEIds = new HashMap<String,List<Long>>(MAX);

	public enum PROPERTY_LABELS{
		URI,
		LEXICAL_FORM,
		LANGUAGE, FROM, TO, VALUE, HAS_PART
	}
	public enum RelTypes implements RelationshipType{
		ARTIFACT_NODE
	}
	public static long serialize(GraphDatabaseService graphDb,Artifact artifact, long skrNodeId){
		//try ( Transaction tx = graphDb.beginTx() ){
		Node skrNode =  graphDb.getNodeById(skrNodeId);
		Node artifactNode = graphDb.createNode();
		artifactNode.addLabel(DynamicLabel.label(Artifact.class.getSimpleName()));
		artifactNode.setProperty(PROPERTY_LABELS.URI.name(), artifact.getUri());
		skrNode.createRelationshipTo(artifactNode,
				DynamicRelationshipType.withName(PART_OF));
		//tx.success();
		return artifactNode.getId();
		//}
	}
	public static long serialize(GraphDatabaseService graphDb,
			KnowledgeElement ke, long artifactId) {
		try ( Transaction tx = graphDb.beginTx() ){
			Node artifactIdNode =  graphDb.getNodeById(artifactId);
			List<Long> ids = getInternalKEIds(ke.getUri(), graphDb);
			Node keNode;
			if(ids.size()==0){
				keNode = graphDb.createNode();
				keNode.addLabel(DynamicLabel.label(KnowledgeElement.class.getSimpleName()));
				keNode.setProperty(PROPERTY_LABELS.URI.name(), ke.getUri());
			}else{
				keNode = graphDb.getNodeById(ids.get(0));
			}
			artifactIdNode.createRelationshipTo(keNode,
					DynamicRelationshipType.withName(KnowledgeElement.class.getSimpleName()));
			tx.success();
			return keNode.getId();
		}

	}
	public static void serialize(GraphDatabaseService graphDb, 
			RSHP rshp,
			long artifactNodeId, long idFrom, long idTo) {
		try ( Transaction tx = graphDb.beginTx() ){
			Node artifactNode =  graphDb.getNodeById(artifactNodeId);
			Node rshpNode = graphDb.createNode();
			rshpNode.addLabel(DynamicLabel.label(RSHP.class.getSimpleName()));
			rshpNode.setProperty(PROPERTY_LABELS.URI.name(), rshp.getUri());
			Node from = graphDb.getNodeById(idFrom);
			Node to = graphDb.getNodeById(idTo);
			//For usability purposes
			rshpNode.setProperty(PROPERTY_LABELS.FROM.name(), from.getId());
			rshpNode.setProperty(PROPERTY_LABELS.TO.name(), to.getId());
			from.createRelationshipTo(to, DynamicRelationshipType.withName(rshp.getUri()));
			//Link RSHP to the artifact
			artifactNode.createRelationshipTo(rshpNode,DynamicRelationshipType.withName(
					PART_OF));
			tx.success();
		}		
	}

	public static long serialize(GraphDatabaseService graphDb, Term term,
			long keNodeId) {
		try ( Transaction tx = graphDb.beginTx() ){
			Node keNode =  graphDb.getNodeById(keNodeId);
			Node termNode = graphDb.createNode();
			termNode.addLabel(DynamicLabel.label(Term.class.getSimpleName()));
			termNode.setProperty(PROPERTY_LABELS.URI.name(), term.getUri()==null?"null":term.getUri());
			termNode.setProperty(PROPERTY_LABELS.LEXICAL_FORM.name(), term.getLexicalForm());
			termNode.setProperty(PROPERTY_LABELS.LANGUAGE.name(), term.getLanguage()==null?"null":term.getLanguage());
			keNode.createRelationshipTo(termNode,
					DynamicRelationshipType.withName(Term.class.getSimpleName()));
			tx.success();
			return termNode.getId();

		}

	}

	public static void serialize(GraphDatabaseService graphDb, TermTag termTag,
			long currentTermId) {
		try ( Transaction tx = graphDb.beginTx() ){
			Node termNode =  graphDb.getNodeById(currentTermId);
			Node termTagNode = graphDb.createNode();
			termTagNode.addLabel(DynamicLabel.label(TermTag.class.getSimpleName()));
			termTagNode.setProperty(PROPERTY_LABELS.VALUE.name(), termTag.getValue()==null?"":termTag.getValue());
			termNode.createRelationshipTo(termTagNode,
					DynamicRelationshipType.withName(TermTag.class.getSimpleName()));
			tx.success();
		}

	}



	public static List<Long> getInternalKEIds(String uri,GraphDatabaseService graphService){
		List<Long> ids = cacheKEIds.get(uri);
		if(ids==null){
			String query = "match (n:KnowledgeElement) where n.URI={uri} return id(n);";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put( "uri", uri);
			ids = runQuery(graphService,query, params, "id(n)");
			if(cacheKEIds.size()==MAX){
				cacheKEIds.clear();
			}
			if(ids.size()!=0){
				logger.debug("Inserting new KE with ids: "+ids+" uri "+uri);
				cacheKEIds.put(uri, ids);
			}else{
				//logger.debug("NO RESULTS FOUND!!!");
			}

		}else{
			logger.debug("Returning from cache KE: "+ids+" uri "+uri);
		}
		return ids;
	}


	private static List<Long> runQuery(GraphDatabaseService graphDb,String query, Map<String, Object> params,String var) {
		List<Long> internalIds = new LinkedList<Long>();
		ExecutionEngine engine = new ExecutionEngine(graphDb);
		//	try (Transaction tx = graphDb.beginTx()) {
		try{
			logger.debug("Running query: "+query+" and params "+params);
			ExecutionResult result = engine.execute(query, params);
			// extract the data out of the result, you cannot iterate over it outside of a tx
			Iterator<Long> n_column = result.columnAs(var);
			for ( Long id : IteratorUtil.asIterable(n_column ) ) {
				internalIds.add(id);
				id = null;
			}
			n_column.remove();
			n_column = null;
			result = null;
			query = null;
			engine = null;
		}catch(Exception e){
			//logger.error(e);
		}
		//	tx.success();

		//	}
		if(params !=null){
			params.clear();
			params = null;
		}
		var = null;
		return internalIds;
	}
	//Only the node is created
	public static KENew serialize(GraphDatabaseService graphDb,
			KnowledgeElement ke) {
		List<Long> ids = getInternalKEIds(ke.getUri(), graphDb);
		Node keNode = null;
		if(ids.size()==0){
			keNode = graphDb.createNode();
			keNode.addLabel(DynamicLabel.label(KnowledgeElement.class.getSimpleName()));
			keNode.setProperty(PROPERTY_LABELS.URI.name(), ke.getUri());
			return new KENew(Boolean.TRUE, keNode.getId());
		}else{
			return new KENew(ids.get(0));
			
		}
	}

	public static long serialize(GraphDatabaseService graphDb, Term term) {
			Node termNode = graphDb.createNode();
			termNode.addLabel(DynamicLabel.label(Term.class.getSimpleName()));
			termNode.setProperty(PROPERTY_LABELS.URI.name(), term.getUri()==null?"null":term.getUri());
			termNode.setProperty(PROPERTY_LABELS.LEXICAL_FORM.name(), term.getLexicalForm());
			termNode.setProperty(PROPERTY_LABELS.LANGUAGE.name(), term.getLanguage()==null?"null":term.getLanguage());
			return termNode.getId();
	}

	public static long serialize(GraphDatabaseService graphDb, TermTag termTag) {
			Node termTagNode = graphDb.createNode();
			termTagNode.addLabel(DynamicLabel.label(TermTag.class.getSimpleName()));
			termTagNode.setProperty(PROPERTY_LABELS.VALUE.name(), termTag.getValue()==null?"":termTag.getValue());
			return termTagNode.getId();
	}


	public static long serialize(GraphDatabaseService graphDb,	RSHP rshp, long from, long to) {
			Node rshpNode = graphDb.createNode();
			rshpNode.addLabel(DynamicLabel.label(RSHP.class.getSimpleName()));
			rshpNode.setProperty(PROPERTY_LABELS.URI.name(), rshp.getUri());
			//For usability purposes
			rshpNode.setProperty(PROPERTY_LABELS.FROM.name(), from);
			rshpNode.setProperty(PROPERTY_LABELS.TO.name(), to);
			return rshpNode.getId();

			//from.createRelationshipTo(to, DynamicRelationshipType.withName(rshp.getUri()));
			//Link RSHP to the artifact
			//artifactNode.createRelationshipTo(rshpNode,DynamicRelationshipType.withName(PART_OF));
	}
	public static void serialize(GraphDatabaseService graphDb, LinkTO link) {
		Node from = graphDb.getNodeById(link.getFrom());
		Node to = graphDb.getNodeById(link.getTo());
		from.createRelationshipTo(to, DynamicRelationshipType.withName(link.getType()));
	}
	public static void serialize(GraphDatabaseService graphDb, long artifactId,
			long idRSHP) {
		Node from = graphDb.getNodeById(artifactId);
		Node to = graphDb.getNodeById(idRSHP);
		from.createRelationshipTo(to, DynamicRelationshipType.withName(PART_OF));
	}
	
}
