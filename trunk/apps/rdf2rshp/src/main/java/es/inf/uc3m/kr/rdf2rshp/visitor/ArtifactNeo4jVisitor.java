package es.inf.uc3m.kr.rdf2rshp.visitor;

import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jDatabaseConnector;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer;
import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Semantics;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class ArtifactNeo4jVisitor extends ArtifactAbstractVisitor{

	private static int MAX_RSHPS=10000;
	protected static Logger logger = Logger.getLogger(ArtifactNeo4jVisitor.class);
	private static GraphDatabaseService graphDb = Neo4jDatabaseConnector.getGraphDatabaseService(true);
	private static long skrNodeId = Neo4jDatabaseConnector.getInitialNode();
	private long artifactNodeId;
	private long currentKeId;
	private long currentTermId;
	private long nkes = 0;
	private long nrhsps = 0;
	private List<RSHP> rshps;
	private int size;

	public int loadRHSPs(int begin, int end){
		try ( Transaction tx = graphDb.beginTx() ){
			int i = 0;
			for(i=begin;i<=end && i<this.size;i++){
				logger.info("RHSP number: "+i+", nkes="+(i*3));
				this.visit(this.rshps.get(i));
			}
			tx.success();
			return (end-begin);
		}
	}
	public Object visit(Artifact artifact) {
		//BE CAREFUL!
		try ( Transaction tx = graphDb.beginTx() ){
			logger.debug("Storing artifact: "+artifact.getUri());
			artifactNodeId = Neo4jSerializer.serialize(graphDb, artifact, skrNodeId);
			tx.success();
		}
		//batch(artifact);
		noBatch(artifact);
		logger.debug("Finishing storing artifact: "+artifact.getUri());

		return null;
	}
	private void noBatch(Artifact artifact) {
		for(RSHP rshp:artifact.getRHSPs()){
			this.visit(rshp);
		}
		
	}
	/**
	 * @param artifact
	 */
	public void batch(Artifact artifact) {
		this.rshps = artifact.getRHSPs();
		this.size = rshps.size();
		for(int i=0;i<size;){
			int loaded = loadRHSPs(i,i+MAX_RSHPS);
			i=i+loaded+1;
		}
	}

	public Object visit(RSHP rshp) {
		logger.debug("Storing RHSP: "+rshp.getUri()+", nrshps: "+(nrhsps++)+", nKEs: "+(nkes+=3)+", nTerms: "+(nkes+3)+", nTermTags: "+(nkes+6));
		long idFrom = (long) this.visit(rshp.getFrom());
		long idTo = (long) this.visit(rshp.getTo());
		this.visit(rshp.getSemantics());
		Neo4jSerializer.serialize(graphDb, rshp, this.artifactNodeId, idFrom, idTo);
		logger.debug("Finishing storing RHSP: "+rshp.getUri());
		return null;
	}

	public Object visit(KnowledgeElement ke) {
		logger.debug("Storing KE: "+ke.getUri());
		long id = Neo4jSerializer.serialize(graphDb, ke, this.artifactNodeId);
		this.currentKeId = id;
		this.visit(ke.getTerm());
		logger.debug("Finishing storing KE: "+ke.getUri());
		return id;
	}

	public Object visit(Term term) {
		logger.debug("Storing term: "+term);
		long id = Neo4jSerializer.serialize(graphDb, term, this.currentKeId);
		this.currentTermId = id; 
		this.visit(term.getTag());
		logger.debug("Finishing storing term: "+term.getUri());
		return null;
	}

	public Object visit(TermTag termTag) {
		logger.debug("Storing term tag: "+termTag);
		Neo4jSerializer.serialize(graphDb, termTag, this.currentTermId);
		logger.debug("Finishing storing term tag: "+termTag);
		return null;
	}



	public Object visit(Semantics semantics) {
		//		System.out.println("\tSemantics[ ");
		//		this.visit(semantics.getTerm());
		//		System.out.println("\t]");
		return null;
	}
}
