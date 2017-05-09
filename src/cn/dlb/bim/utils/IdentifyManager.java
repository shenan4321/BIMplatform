package cn.dlb.bim.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IdentifyManager {
	
	public static final String USER_ID_KEY = "user_id";
	public static final String PID_KEY = "pid_key";
	public static final String PICTURE_KEY = "piture_key";
	
	private static IdentifyManager identifyManager = null;
	
	private final Map<String, IdentifyGenerator> identifyGeneratorContainer = Collections.synchronizedMap(new HashMap<>());
	
	public static IdentifyManager getIdentifyManager() {
		if (identifyManager == null) {
			identifyManager = new IdentifyManager();
		}
		return identifyManager;
	}
	
	public Long nextId(String key) {
		if (!identifyGeneratorContainer.containsKey(key)) {
			identifyGeneratorContainer.put(key, new IdentifyGenerator());
		}
		IdentifyGenerator identifyGenerator = identifyGeneratorContainer.get(key);
		return identifyGenerator.nextId();
	}
	
	public class IdentifyGenerator {

		//2015
		private final long startTime = 1420041600000L;
		
		private final long ONE_STEP = 100;
		private final Lock LOCK = new ReentrantLock();
		private long lastTime = timeMillisFromStartTime();
		private short lastCount = 0;
		private int count = 0;

		@SuppressWarnings("finally")
		public Long nextId() {
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
		
		private long timeMillisFromStartTime() {
			return System.currentTimeMillis() - startTime;
		}

	}
}
