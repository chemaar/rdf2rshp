package es.inf.uc3m.kr.rshp.minimal;

public class Semantics {

	String uri;
	Term term;
	public Semantics(){
		
	}
	public Semantics (KnowledgeElement ke){
		this.uri = ke.uri;
		this.term = ke.term;
	}
	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public void setURI(String uri) {
		this.uri = uri;
		
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return "Semantics [uri=" + uri + ", term=" + term + "]";
	}
	
}
