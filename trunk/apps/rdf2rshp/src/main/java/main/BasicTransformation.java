package main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.riot.RDFFormat;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import es.inf.uc3m.kr.rdf2rshp.loader.FilesResourceLoader;
import es.inf.uc3m.kr.rdf2rshp.loader.JenaOWLReasonerModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.JenaRDFModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.RDF2RSHPModelWrapper;
import es.inf.uc3m.kr.rdf2rshp.loader.ResourceLoader;
import es.inf.uc3m.kr.rdf2rshp.utils.RDFSyntaxHelper;

public class BasicTransformation {

	static String readFile(String path, Charset encoding) 
			throws IOException 	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(RDFFormat.TURTLE.getLang().getName());
		String[] filenames = new String []{"mountain-bike.ttl"};
		ResourceLoader resourceLoader = new FilesResourceLoader(filenames );
		RDF2RSHPModelWrapper model = new JenaOWLReasonerModelWrapper(resourceLoader,
				RDFFormat.TURTLE.getLang().getName(),Boolean.TRUE );
		OntModel ontModel = (OntModel) model.getModel();
		
		String triples=RDFSyntaxHelper.serializeModel(ontModel, RDFFormat.TURTLE);
		System.out.println(triples);
	}

}
