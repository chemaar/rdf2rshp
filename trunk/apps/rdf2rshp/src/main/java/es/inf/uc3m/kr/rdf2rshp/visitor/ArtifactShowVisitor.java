package es.inf.uc3m.kr.rdf2rshp.visitor;

import es.inf.uc3m.kr.rshp.minimal.Artifact;
import es.inf.uc3m.kr.rshp.minimal.KnowledgeElement;
import es.inf.uc3m.kr.rshp.minimal.RSHP;
import es.inf.uc3m.kr.rshp.minimal.Semantics;
import es.inf.uc3m.kr.rshp.minimal.Term;
import es.inf.uc3m.kr.rshp.minimal.TermTag;

public class ArtifactShowVisitor extends ArtifactAbstractVisitor{

	public Object visit(Artifact artifact) {
		System.out.println("ARTIFACT{");
		for(RSHP rshp:artifact.getRHSPs()){
			this.visit(rshp);
		}
		System.out.println("}");
		return null;
	}

	public Object visit(RSHP rshp) {
		System.out.println("RHSP[");
		this.visit(rshp.getFrom());
		this.visit(rshp.getTo());
		this.visit(rshp.getSemantics());
		System.out.println("]");
		return null;
	}

	public Object visit(KnowledgeElement ke) {
		System.out.println("\tKE[ ");
		this.visit(ke.getTerm());
		System.out.println("\t]");
		return null;
	}
	
	public Object visit(Term term) {
		System.out.println("\t\t"+term);
		this.visit(term.getTag());
		return null;
	}
	
	public Object visit(TermTag termTag) {
		System.out.println("\t\t\t"+termTag);
		return null;
	}
	
	
	
	public Object visit(Semantics semantics) {
		System.out.println("\tSemantics[ ");
		this.visit(semantics.getTerm());
		System.out.println("\t]");
		return null;
	}
}
