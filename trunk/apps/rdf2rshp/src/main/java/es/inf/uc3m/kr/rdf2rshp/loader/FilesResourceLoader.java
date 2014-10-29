package es.inf.uc3m.kr.rdf2rshp.loader;




import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;




import es.inf.uc3m.kr.rdf2rshp.exceptions.ResourceNotFoundException;
import es.inf.uc3m.kr.rdf2rshp.to.KnowledgeResourcesTO;




/**
 *
 * This class implements the interface ResourceLoader loading the data
 * from a file referenced by a name of the current classpath.
 *
 */
public class FilesResourceLoader  implements ResourceLoader {

	private static final Logger logger = Logger.getLogger(FilesResourceLoader.class);

	private String []resourceNames;

	public FilesResourceLoader(String[] filenames) {
		this.resourceNames = filenames;
	}
	public FilesResourceLoader(List <String>filenames) {
		this.resourceNames = filenames.toArray(new String[filenames.size()]);
	}

	protected InputStream openInputStream(String filename) throws FileNotFoundException {
		logger.debug("Opening resource input stream for filename: " + filename);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(filename);
		if (in == null) {
			logger.error("Resource file not found: " + filename);
			throw new FileNotFoundException(filename);
		} else {
			return in;
		}
	}

	public KnowledgeResourcesTO[] getKnowledgeResources() {
		Collection<KnowledgeResourcesTO> ontologies = new LinkedList<KnowledgeResourcesTO>();
		String file = "";
		try {
			for (int i = 0 ;i< resourceNames.length;i++) {
				file =  resourceNames[i]; 
				KnowledgeResourcesTO resource = new KnowledgeResourcesTO(
						(this.openInputStream(file)));
				ontologies.add(resource);
			}
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(e, "Resource Files getting resource file:  "+file);
		}
		return  ontologies.toArray(new KnowledgeResourcesTO[ontologies.size()]);
	}


	public String getKnowledgeResourceAsString(String filename)
			throws ResourceNotFoundException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}





}