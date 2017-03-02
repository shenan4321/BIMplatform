package cn.dlb.bim.deserializers;


public abstract class Pass {

	public abstract String process(int lineNumber, String result) throws DeserializeException;
}
