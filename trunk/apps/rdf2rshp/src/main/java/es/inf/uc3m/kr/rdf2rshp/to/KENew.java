package es.inf.uc3m.kr.rdf2rshp.to;

public class KENew {

	boolean isNew;
	long id;
	public boolean isNew() {
		return isNew;
	}
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public KENew(boolean isNew, long id) {
		super();
		this.isNew = isNew;
		this.id = id;
	}
	public KENew() {
		super();
		this.isNew = false;
	}
	
	public KENew(long id) {
		super();
		this.isNew = false;
		this.id = id;
	}
}
