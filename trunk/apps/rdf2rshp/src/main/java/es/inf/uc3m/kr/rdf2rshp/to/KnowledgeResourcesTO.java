package es.inf.uc3m.kr.rdf2rshp.to;

import java.io.InputStream;

public class KnowledgeResourcesTO {

	 private InputStream knowledgeSourceData;

	 
	public KnowledgeResourcesTO(InputStream knowledgeSourceData) {
		super();
		this.knowledgeSourceData = knowledgeSourceData;
	}

	public KnowledgeResourcesTO() {
		// TODO Auto-generated constructor stub
	}

	public InputStream getKnowledgeSourceData() {
		return knowledgeSourceData;
	}

	public void setKnowledgeSourceData(InputStream knowledgeSourceData) {
		this.knowledgeSourceData = knowledgeSourceData;
	}
	 
}
