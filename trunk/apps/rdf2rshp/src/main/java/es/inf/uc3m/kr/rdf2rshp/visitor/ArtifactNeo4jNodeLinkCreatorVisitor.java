package es.inf.uc3m.kr.rdf2rshp.visitor;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.hp.hpl.jena.util.OneToManyMap.Entry;

import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jDatabaseConnector;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer;
import es.inf.uc3m.kr.rdf2rshp.to.KENew;
import es.inf.uc3m.kr.rdf2rshp.to.LinkTO;
import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Semantics;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class ArtifactNeo4jNodeLinkCreatorVisitor extends ArtifactAbstractVisitor{

	private static int MAX_RSHPS=10000;
	protected static Logger logger = Logger.getLogger(ArtifactNeo4jNodeLinkCreatorVisitor.class);
	private static GraphDatabaseService graphDb = Neo4jDatabaseConnector.getGraphDatabaseService(true);
	private static long skrNodeId = Neo4jDatabaseConnector.getInitialNode();
	private List<RSHP> rshps;
	private int size;
	private List<LinkTO> edges = new LinkedList<LinkTO>();
	private long artifactId;

	public int loadRHSPs(int begin, int end){
		try ( Transaction tx = graphDb.beginTx() ){
			int i = 0;
			for(i=begin;i<=end && i<this.size;i++){
				logger.info("RHSP number: "+i+", nkes="+(i*3));
				this.visit(this.rshps.get(i));
			}
			tx.success();
			System.gc();//Release memory
			return (end-begin);
			
		}
	}
	public Object visit(Artifact artifact) {
		//BE CAREFUL!
		try ( Transaction tx = graphDb.beginTx() ){
			logger.debug("Storing artifact: "+artifact.getUri());
			artifactId = Neo4jSerializer.serialize(graphDb, artifact, skrNodeId);
			tx.success();
		}
		batch(artifact);//Create nodes in the database, commit and now links are available
		System.gc();//Release memory
		batchLinks();
		logger.debug("Finishing storing artifact: "+artifact.getUri());

		return null;
	}
	private void batchLinks() {
		try ( Transaction tx = graphDb.beginTx() ){
			for(LinkTO link:this.edges){
				Neo4jSerializer.serialize(graphDb, link);//all unless between artifact y RHSP
			}
			tx.success();
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
		logger.debug("Storing RHSP: "+rshp.getUri());
		long idFrom = (long) this.visit(rshp.getFrom());
		long idTo = (long) this.visit(rshp.getTo());
		long idRSHP = Neo4jSerializer.serialize(graphDb, rshp,idFrom,idTo);
		this.visit(rshp.getSemantics());
		//Create link between artifact y rhsp
		Neo4jSerializer.serialize(graphDb, artifactId, idRSHP);
		logger.debug("Finishing storing RHSP: "+rshp.getUri());
		//Create links
		LinkTO linkTO = new LinkTO();
		linkTO.setFrom(idRSHP);
		linkTO.setTo(idFrom);
		linkTO.setType(Neo4jSerializer.PROPERTY_LABELS.FROM.name());
		this.edges.add(linkTO);
		LinkTO linkTO2 = new LinkTO();
		linkTO2.setFrom(idRSHP);
		linkTO2.setTo(idTo);
		linkTO2.setType(Neo4jSerializer.PROPERTY_LABELS.TO.name());
		this.edges.add(linkTO2);
		return null;
	}

	public Object visit(KnowledgeElement ke) {
		logger.debug("Storing KE: "+ke.getUri());
		KENew keNew = Neo4jSerializer.serialize(graphDb, ke);
		long keId = keNew.getId();
		if(keNew.isNew()){
			//Create link if it was not previously created this KE
			long termId=(long) this.visit(ke.getTerm());
			LinkTO linkTO = new LinkTO();
			linkTO.setFrom(keId);
			linkTO.setTo(termId);
			linkTO.setType(Term.class.getSimpleName());
			this.edges.add(linkTO);
		}
		logger.debug("Finishing storing KE: "+ke.getUri());
		return keId;
	}
	


	public Object visit(Term term) {
		logger.debug("Storing term: "+term);
		long termId = Neo4jSerializer.serialize(graphDb, term);
		long tagId= (long) this.visit(term.getTag());
		logger.debug("Finishing storing term: "+term.getUri());
		LinkTO linkTO = new LinkTO();
		linkTO.setFrom(termId);
		linkTO.setTo(tagId);
		linkTO.setType(TermTag.class.getSimpleName());
		this.edges.add(linkTO);
		return termId;
	}

	public Object visit(TermTag termTag) {
		logger.debug("Storing term tag: "+termTag);
		long tagId = Neo4jSerializer.serialize(graphDb, termTag);
		logger.debug("Finishing storing term tag: "+termTag);
		return tagId;
	}



	public Object visit(Semantics semantics) {
		//		System.out.println("\tSemantics[ ");
		//		this.visit(semantics.getTerm());
		//		System.out.println("\t]");
		return null;
	}
}
