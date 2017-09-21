package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 用于配合ThreadPoolExecutor使用，实现阻塞的submit
 * @author linfujun
 *
 * @param <E>
 */
public class LimitedQueue<E> extends LinkedBlockingQueue<E> {
	private static final long serialVersionUID = 3342768156404683229L;

	public LimitedQueue(int maxSize) {
	    super(maxSize);
	  }

	  @Override
	  public boolean offer(E e) {//ThreadPoolExecutor的submit内部调用了offer,LinkedBlockingQueue的offer方法是非阻塞的
	    // turn offer() and add() into a blocking calls (unless interrupted)
	    try {
	      put(e);
	      return true;
	    } catch (InterruptedException ie) {
	      Thread.currentThread().interrupt();
	    }
	    return false;
	  }
	}