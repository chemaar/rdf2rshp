package es.inf.uc3m.kr.rdf2rshp.loader;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.util.FileUtils;

import es.inf.uc3m.kr.rdf2rshp.exceptions.RDF2RSHPModelException;
import es.inf.uc3m.kr.rdf2rshp.exceptions.ResourceNotFoundException;
import es.inf.uc3m.kr.rdf2rshp.to.KnowledgeResourcesTO;

/**
 *
 * This class implements the interface ResourceLoader loading the data
 * from a String.
 *
 */
public class StringResourceLoader  implements ResourceLoader {

	private static final Logger logger = Logger.getLogger(StringResourceLoader.class);

	private String content;

	public StringResourceLoader(String content) {
		this.content = content;
	}



	public KnowledgeResourcesTO[] getKnowledgeResources() {
		KnowledgeResourcesTO knowledgeResourcesTO = new KnowledgeResourcesTO();
		InputStream knowledgeSourceData = null;
		try {
			knowledgeSourceData = new ByteArrayInputStream(content.getBytes("UTF-8"));
			knowledgeResourcesTO.setKnowledgeSourceData(knowledgeSourceData);
			return  new KnowledgeResourcesTO[]{knowledgeResourcesTO};
		} catch (UnsupportedEncodingException e) {
			throw new RDF2RSHPModelException(e);
		}
	
	}





	public String getKnowledgeResourceAsString(String filename)
			throws ResourceNotFoundException {
		try {
			return FileUtils.readWholeFileAsUTF8(filename);
		} catch (IOException e) {
			throw new ResourceNotFoundException(e);
		}
	}





}