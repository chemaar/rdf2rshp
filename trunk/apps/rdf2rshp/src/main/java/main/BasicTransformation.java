package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.riot.RDFFormat;

import com.hp.hpl.jena.graph.NodeVisitor;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import es.inf.uc3m.kr.rdf2rshp.loader.FilesResourceLoader;
import es.inf.uc3m.kr.rdf2rshp.loader.JenaOWLReasonerModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.JenaRDFModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.RDF2RSHPModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.ResourceLoader;
import es.inf.uc3m.kr.rdf2rshp.utils.RDFSyntaxHelper;
import es.inf.uc3m.kr.rdf2rshp.visitor.ArtifactNeo4jVisitor;
import es.inf.uc3m.kr.rdf2rshp.visitor.ArtifactShowVisitor;
import es.inf.uc3m.kr.rdf2rshp.visitor.RDF2RSHPVisitor;
import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Semantics;

public class BasicTransformation {

	static String readFile(String path, Charset encoding) 
			throws IOException 	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}



	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println(RDFFormat.TURTLE.getLang().getName());
		String[] filenames = new String []{"mountain-bike.ttl"};
		ResourceLoader resourceLoader = new FilesResourceLoader(filenames );
		//RDF2RSHPModelWrapper model = new JenaOWLReasonerModelWrapper(resourceLoader,
		//		RDFFormat.TURTLE.getLang().getName(),Boolean.TRUE );
		//OntModel ontModel = (OntModel) model.getModel();
		RDF2RSHPModelWrapper model = new JenaRDFModelWrapper(resourceLoader,
				RDFFormat.TURTLE.getLang().getName() );
		Model rdfModel = (Model) model.getModel();
		
		String defaultNamespace="http://purl.org/krgroup/rshp/example/";
		Artifact artifact= new Artifact();
		artifact.setURI(defaultNamespace);
	
		RDF2RSHPVisitor visitor = new RDF2RSHPVisitor(defaultNamespace);
		StmtIterator stmts = rdfModel.listStatements();
		while(stmts.hasNext()){
			Statement stm = stmts.nextStatement();
			KnowledgeElement from = (KnowledgeElement) stm.getSubject().visitWith(visitor);
			KnowledgeElement predicate = (KnowledgeElement) stm.getPredicate().visitWith(visitor);
			KnowledgeElement to = (KnowledgeElement) stm.getObject().visitWith(visitor);
			RSHP rshp = new RSHP();
			rshp.setFrom(from);
			rshp.setTo(to);
			rshp.setSemantics(new Semantics(predicate));
			rshp.setUri(predicate.getUri());
			artifact.getRHSPs().add(rshp);
		}
//		ArtifactShowVisitor printer = new ArtifactShowVisitor();
//		printer.visit(artifact);
		ArtifactNeo4jVisitor serializer = new ArtifactNeo4jVisitor();
		serializer.visit(artifact);
//		String triples=RDFSyntaxHelper.serializeModel(rdfModel, RDFFormat.RDFXML);
//		System.out.println(triples);
	}

}
