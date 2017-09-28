package cn.dlb.bim.database;

public class DatabaseInitException extends Exception {

	private static final long serialVersionUID = -7160444254695347258L;

	public DatabaseInitException(String message) {
		super(message);
	}

	public DatabaseInitException(Exception e) {
		super(e);
	}
}