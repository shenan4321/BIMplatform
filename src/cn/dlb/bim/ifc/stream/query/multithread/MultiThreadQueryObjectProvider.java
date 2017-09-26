package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.emf.ecore.EClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.MetaDataManager;
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

public class MultiThreadQueryObjectProvider implements ObjectProvider {//TODO 提高吞吐量

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadQueryObjectProvider.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final int MAX_STACK_FRAMES_PROCESSED = 10000;
	private static final int MAX_STACK_SIZE = 100;

	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;

	private final Set<Long> oidsRead = new ConcurrentSkipListSet<>();
	private final Set<Long> goingToRead = new ConcurrentSkipListSet<>();
	private Query query;
	private Integer rid;
	private PackageMetaData packageMetaData;

	private final ThreadPoolExecutor executor;

	private Queue<VirtualObject> virtualObjectStorage = new ConcurrentLinkedQueue<>();

	private Map<RunnableStackFrame, Future<?>> futureMap = new ConcurrentHashMap<>();

	private volatile boolean isDone = false;

	public MultiThreadQueryObjectProvider(ThreadPoolExecutor executor, CatalogService catalogService,
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
		if (!oidsRead.contains(object.getOid())) {
			oidsRead.add(object.getOid());
			virtualObjectStorage.add(object);
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	public MultiThreadQueryObjectProvider copy() throws IOException, QueryException {
		MultiThreadQueryObjectProvider queryObjectProvider = new MultiThreadQueryObjectProvider(executor,
				catalogService, virtualObjectService, query, rid, packageMetaData);
		return queryObjectProvider;
	}

	public static MultiThreadQueryObjectProvider fromJsonNode(ThreadPoolExecutor executor,
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

	public static MultiThreadQueryObjectProvider fromJsonString(ThreadPoolExecutor executor,
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
		return oidsRead.contains(oid);
	}

	public void push(RunnableStackFrame stackFrame) {
		synchronized (futureMap) {
			if (!isDone && stackFrame.getStatus() != Status.DONE) {
				Future<?> future = executor.submit(stackFrame);
				futureMap.put(stackFrame, future);
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
		if (oidsRead.contains(oid)) {
			return true;
		}
		if (goingToRead.contains(oid)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public void removeFuture(RunnableStackFrame stackFrame) {
		synchronized (futureMap) {
			futureMap.remove(stackFrame);
			if (futureMap.isEmpty()) {
				isDone = true;
				synchronized (this) {
					this.notifyAll();
				}
			}
			
		}
		
		
	}

	@Override
	public VirtualObject next() throws DatabaseException, InterruptedException, ExecutionException {
		
		while (!isDone || !virtualObjectStorage.isEmpty()) {
			if (virtualObjectStorage.isEmpty()) {
				synchronized (this) {
					if (!isDone && virtualObjectStorage.isEmpty()) {
						this.wait();
					}
				}
			} else {
				return virtualObjectStorage.poll();
			}
		}
		
		return null;
	}

	/**
	 * 是否完成，未完成将阻塞等待完成
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean isDoneBlock() throws InterruptedException {
		while (!isDone) {
			synchronized (this) {
				this.wait();
			}
		}
		return isDone;
	}

}