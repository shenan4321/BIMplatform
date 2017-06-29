package cn.dlb.bim.ifc.database;

public class DatabaseException extends Exception {

	private static final long serialVersionUID = 3947604679506343680L;

	public DatabaseException(String message, Throwable e) {
		super(message, e);
	}

	public DatabaseException(String message) {
		super(message);
	}
	
	public DatabaseException(Throwable e) {
		super(e);
	}
}
