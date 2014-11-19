package es.inf.uc3m.kr.rdf2rshp.visitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jDatabaseBatchConnector;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jDatabaseConnector;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer;
import es.inf.uc3m.kr.rdf2rshp.dao.Neo4jSerializer.PROPERTY_LABELS;
import es.inf.uc3m.kr.rdf2rshp.to.KENew;
import es.inf.uc3m.kr.rdf2rshp.to.LinkTO;
import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Semantics;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class BatchArtifactNeo4jNodeLinkCreatorVisitor extends ArtifactAbstractVisitor{

	private static final int MAX_CACHE = 500000;
	private static final int MAX_RSHPS = 100000;
	protected static Logger logger = Logger.getLogger(BatchArtifactNeo4jNodeLinkCreatorVisitor.class);
	private static GraphDatabaseService graphDb =
			Neo4jDatabaseBatchConnector.getGraphDatabaseService(true);
	private static long skrNodeId = Neo4jDatabaseConnector.getInitialNode();
	private List<RSHP> rshps;
	private int size;
	private List<LinkTO> edges = new LinkedList<LinkTO>();
	private long artifactId;
	protected  Map<String,Long> cacheKEIds = new HashMap<String,Long>(MAX_CACHE);


	public int loadRHSPs(int begin, int end){
		int i = 0;
		for(i=begin;i<=end && i<this.size;i++){
			logger.info("RHSP number: "+i+", nkes="+(i*3));
			this.visit(this.rshps.get(i));
		}
		System.gc();//Release memory
		return (end-begin);

	}
	public Object visit(Artifact artifact) {
		logger.debug("Storing artifact: "+artifact.getUri());
		artifactId = serialize(artifact, skrNodeId);
		batchNodes(artifact);//Create nodes in the database, commit and now links are available
		System.gc();//Release memory
		batchLinks();
		logger.debug("Finishing storing artifact: "+artifact.getUri());
		return null;
	}
	private void batchLinks() {
			for(LinkTO link:this.edges){
				serialize(link);//all unless between artifact y RHSP
			}
	}
	/**
	 * @param artifact
	 */
	public void batchNodes(Artifact artifact) {
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
		long idRSHP = serialize(rshp,idFrom,idTo);
		this.visit(rshp.getSemantics());
		//Create link between artifact y rhsp
		serialize(artifactId, idRSHP);
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
		KENew keNew = serialize(ke);
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
		long termId = serialize(term);
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
		long tagId = serialize(termTag);
		logger.debug("Finishing storing term tag: "+termTag);
		return tagId;
	}

	//New Serializing methods

	private void serialize(long artifactID, long idRSHP) {
		Node from = graphDb.getNodeById(artifactId);
		Node to = graphDb.getNodeById(idRSHP);
		from.createRelationshipTo(to, DynamicRelationshipType.withName(PROPERTY_LABELS.HAS_PART.name()));
	}
	
	public static long serialize(Artifact artifact, long skrNodeId){
		Node skrNode =  graphDb.getNodeById(skrNodeId);
		Node artifactNode = graphDb.createNode();
		artifactNode.addLabel(DynamicLabel.label(Artifact.class.getSimpleName()));
		artifactNode.setProperty(PROPERTY_LABELS.URI.name(), artifact.getUri());
		skrNode.createRelationshipTo(artifactNode,
				DynamicRelationshipType.withName(PROPERTY_LABELS.HAS_PART.name()));
		return artifactNode.getId();
	}
	

	private long serialize(RSHP rshp,long idFrom, long idTo) {
		Node rshpNode = graphDb.createNode();
		rshpNode.addLabel(DynamicLabel.label(RSHP.class.getSimpleName()));
		rshpNode.setProperty(PROPERTY_LABELS.URI.name(), rshp.getUri());
		//For usability purposes
		rshpNode.setProperty(PROPERTY_LABELS.FROM.name(), idFrom);
		rshpNode.setProperty(PROPERTY_LABELS.TO.name(), idTo);
		return rshpNode.getId();
	}
	
	
	private KENew serialize(KnowledgeElement ke) {
		Long id = cacheKEIds.get(ke.getUri());
		Node keNode = null;
		if(id==null){
			keNode = graphDb.createNode();
			keNode.addLabel(DynamicLabel.label(KnowledgeElement.class.getSimpleName()));
			keNode.setProperty(PROPERTY_LABELS.URI.name(), ke.getUri());
			cacheKEIds.put(ke.getUri(),keNode.getId());
			return new KENew(Boolean.TRUE, keNode.getId());
		}else{
			return new KENew(id);
			
		}
	}

	

	private long serialize(Term term) {
		Node termNode = graphDb.createNode();
		termNode.addLabel(DynamicLabel.label(Term.class.getSimpleName()));
		termNode.setProperty(PROPERTY_LABELS.URI.name(), term.getUri()==null?"null":term.getUri());
		termNode.setProperty(PROPERTY_LABELS.LEXICAL_FORM.name(), term.getLexicalForm());
		termNode.setProperty(PROPERTY_LABELS.LANGUAGE.name(), term.getLanguage()==null?"null":term.getLanguage());
		return termNode.getId();
	}
	
	private long serialize(TermTag termTag) {
		Node termTagNode = graphDb.createNode();
		termTagNode.addLabel(DynamicLabel.label(TermTag.class.getSimpleName()));
		termTagNode.setProperty(PROPERTY_LABELS.VALUE.name(), termTag.getValue()==null?"":termTag.getValue());
		return termTagNode.getId();
	}
	
	public static void serialize(LinkTO link) {
		Node from = graphDb.getNodeById(link.getFrom());
		Node to = graphDb.getNodeById(link.getTo());
		from.createRelationshipTo(to, DynamicRelationshipType.withName(link.getType()));
	}
	public Object visit(Semantics semantics) {
		//		System.out.println("\tSemantics[ ");
		//		this.visit(semantics.getTerm());
		//		System.out.println("\t]");
		return null;
	}
}
