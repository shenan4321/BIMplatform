package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.emf.ecore.EClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.JsonQueryObjectModelConverter;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.multithread.RunnableStackFrame.Status;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class MultiThreadQueryObjectProvider implements ObjectProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadQueryObjectProvider.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;

	private final Set<Long> oidsRead = new HashSet<>();
	private final Set<Long> goingToRead = new HashSet<>();
	private Query query;
	private Integer rid;
	private PackageMetaData packageMetaData;

	private final ThreadPoolTaskExecutor executor;

	private Queue<VirtualObject> virtualObjectStorage = new ConcurrentLinkedQueue<>();
	private ReadWriteLock virtualObjectStorageRwLock = new ReentrantReadWriteLock();

	private Map<RunnableStackFrame, Future<?>> futureMap = new ConcurrentHashMap<>();

	private AtomicBoolean isDone = new AtomicBoolean(false);

	public MultiThreadQueryObjectProvider(ThreadPoolTaskExecutor executor, CatalogService catalogService,
			VirtualObjectService virtualObjectService, Query query, Integer rid,
			PackageMetaData packageMetaData) throws IOException, QueryException {
		this.executor = executor;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.query = query;
		this.rid = rid;
		this.packageMetaData = packageMetaData;

		for (QueryPart queryPart : query.getQueryParts()) {
			if (queryPart.hasOids()) {
				goingToRead.addAll(queryPart.getOids());
			}
		}

		QueryContext queryContext = new QueryContext(catalogService, virtualObjectService, packageMetaData, rid);

		push(new RunnableQueryStackFrame(this, rid, queryContext));

	}

	public void addToStorage(VirtualObject object) throws InterruptedException {
		synchronized (oidsRead) {
			virtualObjectStorageRwLock.writeLock().lock();
			try {
				if (!oidsRead.contains(object.getOid())) {
					oidsRead.add(object.getOid());
					virtualObjectStorage.add(object);
					synchronized (this) {
						this.notifyAll();
					}
				}
			} finally {
				virtualObjectStorageRwLock.writeLock().unlock();
			}
		}
	}

	public MultiThreadQueryObjectProvider copy() throws IOException, QueryException {
		MultiThreadQueryObjectProvider queryObjectProvider = new MultiThreadQueryObjectProvider(executor,
				catalogService, virtualObjectService, query, rid, packageMetaData);
		return queryObjectProvider;
	}

	public static MultiThreadQueryObjectProvider fromJsonNode(ThreadPoolTaskExecutor executor,
			CatalogService catalogService, VirtualObjectService virtualObjectService, PlatformServer server,
			JsonNode fullQuery, Integer rid, PackageMetaData packageMetaData)
			throws JsonParseException, JsonMappingException, IOException, QueryException {
		if (fullQuery instanceof ObjectNode) {
			JsonQueryObjectModelConverter converter = new JsonQueryObjectModelConverter(packageMetaData);
			Query query = converter.parseJson("query", (ObjectNode) fullQuery);
			return new MultiThreadQueryObjectProvider(executor, catalogService, virtualObjectService, query,
					rid, packageMetaData);
		} else {
			throw new QueryException("Query root must be of type object");
		}
	}

	public static MultiThreadQueryObjectProvider fromJsonString(ThreadPoolTaskExecutor executor,
			CatalogService catalogService, VirtualObjectService virtualObjectService, PlatformServer server,
			String json, Integer rid, PackageMetaData packageMetaData)
			throws JsonParseException, JsonMappingException, IOException, QueryException {
		return fromJsonNode(executor, catalogService, virtualObjectService, server,
				OBJECT_MAPPER.readValue(json, ObjectNode.class), rid, packageMetaData);
	}

	public Query getQuery() {
		return query;
	}

	public VirtualObjectService getVirtualObjectService() {
		return virtualObjectService;
	}

	public CatalogService getCatalogService() {
		return catalogService;
	}

	public boolean hasRead(long oid) {
		synchronized (oidsRead) {
			return oidsRead.contains(oid);
		}
	}

	public void push(RunnableStackFrame stackFrame) {
		synchronized (futureMap) {
			synchronized (executor) {
				if (!futureMap.containsKey(stackFrame) && !isDone.get() && stackFrame.getStatus() != Status.DONE) {
					Future<?> future = executor.submit(stackFrame);
					futureMap.put(stackFrame, future);
				} 
			}
		}
	}

	public boolean hasReadOrIsGoingToRead(EClass eClass) {
		for (QueryPart queryPart : query.getQueryParts()) {
			if (queryPart.hasTypes()) {
				if (queryPart.getTypes().contains(eClass)) {
					if (queryPart.getGuids() == null && queryPart.getNames() == null && queryPart.getOids() == null
							&& queryPart.getInBoundingBox() == null && queryPart.getProperties() == null
							&& queryPart.getClassifications() == null) {
						return true;
					}
				}
			} else {
				return (queryPart.getGuids() == null && queryPart.getNames() == null && queryPart.getOids() == null
						&& queryPart.getInBoundingBox() == null && queryPart.getProperties() == null
						&& queryPart.getClassifications() == null);
			}
		}
		return false;
	}

	public boolean hasReadOrIsGoingToRead(Long oid) {
		synchronized (oidsRead) {
			synchronized (goingToRead) {
				if (oidsRead.contains(oid)) {
					return true;
				}
				if (goingToRead.contains(oid)) {
					return true;
				}
				return false;
			}
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public void removeFuture(RunnableStackFrame stackFrame) {
		synchronized (futureMap) {
			futureMap.remove(stackFrame);
			if (futureMap.isEmpty()) {
				isDone.set(true);
				synchronized (this) {
					this.notifyAll();
				}
			}
		}
	}

	@Override
	public VirtualObject next() throws DatabaseException, InterruptedException, ExecutionException {
		virtualObjectStorageRwLock.readLock().lock();
		try {
			while (!isDone.get() || !virtualObjectStorage.isEmpty()) {
				if (virtualObjectStorage.isEmpty()) {
					if (!isDone.get() && virtualObjectStorage.isEmpty()) {
						virtualObjectStorageRwLock.readLock().unlock();
						synchronized (this) {
							this.wait(10);
						}
						virtualObjectStorageRwLock.readLock().lock();
					}
				} else {
					return virtualObjectStorage.poll();
				}
			}
			return null;
		} finally {
			virtualObjectStorageRwLock.readLock().unlock();
		}
	}

	/**
	 * 是否完成，未完成将阻塞等待完成
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean isDoneBlock() throws InterruptedException {
		while (!isDone.get()) {
			synchronized (this) {
				this.wait();
			}
		}
		return isDone.get();
	}

}