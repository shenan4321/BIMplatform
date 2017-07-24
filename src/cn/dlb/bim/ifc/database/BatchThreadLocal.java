package cn.dlb.bim.ifc.database;

import java.lang.reflect.Constructor;

public class BatchThreadLocal<T> extends ThreadLocal<T> {
	/** 参数集合 */
	Object[] obj;
	/** 实例化构造函数 */
	Constructor<T> construct;

	/**
	 * @param clazz 本地变量的class
	 * @param args  构造函数的参数
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BatchThreadLocal(Class clazz, Object... args) throws NoSuchMethodException, SecurityException {
		this.obj = args;
		Class[] clazzs = null;
		/** new 获取参数class供获取构造函数用 */
		if (args != null)
			if (args.length != 0) {
				clazzs = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					clazzs[i] = args[i].getClass();
				}
			}
		this.construct = clazz.getConstructor(clazzs);
	}

	/**
	 * 如果当前线程没有对象创建一个新对象
	 * @return
	 */
	public T newGet() {
		T tar = super.get();
		if (tar == null) {
			try {
				tar = construct.newInstance(obj);
				super.set(tar);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tar;
	}
}
