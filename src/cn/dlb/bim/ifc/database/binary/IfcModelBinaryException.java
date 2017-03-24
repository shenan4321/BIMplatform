package cn.dlb.bim.ifc.database.binary;

public class IfcModelBinaryException extends Exception {

	private static final long serialVersionUID = 3947604679506343680L;

	public IfcModelBinaryException(String message, Throwable e) {
		super(message, e);
	}

	public IfcModelBinaryException(String message) {
		super(message);
	}
	
	public IfcModelBinaryException(Throwable e) {
		super(e);
	}
}
