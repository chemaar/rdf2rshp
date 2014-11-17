package es.inf.uc3m.kr.rdf2rshp.visitor;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;

import es.inf.uc3m.kr.rdf2rshp.utils.MappingXSDRSHP;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class RDF2RSHPVisitor implements RDFVisitor{

	private static final String DEFAULT_LANGUAGE = "EN";
	private static final String DEFAULT_NOUN = "NOUN";
	private String defaultNamespace;

	
	public RDF2RSHPVisitor(String defaultNamespace){
		this.defaultNamespace = defaultNamespace;
	}

	@Override
	public Object visitBlank(Resource arg0, AnonId arg1) {
		System.out.println("BLANK NODE:"+arg0.getURI()+" "+arg1.getLabelString());
		return null;
	}

	@Override
	public Object visitLiteral(Literal literal) {
		KnowledgeElement ke = new KnowledgeElement();
		ke.setURI(defaultNamespace+literal.hashCode());
		Term term = new Term();
		term.setLexicalForm(literal.getLexicalForm());
		term.setLanguage(literal.getLanguage());
		TermTag tag = new TermTag();
		if(literal.getDatatype()!=null) {
			tag.setValue(literal.getDatatype().getURI());
		}else{
			tag.setValue(DEFAULT_NOUN);
		}
		term.setTag(tag );
		ke.setTerm(term );
		return ke;
	}

	@Override
	public Object visitURI(Resource resource, String arg1) {
		if (resource.getURI() == null) {
			return null;
		}
		KnowledgeElement ke = new KnowledgeElement();
		ke.setURI(resource.getURI());
		Term term = new Term();
		term.setLexicalForm(extractLabelFromURI(resource.getURI()));
		term.setLanguage(DEFAULT_LANGUAGE);
		term.setURI(resource.getURI());
		TermTag tag = new TermTag();
		tag.setValue(DEFAULT_NOUN);
		term.setTag(tag );
		ke.setTerm(term );
		return ke;
	}

	private String extractLabelFromURI(String uri) {
		int indexSlash = uri.lastIndexOf("/");
		int indexSharp = uri.lastIndexOf("#");
		if(indexSlash>indexSharp){
			return uri.substring(indexSlash+1,uri.length());
		}else{
			return uri.substring(indexSharp+1,uri.length());
		}
		
	
	}


}
