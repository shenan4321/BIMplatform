package cn.dlb.bim.ifc.database;

public class IfcModelDbException extends Exception {

	private static final long serialVersionUID = 3947604679506343680L;

	public IfcModelDbException(String message, Throwable e) {
		super(message, e);
	}

	public IfcModelDbException(String message) {
		super(message);
	}
	
	public IfcModelDbException(Throwable e) {
		super(e);
	}
}
