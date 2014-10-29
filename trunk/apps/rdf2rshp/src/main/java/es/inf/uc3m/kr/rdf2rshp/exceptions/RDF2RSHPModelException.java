package es.inf.uc3m.kr.rdf2rshp.exceptions;

import java.io.IOException;
import java.net.URISyntaxException;

public class RDF2RSHPModelException extends RuntimeException {

	public RDF2RSHPModelException(IOException e, String string) {
		// TODO Auto-generated constructor stub
		super(e);
	}

	public RDF2RSHPModelException(URISyntaxException e, String string) {
		super(e);
	}

	public RDF2RSHPModelException(Exception e) {
		super(e);
	}

}
