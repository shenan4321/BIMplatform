package cn.dlb.bim.action;

public interface ActionStateListener {
	public void onStarted(LongAction longAction);
	public void onError(LongAction longAction);
	public void onFinished(LongAction longAction);
	public void onUpdate(LongAction longAction);
}
