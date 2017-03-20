package cn.dlb.bim.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IdentifyUtil {

	/** 开始时间截 (2015-01-01) */
	private static final long startTime = 1420041600000L;
	
	private static final long ONE_STEP = 100;
	private static final Lock LOCK = new ReentrantLock();
	private static long lastTime = timeMillisFromStartTime();
	private static short lastCount = 0;
	private static int count = 0;

	@SuppressWarnings("finally")
	public static Long nextId() {
		LOCK.lock();
		try {
			if (lastCount == ONE_STEP) {
				boolean done = false;
				while (!done) {
					long now = timeMillisFromStartTime();
					if (now == lastTime) {
						try {
							Thread.currentThread();
							Thread.sleep(1);
						} catch (java.lang.InterruptedException e) {
						}
						continue;
					} else {
						lastTime = now;
						lastCount = 0;
						done = true;
					}
				}
			}
			count = lastCount++;
		} finally {
			LOCK.unlock();
			return Long.valueOf(lastTime + "" + String.format("%03d", count));
		}
	}
	
	private static long timeMillisFromStartTime() {
		return System.currentTimeMillis() - startTime;
	}

	public static void main(String[] args) {
		// 测试
		for (int i = 0; i < 1000; i++) {
			System.out.println(nextId());
		}
	}
}
