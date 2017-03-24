package cn.dlb.bim.ifc.database;

public class ModelDbException extends Exception {
	private static final long serialVersionUID = 3947604679506343680L;

	public ModelDbException(String message, Throwable e) {
		super(message, e);
	}

	public ModelDbException(String message) {
		super(message);
	}
	
	public ModelDbException(Throwable e) {
		super(e);
	}
}
