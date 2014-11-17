package es.inf.uc3m.kr.rshp.minimal;

import java.util.LinkedList;
import java.util.List;

public class Artifact extends KnowledgeElement {

	List<RSHP> rshps;

	public Artifact(){
		this.rshps = new LinkedList<RSHP>();
	}
	public List<RSHP> getRHSPs() {
		if(this.rshps==null){
			this.rshps = new LinkedList<RSHP>();
		}
		return rshps;
	}

	public void setRHSPs(List<RSHP> rshps) {
		this.rshps = rshps;
	}
	@Override
	public String toString() {
		return "Artifact [rshps=" + rshps + ", toString()=" + super.toString()
				+ "]";
	}
	
}
