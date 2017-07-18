package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.JsonQueryObjectModelConverter;
import cn.dlb.bim.ifc.database.queries.om.Query;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;
import cn.dlb.bim.ifc.emf.MetaDataManager;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
import cn.dlb.bim.service.PlatformService;

public class QueryObjectProvider implements ObjectProvider {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// So far 10000000 has proven to not be enough for some legit IFC files
	private static final int MAX_STACK_FRAMES_PROCESSED = 100000000;
	
	// So far 100000 has proven to not be enough for some legit IFC files
	private static final int MAX_STACK_SIZE = 1000000;
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryObjectProvider.class);
	private PlatformService platformService;
	private PlatformServer server;
	
	private final Set<Long> oidsRead = new HashSet<>();
	private Deque<StackFrame> stack;
	private long start = -1;
	private long reads = 0;
	private long stackFramesProcessed = 0;
	private final Set<Long> goingToRead = new HashSet<>();
	private Query query;
	private StackFrame stackFrame;

	private Integer rid;

	private PackageMetaData packageMetaData;

	public QueryObjectProvider(PlatformService platformService, PlatformServer server, Query query, Integer rid, PackageMetaData packageMetaData) throws IOException, QueryException {
		this.platformService = platformService;
		this.server = server;
		this.query = query;
		this.rid = rid;
		this.packageMetaData = packageMetaData;
		
		stack = new ArrayDeque<StackFrame>();
		stack.push(new QueryStackFrame(this, rid));
		
		for (QueryPart queryPart : query.getQueryParts()) {
			if (queryPart.hasOids()) {
				goingToRead.addAll(queryPart.getOids());
			}
		}
	}

	public QueryObjectProvider copy() throws IOException, QueryException {
		QueryObjectProvider queryObjectProvider = new QueryObjectProvider(platformService, server, query, rid, packageMetaData);
		return queryObjectProvider;
	}
	
	public static QueryObjectProvider fromJsonNode(PlatformService platformService, PlatformServer server, JsonNode fullQuery, Integer rid, PackageMetaData packageMetaData) throws JsonParseException, JsonMappingException, IOException, QueryException {
		if (fullQuery instanceof ObjectNode) {
			JsonQueryObjectModelConverter converter = new JsonQueryObjectModelConverter(packageMetaData);
			Query query = converter.parseJson("query", (ObjectNode) fullQuery);
			return new QueryObjectProvider(platformService, server, query, rid, packageMetaData);
		} else {
			throw new QueryException("Query root must be of type object");
		}
	}
	
	public static QueryObjectProvider fromJsonString(PlatformService platformService, PlatformServer server, String json, Integer rid, PackageMetaData packageMetaData) throws JsonParseException, JsonMappingException, IOException, QueryException {
		return fromJsonNode(platformService, server, OBJECT_MAPPER.readValue(json, ObjectNode.class), rid, packageMetaData);
	}
	
	public Query getQuery() {
		return query;
	}

	@Override
	public VirtualObject next() throws DatabaseException {
		if (start == -1) {
			start = System.nanoTime();
		}
		try {
			while (!stack.isEmpty()) {
				if (stack.size() > MAX_STACK_SIZE) {
					dumpEndQuery();
					throw new DatabaseException("Query stack size > 10000, probably a bug, please report");
				}
				stackFrame = stack.peek();
				if (stackFrame.isDone()) {
					stack.pop();
					continue;
				}
				stackFramesProcessed++;
				if (stackFramesProcessed > MAX_STACK_FRAMES_PROCESSED) {
					dumpEndQuery();
					throw new DatabaseException("Too many stack frames processed ( > " + MAX_STACK_FRAMES_PROCESSED + "), probably a bug, please report");
				}
				boolean done = stackFrame.process();
				stackFrame.setDone(done);
				if (stackFrame instanceof ObjectProvidingStackFrame) {
					VirtualObject currentObject = ((ObjectProvidingStackFrame) stackFrame).getCurrentObject();
					if (currentObject != null) {
						if (!oidsRead.contains(currentObject.getOid())) {
							oidsRead.add(currentObject.getOid());
							return currentObject;
						}
					}
				}
			}
		} catch (Exception e) {
			throw new DatabaseException(e);
		}

		dumpEndQuery();
		
		return null;
	}
	
	public StackFrame getStackFrame() {
		return stackFrame;
	}
	
	private void dumpEndQuery() {
		StackFrame poll = stack.poll();
		int i=0;
		if (poll != null) {
			LOGGER.info("Query dump");
			while (poll != null && i < 20) {
				i++;
				LOGGER.info("\t" + poll.toString());
				poll = stack.poll();
			}
		}
		long end = System.nanoTime();
		LOGGER.debug("Query, " + reads + " reads, " + stackFramesProcessed + " stack frames processed, " + oidsRead.size() + " objects read, " + ((end - start) / 1000000) + "ms");
	}

	public void incReads() {
		reads++;
	}

	public PlatformService getPlatformService() {
		return platformService;
	}

	public MetaDataManager getMetaDataManager() {
		return server.getMetaDataManager();
	}

	public boolean hasRead(long oid) {
		return oidsRead.contains(oid);
	}

	public void push(StackFrame stackFrame) {
		if (!stackFrame.isDone()) {
			stack.push(stackFrame);
		}
	}

	public boolean hasReadOrIsGoingToRead(EClass eClass) {
		for (QueryPart queryPart : query.getQueryParts()) {
			if (queryPart.hasTypes()) {
				if (queryPart.getTypes().contains(eClass)) {
					if (queryPart.getGuids() == null && queryPart.getNames() == null && queryPart.getOids() == null && queryPart.getInBoundingBox() == null && queryPart.getProperties() == null && queryPart.getClassifications() == null) {
						return true;
					}
				}
			} else {
				return (queryPart.getGuids() == null && queryPart.getNames() == null && queryPart.getOids() == null && queryPart.getInBoundingBox() == null && queryPart.getProperties() == null && queryPart.getClassifications() == null);
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
}