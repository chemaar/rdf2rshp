package es.inf.uc3m.kr.rshp.minimal;

public class Term {

	String uri;
	String lexicalForm;
	String language;
	TermTag tag;
	
	public Term() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Term(String lexicalForm, String language, TermTag tag) {
		super();
		this.lexicalForm = lexicalForm;
		this.language = language;
		this.tag = tag;
	}



	public String getLexicalForm() {
		return lexicalForm;
	}
	public void setLexicalForm(String lexicalForm) {
		this.lexicalForm = lexicalForm;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public TermTag getTag() {
		return tag;
	}
	public void setTag(TermTag tag) {
		this.tag = tag;
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
		return "Term [uri=" + uri + ", lexicalForm=" + lexicalForm
				+ ", language=" + language + ", tag=" + tag + "]";
	}
	
	
	
}
