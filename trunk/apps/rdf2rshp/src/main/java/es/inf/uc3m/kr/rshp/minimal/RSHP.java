package es.inf.uc3m.kr.rshp.minimal;

public class RSHP {

	String uri;
	KnowledgeElement from;
	KnowledgeElement to;
	Semantics semantics;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public KnowledgeElement getFrom() {
		return from;
	}
	public void setFrom(KnowledgeElement from) {
		this.from = from;
	}
	public KnowledgeElement getTo() {
		return to;
	}
	public void setTo(KnowledgeElement to) {
		this.to = to;
	}
	public Semantics getSemantics() {
		return semantics;
	}
	public void setSemantics(Semantics semantics) {
		this.semantics = semantics;
	}
	@Override
	public String toString() {
		return "RSHP [uri=" + uri + ", from=" + from + ", to=" + to
				+ ", semantics=" + semantics + "]";
	}
	
	
}
