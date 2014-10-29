package es.inf.uc3m.kr.rdf2rshp.loader;



import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
//import org.mindswap.pellet.jena.PelletReasonerFactory;




















import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.OWLMicroReasoner;
import com.hp.hpl.jena.reasoner.rulesys.OWLMicroReasonerFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

import es.inf.uc3m.kr.rdf2rshp.exceptions.RDF2RSHPModelException;
import es.inf.uc3m.kr.rdf2rshp.to.KnowledgeResourcesTO;




/**
 *
 * This class implements the interface QuodModelWrapper loading (with a reasoner) the data from a file and
 * returning a Jena OntoModel.
 *
 */
public class JenaOWLReasonerModelWrapper implements RDF2RSHPModelWrapper {
  

    private static final Logger logger = Logger.getLogger(JenaOWLReasonerModelWrapper.class);

    private ResourceLoader resourceLoader;    
    private OntModel jenaOWLModel;
    private String format = RDFFormat.RDFXML.getLang().getName();
    private boolean inferred = Boolean.FALSE;
    
    
    public JenaOWLReasonerModelWrapper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    

    public JenaOWLReasonerModelWrapper(ResourceLoader resourceLoader, String format, boolean inferred) {
        this.resourceLoader = resourceLoader;
        this.format = format;
        this.inferred = inferred;
    }
    
    private Model getJenaOWLModel() {   // NOTE: !synchronized
        if (loadJenaOWLModel(this.resourceLoader)) {  
        	  logger.debug("Loaded Jena Owl (with reasoning) model");
        }            
        return jenaOWLModel;
    }
    
    
    private synchronized boolean loadJenaOWLModel(ResourceLoader rdfSource)  {
        if(this.jenaOWLModel == null){
            logger.debug("Jena model for is null: creating new Jena Model");
            //Create JenaModel               
            jenaOWLModel = createJenaOWLModel(rdfSource);
            return true;
        } else {
            return false;
        }        
    }
    
    private OntModel createJenaOWLModel(ResourceLoader owlSource){
        KnowledgeResourcesTO[] owlsources = owlSource.getKnowledgeResources();
        Reasoner reasoner = null; //FIXME: PelletReasonerFactory.theInstance().create();
        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setProcessImports(false);
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM);
        spec.setDocumentManager(dm);        
       // spec.setReasoner(new OWLMicroReasoner(OWLMicroReasonerFactory.theInstance()));
        //spec.setReasoner(reasoner);
        final OntModel ontModel = ModelFactory.createOntologyModel( spec, null );
        logger.debug("Loading with reasoner " + owlsources.length  +" resources into the model");
        for ( int i = 0; i< owlsources.length ; i++ ) {                   
            final InputStream is = owlsources[i].getKnowledgeSourceData();
           ontModel.read(is,"", this.format);   
        	  
            try {
                is.close();
            } catch (IOException e) {
                throw new RDF2RSHPModelException(e, "JenaOWLReasonerModelWrapper reading " + i);
            }
        }
      //Inferred models
        if(inferred){
        	  InfModel pModel = ModelFactory.createInfModel(new OWLMicroReasoner(OWLMicroReasonerFactory.theInstance()), 
              		ontModel);
              ExtendedIterator<Statement> stmts = pModel.listStatements().filterDrop( 
              		new Filter<Statement>() {
                  @Override
                  public boolean accept(Statement o) {
                      return ontModel.contains( o );
                  }
              });
              Model deductions = ModelFactory.createDefaultModel().add( 
              		new StmtIteratorImpl( stmts ));
              ontModel.add(deductions);
        }
      
        return ontModel;
    }


    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
    
    public void setOntologySource(ResourceLoader ontologySource) {
        this.resourceLoader = ontologySource;
    }
    
    public Object getModel() {
        return getJenaOWLModel();
    }
    
    public void setModel(Model model) {
        this.jenaOWLModel = (OntModel) model;
    }
    
}