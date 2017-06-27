package cn.dlb.bim.vo;

public interface ITransformer <T> {
	public void transformFrom(T origin);
	public T transformTo();
}
