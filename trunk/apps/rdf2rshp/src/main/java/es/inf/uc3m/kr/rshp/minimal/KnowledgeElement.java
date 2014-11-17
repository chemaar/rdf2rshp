package es.inf.uc3m.kr.rshp.minimal;

public class KnowledgeElement {

	String uri;
	Term term;

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


	@Override
	public String toString() {
		return "KnowledgeElement [uri=" + uri + ", term=" + term + "]";
	}
	
	
}
