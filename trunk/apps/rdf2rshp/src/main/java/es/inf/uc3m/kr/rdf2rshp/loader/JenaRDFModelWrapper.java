package es.inf.uc3m.kr.rdf2rshp.loader;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import es.inf.uc3m.kr.rdf2rshp.exceptions.RDF2RSHPModelException;
import es.inf.uc3m.kr.rdf2rshp.to.KnowledgeResourcesTO;



public class JenaRDFModelWrapper implements RDF2RSHPModelWrapper{

	private static final Logger logger = Logger.getLogger(JenaRDFModelWrapper.class);

    private ResourceLoader resourceLoader;
    private Model jenaRDFModel;
    private String format = RDFFormat.RDFXML.getLang().getName();
    
    public JenaRDFModelWrapper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    public JenaRDFModelWrapper(ResourceLoader resourceLoader, String format) {
        this.resourceLoader = resourceLoader;
        this.format = format;
    }
    
    
    public synchronized void forceReload() {
        jenaRDFModel = null; // the model will be reloaded on demand
    }
    
    /**
     * @return an instance of Model
     */
    private Model getJenaRDFModel() {   // NOTE: !synchronized
        if (loadJenaRDFModel(this.resourceLoader)) {  
        	logger.debug("Loaded Jena Rdf model");
        }            
        return jenaRDFModel;
    }
    
    
    private synchronized boolean loadJenaRDFModel(ResourceLoader rdfSource)  {
        if(this.jenaRDFModel == null){
            logger.debug("Jena model for is null: creating new Jena Model");
            //Create JenaModel               
            jenaRDFModel = createRDFModel(rdfSource);            
            return true;
        } else {
            return false;
        }        
    }
    
    private Model createRDFModel(ResourceLoader rdfSource) {
        KnowledgeResourcesTO[] rdfResources =  rdfSource.getKnowledgeResources(); 
        Model model =ModelFactory.createDefaultModel();
        logger.debug("Loading " + rdfResources.length  +" resources into the model");
        for ( int i = 0; i< rdfResources.length ; i++ ) {                   
            final InputStream is = rdfResources[i].getKnowledgeSourceData();
            model.read(is, "",this.format);    
            try {
                is.close();
            } catch (IOException e) {
                throw new RDF2RSHPModelException(e, "JenaRDFModelWrapper reading file "+i);
            }
            logger.debug("RDF stream loaded, model contains " + model.size() + " triples.");          
        }        
        return model;
    }


    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
    
    public Object getModel() {
        return getJenaRDFModel();
    }
    
    public void setModel(Model model) {
        this.jenaRDFModel = (Model) model;
    }


}
