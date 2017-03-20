package cn.dlb.bim.ifc.idm;

public class ObjectIDMException extends Exception {

	private static final long serialVersionUID = -2702814390930893588L;

	public ObjectIDMException(String message) {
		super(message);
	}

	public ObjectIDMException(Exception e) {
		super(e);
	}
}
