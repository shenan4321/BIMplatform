//package cn.dlb.bim.ifc.stream;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.DoubleBuffer;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.eclipse.emf.ecore.EClass;
//import org.eclipse.emf.ecore.EStructuralFeature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.util.CloseableIterator;
//
//import cn.dlb.bim.component.PlatformServer;
//import cn.dlb.bim.ifc.database.DatabaseException;
//import cn.dlb.bim.ifc.emf.PackageMetaData;
//import cn.dlb.bim.ifc.emf.Schema;
//import cn.dlb.bim.ifc.engine.EntityNotFoundException;
//import cn.dlb.bim.ifc.engine.IRenderEngine;
//import cn.dlb.bim.ifc.engine.IRenderEngineInstance;
//import cn.dlb.bim.ifc.engine.IRenderEngineModel;
//import cn.dlb.bim.ifc.engine.IndexFormat;
//import cn.dlb.bim.ifc.engine.Precision;
//import cn.dlb.bim.ifc.engine.RenderEngineException;
//import cn.dlb.bim.ifc.engine.RenderEngineFilter;
//import cn.dlb.bim.ifc.engine.RenderEngineGeometry;
//import cn.dlb.bim.ifc.engine.RenderEngineSettings;
//import cn.dlb.bim.ifc.engine.cells.GenerateGeometryResult;
//import cn.dlb.bim.ifc.engine.cells.Matrix;
//import cn.dlb.bim.ifc.engine.pool.RenderEnginePool;
//import cn.dlb.bim.ifc.model.IfcHeader;
//import cn.dlb.bim.ifc.stream.serializers.IfcStepStreamingSerializer;
//import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
//import cn.dlb.bim.ifc.stream.serializers.OidConvertingSerializer;
//import cn.dlb.bim.ifc.stream.serializers.SimpleObjectProvider;
//import cn.dlb.bim.ifc.stream.serializers.StreamingSerializer;
//import cn.dlb.bim.models.geometry.GeometryPackage;
//
//public class StreamingGeometryGenerator extends GenericGeometryGenerator {
//	
//	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";
//	private static String IFC4_SCHEMA_SHORT = "IFC4";
//	
//	private static final Logger LOGGER = LoggerFactory.getLogger(StreamingGeometryGenerator.class);
//	
//	private final Map<Integer, Long> hashes = new ConcurrentHashMap<>();
//
//	private final PlatformServer server;
//	private EClass productClass;
//	private EStructuralFeature geometryFeature;
//	private EStructuralFeature representationFeature;
//	private PackageMetaData packageMetaData;
//
//	private AtomicLong bytesSaved = new AtomicLong();
//	private AtomicLong totalBytes = new AtomicLong();
//	private AtomicLong saveableColorBytes = new AtomicLong();
//
//	private AtomicInteger jobsDone = new AtomicInteger();
//	private AtomicInteger jobsTotal = new AtomicInteger();
//
//	private volatile boolean allJobsPushed;
//	private volatile Integer rid = -1;
//
//	private int maxObjectsPerFile = 10;
//	private volatile boolean running = true;
//
//	public StreamingGeometryGenerator(final PlatformServer server, Integer rid) {
//		this.server = server;
//		this.rid = rid;
//	}
//	
//	public class Runner implements Runnable {
//
//		private EClass eClass;
//		private RenderEngineSettings renderEngineSettings;
//		private RenderEngineFilter renderEngineFilter;
//		private GenerateGeometryResult generateGeometryResult;
//		private ObjectProvider objectProvider;
//		private RenderEnginePool renderEnginePool;
//
//		public Runner(EClass eClass, RenderEnginePool renderEnginePool, RenderEngineSettings renderEngineSettings, ObjectProvider objectProvider, RenderEngineFilter renderEngineFilter, GenerateGeometryResult generateGeometryResult) {
//			this.eClass = eClass;
//			this.renderEnginePool = renderEnginePool;
//			this.renderEngineSettings = renderEngineSettings;
//			this.objectProvider = objectProvider;
//			this.renderEngineFilter = new RenderEngineFilter(true);
//			this.generateGeometryResult = generateGeometryResult;
//		}
//
//		@Override
//		public void run() {
//			try {
//				VirtualObject next;
//				next = objectProvider.next();
//				
//				StreamingSerializer ifcSerializer = new IfcStepStreamingSerializer() {
//				};
//				IRenderEngine renderEngine = null;
//				byte[] bytes = null;
//				try {
//					final Set<VirtualObject > objects = new HashSet<>();
//					
//					CloseableIterator<VirtualObject> iterator = server.getPlatformService().streamVirtualObjectByRid(rid);
//					IfcHeader ifcHeader = server.getPlatformService().queryIfcHeader(rid);
//					SimpleObjectProvider objectProvider = new SimpleObjectProvider(iterator);
//					
//					ifcSerializer.init(server.getPlatformService(), objectProvider, ifcHeader, packageMetaData);
//	
//					ByteArrayOutputStream baos = new ByteArrayOutputStream();
//					IOUtils.copy(ifcSerializer.getInputStream(), baos);
//					bytes = baos.toByteArray();
//					InputStream in = new ByteArrayInputStream(bytes);
//					try {
//						if (!objects.isEmpty()) {
//							renderEngine = renderEnginePool.borrowObject();
//							try (IRenderEngineModel renderEngineModel = renderEngine.openModel(in, bytes.length)) {
//								renderEngineModel.setSettings(renderEngineSettings);
//								renderEngineModel.setFilter(renderEngineFilter);
//		
//								try {
//									renderEngineModel.generateGeneralGeometry();
//								} catch (RenderEngineException e) {
//									if (e.getCause() instanceof java.io.EOFException) {
//										if (objects.isEmpty() || eClass.getName().equals("IfcAnnotation")) {
//										} else {
//											LOGGER.error("Error in " + eClass.getName(), e);
//										}
//									}
//								}
//								
//								OidConvertingSerializer oidConvertingSerializer = (OidConvertingSerializer)ifcSerializer;
//								Map<Long, Integer> oidToEid = oidConvertingSerializer.getOidToEid();
//								
//								for (VirtualObject ifcProduct : objects) {
//									if (!running) {
//										return;
//									}
//									Integer expressId = oidToEid.get(ifcProduct.getOid());
//									try {
//										IRenderEngineInstance renderEngineInstance = renderEngineModel.getInstanceFromExpressId(expressId);
//										RenderEngineGeometry geometry = renderEngineInstance.generateGeometry();
//										boolean translate = true;
//										
//										if (geometry != null && geometry.getNrIndices() > 0) {
//											Short geometryInfoCid = server.getPlatformService().getCidOfEClass(GeometryPackage.eINSTANCE.getGeometryInfo());
//											Long geometryInfoOid = server.getPlatformService().newOid(GeometryPackage.eINSTANCE.getGeometryInfo());
//											VirtualObject geometryInfo = new VirtualObject(rid, geometryInfoCid, geometryInfoOid);
//											
//											Short vector3fCid = server.getPlatformService().getCidOfEClass(GeometryPackage.eINSTANCE.getVector3f());
//											WrappedVirtualObject minBounds = new WrappedVirtualObject(vector3fCid);
//											WrappedVirtualObject maxBounds = new WrappedVirtualObject(vector3fCid);
//											
//											minBounds.set("x", Double.POSITIVE_INFINITY);
//											minBounds.set("y", Double.POSITIVE_INFINITY);
//											minBounds.set("z", Double.POSITIVE_INFINITY);
//											
//											maxBounds.set("x", -Double.POSITIVE_INFINITY);
//											maxBounds.set("y", -Double.POSITIVE_INFINITY);
//											maxBounds.set("z", -Double.POSITIVE_INFINITY);
//											
//											geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_MinBounds(), minBounds);
//											geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_MaxBounds(), maxBounds);
//	
//											renderEngineInstance.getArea();
//											
//											geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_Area(), renderEngineInstance.getArea());
//											geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_Volume(), renderEngineInstance.getVolume());
//											
//											Short geometryDataCid = server.getPlatformService().getCidOfEClass(GeometryPackage.eINSTANCE.getGeometryData());
//											Long geometryDataOid = server.getPlatformService().newOid(GeometryPackage.eINSTANCE.getGeometryData());
//											VirtualObject geometryData = new VirtualObject(rid, geometryDataCid, geometryDataOid);
//	
//											int[] indices = geometry.getIndices();
//											geometryData.setAttribute(GeometryPackage.eINSTANCE.getGeometryData_Indices(), intArrayToByteArray(indices));
//											float[] vertices = geometry.getVertices();
//											geometryData.setAttribute(GeometryPackage.eINSTANCE.getGeometryData_Vertices(), floatArrayToByteArray(vertices));
////												geometryData.setAttribute(GeometryPackage.eINSTANCE.getGeometryData_MaterialIndices(), intArrayToByteArray(geometry.getMaterialIndices()));
//											geometryData.setAttribute(GeometryPackage.eINSTANCE.getGeometryData_Normals(), floatArrayToByteArray(geometry.getNormals()));
//											
//											geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_PrimitiveCount(), indices.length / 3);
//
//											Set<Color4f> usedColors = new HashSet<>();
//											
//											int saveableColorBytes = 0;
//											
//											if (geometry.getMaterialIndices() != null && geometry.getMaterialIndices().length > 0) {
//												boolean hasMaterial = false;
//												float[] vertex_colors = new float[vertices.length / 3 * 4];
//												for (int i = 0; i < geometry.getMaterialIndices().length; ++i) {
//													int c = geometry.getMaterialIndices()[i];
//													for (int j = 0; j < 3; ++j) {
//														int k = indices[i * 3 + j];
//														if (c > -1) {
//															hasMaterial = true;
//															Color4f color = new Color4f();
//															for (int l = 0; l < 4; ++l) {
//																float val = geometry.getMaterials()[4 * c + l];
//																vertex_colors[4 * k + l] = val;
//																color.set(l, val);
//															}
//															usedColors.add(color);
//														}
//													}
//												}
//												if (!usedColors.isEmpty()) {
//													if (usedColors.size() == 1) {
//														saveableColorBytes = (4 * vertex_colors.length) - 16;
//													}
//												}
//												if (hasMaterial) {
//													geometryData.setAttribute(GeometryPackage.eINSTANCE.getGeometryData_Materials(), floatArrayToByteArray(vertex_colors));
//												}
//											}
//	
//											double[] tranformationMatrix = new double[16];
//											if (translate && renderEngineInstance.getTransformationMatrix() != null) {
//												tranformationMatrix = renderEngineInstance.getTransformationMatrix();
//											} else {
//												Matrix.setIdentityM(tranformationMatrix, 0);
//											}
//	
//											for (int i = 0; i < indices.length; i++) {
//												processExtends(geometryInfo, tranformationMatrix, vertices, indices[i] * 3, generateGeometryResult);
//											}
//											
//											geometryInfo.setReference(GeometryPackage.eINSTANCE.getGeometryInfo_Data(), geometryData.getOid());
//	
//											long size = getSize(geometryData);
//	
//											setTransformationMatrix(geometryInfo, tranformationMatrix);
//											
//											int hash = hash(geometryData);
//											if (hashes.containsKey(hash)) {
//												geometryInfo.setReference(GeometryPackage.eINSTANCE.getGeometryInfo_Data(), hashes.get(hash));
//												bytesSaved.addAndGet(size);
//											} else {
////												if (sizes.containsKey(size) && sizes.get(size).eClass() == ifcProduct.eClass()) {
////													LOGGER.info("More reuse might be possible " + size + " " + ifcProduct.eClass().getName() + ":" + ifcProduct.getOid() + " / " + sizes.get(size).eClass().getName() + ":" + sizes.get(size).getOid());
////												}
//												hashes.put(hash, geometryData.getOid());
//												StreamingGeometryGenerator.this.saveableColorBytes.addAndGet(saveableColorBytes);
//												server.getPlatformService().saveBatch(geometryData);
////												sizes.put(size, ifcProduct);
//											}
//											
//											server.getPlatformService().saveBatch(geometryInfo);
//											totalBytes.addAndGet(size);
//	
//											ifcProduct.setReference(geometryFeature, geometryInfo.getOid());
//											server.getPlatformService().saveBatch(ifcProduct);
//											
//										} 
//									} catch (EntityNotFoundException e) {
//	//									e.printStackTrace();
//										// As soon as we find a representation that is not Curve2D, then we should show a "INFO" message in the log to indicate there could be something wrong
//										boolean ignoreNotFound = eClass.getName().equals("IfcAnnotation");
//										
//	//									for (Object rep : representations) {
//	//										if (rep instanceof IfcShapeRepresentation) {
//	//											IfcShapeRepresentation ifcShapeRepresentation = (IfcShapeRepresentation)rep;
//	//											if (!"Curve2D".equals(ifcShapeRepresentation.getRepresentationType())) {
//	//												ignoreNotFound = false;
//	//											}
//	//										}
//	//									}
//										if (!ignoreNotFound) {
//											EClass productEClass = server.getPlatformService().getEClassForCid(ifcProduct.getEClassId());
//											LOGGER.warn("Entity not found " + productEClass.getName() + " " + (expressId) + "/" + ifcProduct.getOid());
//										}
//									} catch (DatabaseException | RenderEngineException e) {
//										LOGGER.error("", e);
//									}
//								}
//							}
//						}
//					} finally {
//						try {
//	//						if (notFoundsObjects) {
////								writeDebugFile(bytes, false);
//	//							Thread.sleep(60000);
//	//						}
//							in.close();
//						} catch (Throwable e) {
//							
//						}
//						if (renderEngine != null) {
//							renderEnginePool.returnObject(renderEngine);
//						}
//						jobsDone.incrementAndGet();
//						updateProgress();
//					}
//				} catch (Exception e) {
//					LOGGER.error("", e);
//				}
//			} catch (Exception e) {
//				LOGGER.error("", e);
//			}
//		}
//
//	}
//
//	private void updateProgress() {
//		if (allJobsPushed) {
////			if (progressListener != null) {
////				progressListener.updateProgress("Generating geometry...", (int) (100.0 * jobsDone.get() / jobsTotal.get()));
////			}
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	public GenerateGeometryResult generateGeometry() throws DatabaseException {
//		GenerateGeometryResult generateGeometryResult = new GenerateGeometryResult();
//		IfcHeader ifcHeader = server.getPlatformService().queryIfcHeader(rid);
//		String ifcSchemaVersion = ifcHeader.getIfcSchemaVersion();
//		Schema schema = null;
//		if (ifcSchemaVersion.startsWith(IFC2X3_SCHEMA_SHORT)) {
//			schema = Schema.IFC2X3TC1;
//		} else if (ifcSchemaVersion.startsWith(IFC4_SCHEMA_SHORT)) {
//			schema = Schema.IFC4;
//		}
//		if (schema == null) {
//			return null;
//		}
//		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
//		productClass = packageMetaData.getEClass("IfcProduct");
//		geometryFeature = productClass.getEStructuralFeature("geometry");
//		representationFeature = productClass.getEStructuralFeature("Representation");
//
//		long start = System.nanoTime();
//
//		try {
//
//			final RenderEngineSettings settings = new RenderEngineSettings();
//			settings.setPrecision(Precision.SINGLE);
//			settings.setIndexFormat(IndexFormat.AUTO_DETECT);
//			settings.setGenerateNormals(true);
//			settings.setGenerateTriangles(true);
//			settings.setGenerateWireFrame(true);
//			
//			final RenderEngineFilter renderEngineFilter = new RenderEngineFilter();
//
//			RenderEnginePool renderEnginePool = server.getRenderEnginePools().getRenderEnginePool(packageMetaData.getSchema());
//			
//			int maxSimultanousThreads = 100;
//			ThreadPoolExecutor executor = new ThreadPoolExecutor(maxSimultanousThreads, maxSimultanousThreads, 24, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(10000000));
//
//			Map<Long, AtomicInteger> counters = new HashMap<>();
//			
//			for (EClass eClass : queryContext.getOidCounters().keySet()) {
//				if (packageMetaData.getEClass("IfcProduct").isSuperTypeOf(eClass)) {
//					Query query2 = new Query("test", packageMetaData);
//					QueryPart queryPart2 = query2.createQueryPart();
//					queryPart2.addType(eClass, false);
//					Include include = queryPart2.createInclude();
//					include.addType(eClass, false);
//					include.addFieldDirect("Representation");
//					QueryObjectProvider queryObjectProvider2 = new QueryObjectProvider(databaseSession, bimServer, query2, Collections.singleton(queryContext.getRoid()), packageMetaData);
//					HashMapVirtualObject next = queryObjectProvider2.next();
//					while (next != null) {
//						if (next.eClass() == eClass) {
//							Set<Long> representationItems = getRepresentationItems(databaseSession, queryContext, next);
//							for (Long l : representationItems) {
//								AtomicInteger atomicInteger = counters.get(l);
//								if (atomicInteger == null) {
//									atomicInteger = new AtomicInteger(0);
//									counters.put(l, atomicInteger);
//								}
//								atomicInteger.incrementAndGet();
//							}
//							HashMapVirtualObject representation = next.getDirectFeature(representationFeature);
//							if (representation != null) {
//								List<Long> representations = (List<Long>) representation.get("Representations");
//								if (representations != null && !representations.isEmpty()) {
//									Query query = new Query("test", packageMetaData);
//									QueryPart queryPart = query.createQueryPart();
//									queryPart.addType(eClass, false);
//									int x = 0;
//									queryPart.addOid(next.getOid());
//									while (next != null && x < maxObjectsPerFile - 1) {
//										next = queryObjectProvider2.next();
//										if (next != null) {
//											if (next.eClass() == eClass) {
//												representation = next.getDirectFeature(representationFeature);
//												if (representation != null) {
//													representations = (List<Long>) representation.get("Representations");
//													if (representations != null && !representations.isEmpty()) {
//														queryPart.addOid(next.getOid());
//														x++;
//													}
//												}
//											}
//										}
//									}
//									JsonQueryObjectModelConverter jsonQueryObjectModelConverter = new JsonQueryObjectModelConverter(packageMetaData);
//									
//									String queryNameSpace = "validifc";
//									if (packageMetaData.getSchema() == Schema.IFC4) {
//										queryNameSpace = "ifc4stdlib";
//									}
//									
//									if (eClass.getName().equals("IfcAnnotation")) {
//										// IfcAnnotation also has the field ContainedInStructure, but that is it's own field (looks like a hack on the IFC-spec side)
//										queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":IfcAnnotationContainedInStructure"));
//									} else {
//										queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":ContainedInStructure"));
//									}
//									if (packageMetaData.getSchema() == Schema.IFC4) {
//										queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":IsTypedBy"));
//									}
//									queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":Decomposes"));
//									queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":OwnerHistory"));
//									Include representationInclude = jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":Representation");
//									queryPart.addInclude(representationInclude);
//									Include objectPlacement = jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":ObjectPlacement");
//									queryPart.addInclude(objectPlacement);
//									if (packageMetaData.getEClass("IfcElement").isSuperTypeOf(eClass)) {
//										Include openingsInclude = queryPart.createInclude();
//										openingsInclude.addType(packageMetaData.getEClass(eClass.getName()), false);
//										openingsInclude.addField("HasOpenings");
//										Include hasOpenings = openingsInclude.createInclude();
//										hasOpenings.addType(packageMetaData.getEClass("IfcRelVoidsElement"), false);
//										hasOpenings.addField("RelatedOpeningElement");
//										hasOpenings.addInclude(representationInclude);
//										hasOpenings.addInclude(objectPlacement);
//										//						Include relatedOpeningElement = hasOpenings.createInclude();
//										//						relatedOpeningElement.addType(packageMetaData.getEClass("IfcOpeningElement"), false);
//										//						relatedOpeningElement.addField("HasFillings");
//										//						Include hasFillings = relatedOpeningElement.createInclude();
//										//						hasFillings.addType(packageMetaData.getEClass("IfcRelFillsElement"), false);
//										//						hasFillings.addField("RelatedBuildingElement");
//									}
//									QueryObjectProvider queryObjectProvider = new QueryObjectProvider(databaseSession, bimServer, query, Collections.singleton(queryContext.getRoid()), packageMetaData);
//									
//									Runner runner = new Runner(eClass, renderEnginePool, databaseSession, settings, queryObjectProvider, ifcSerializerPlugin, renderEngineFilter, generateGeometryResult, queryContext, query);
//									executor.submit(runner);
//									jobsTotal.incrementAndGet();
//									
//								}
//							}
//						}
//						next = queryObjectProvider2.next();
//					}
//				}
//			}
//			
////			for (Long l : counters.keySet()) {
////				LOGGER.info(databaseSession.getEClassForOid(l).getName() + "(" + l + "): " + counters.get(l));
////			}
//			
//			allJobsPushed = true;
//			
//			executor.shutdown();
//			executor.awaitTermination(24, TimeUnit.HOURS);				
//
//			long end = System.nanoTime();
//			LOGGER.info("Rendertime: " + ((end - start) / 1000000) + "ms, " + "Reused: " + Formatters.bytesToString(bytesSaved.get()) + ", Total: " + Formatters.bytesToString(totalBytes.get()) + ", Final: " + Formatters.bytesToString(totalBytes.get() - bytesSaved.get()));
//			LOGGER.info("Saveable color data: " + Formatters.bytesToString(saveableColorBytes.get()));
//		} catch (Exception e) {
//			running = false;
//			LOGGER.error("", e);
//			throw new GeometryGeneratingException(e);
//		}
//		return generateGeometryResult;
//	}
//	
//	private Set<Long> getRepresentationItems(DatabaseSession databaseSession, QueryContext queryContext, HashMapVirtualObject next) throws QueryException, IOException {
//		Set<Long> result = new HashSet<>();
//		Query query = new Query("test", packageMetaData);
//		
//		Include representation = query.createDefine("Representation");
//		representation.addType(packageMetaData.getEClass("IfcShapeRepresentation"), true);
//		representation.addField("Items");
//		Include mapped = representation.createInclude();
//		mapped.addType(packageMetaData.getEClass("IfcMappedItem"), true);
//		mapped.addField("MappingSource");
//		Include mappingSource = mapped.createInclude();
//		mappingSource.addType(packageMetaData.getEClass("IfcRepresentationMap"), false);
//		mappingSource.addField("MappedRepresentation");
//		mappingSource.addInclude(representation);
//		
//		QueryPart queryPart = query.createQueryPart();
//		queryPart.addOid(next.getOid());
//		Include include = queryPart.createInclude();
//		include.addType(next.eClass(), false);
//		include.addField("Representation");
//		Include representations = include.createInclude();
//		representations.addType(packageMetaData.getEClass("IfcProductDefinitionShape"), true);
//		representations.addField("Representations");
//		representations.addInclude(representation);
//		
//		QueryObjectProvider queryObjectProvider = new QueryObjectProvider(databaseSession, bimServer, query, Collections.singleton(queryContext.getRoid()), packageMetaData);
//		try {
//			VirtualObject next2 = queryObjectProvider.next();
//			while (next2 != null) {
//				if (packageMetaData.getEClass("IfcRepresentationItem").isSuperTypeOf(next2.eClass())) {
//					result.add(next2.getOid());
//				}
//				next2 = queryObjectProvider.next();
//			}
//		} catch (DatabaseException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	private long getSize(VirtualObject geometryData) {
//		long size = 0;
//		if (geometryData.has("indices")) {
//			size += ((byte[])geometryData.get("vertices")).length;
//		}
//		if (geometryData.has("vertices")) {
//			size += ((byte[])geometryData.get("vertices")).length;
//		}
//		if (geometryData.has("normals")) {
//			size += ((byte[])geometryData.get("normals")).length;
//		}
//		if (geometryData.has("materialIndices")) {
//			size += ((byte[])geometryData.get("materialIndices")).length;
//		}
//		if (geometryData.has("materials")) {
//			size += ((byte[])geometryData.get("materials")).length;
//		}
//		return size;
//	}
//
//	private int hash(VirtualObject geometryData) {
//		int hashCode = 0;
//		if (geometryData.has("indices")) {
//			hashCode += Arrays.hashCode((byte[])geometryData.get("vertices"));
//		}
//		if (geometryData.has("vertices")) {
//			hashCode += Arrays.hashCode((byte[])geometryData.get("vertices"));
//		}
//		if (geometryData.has("normals")) {
//			hashCode += Arrays.hashCode((byte[])geometryData.get("normals"));
//		}
//		if (geometryData.has("materialIndices")) {
//			hashCode += Arrays.hashCode((byte[])geometryData.get("materialIndices"));
//		}
//		if (geometryData.has("materials")) {
//			hashCode += Arrays.hashCode((byte[])geometryData.get("materials"));
//		}
//		return hashCode;
//	}
//
//	private void processExtends(VirtualObject geometryInfo, double[] transformationMatrix, float[] vertices, int index, GenerateGeometryResult generateGeometryResult) throws DatabaseException {
//		double x = vertices[index];
//		double y = vertices[index + 1];
//		double z = vertices[index + 2];
//		
//		double[] result = new double[4];
//		
//		Matrix.multiplyMV(result, 0, transformationMatrix, 0, new double[] { x, y, z, 1 }, 0);
//		x = result[0];
//		y = result[1];
//		z = result[2];
//		
//		WrappedVirtualObject minBounds = (WrappedVirtualObject) geometryInfo.eGet(GeometryPackage.eINSTANCE.getGeometryInfo_MinBounds());
//		WrappedVirtualObject maxBounds = (WrappedVirtualObject) geometryInfo.eGet(GeometryPackage.eINSTANCE.getGeometryInfo_MaxBounds());
//		
//		minBounds.set("x", Math.min(x, (double)minBounds.eGet("x")));
//		minBounds.set("y", Math.min(y, (double)minBounds.eGet("y")));
//		minBounds.set("z", Math.min(z, (double)minBounds.eGet("z")));
//		maxBounds.set("x", Math.max(x, (double)maxBounds.eGet("x")));
//		maxBounds.set("y", Math.max(y, (double)maxBounds.eGet("y")));
//		maxBounds.set("z", Math.max(z, (double)maxBounds.eGet("z")));
//
//		generateGeometryResult.setMinX(Math.min(x, generateGeometryResult.getMinX()));
//		generateGeometryResult.setMinY(Math.min(y, generateGeometryResult.getMinY()));
//		generateGeometryResult.setMinZ(Math.min(z, generateGeometryResult.getMinZ()));
//		generateGeometryResult.setMaxX(Math.max(x, generateGeometryResult.getMaxX()));
//		generateGeometryResult.setMaxY(Math.max(y, generateGeometryResult.getMaxY()));
//		generateGeometryResult.setMaxZ(Math.max(z, generateGeometryResult.getMaxZ()));
//	}
//
//	private void setTransformationMatrix(VirtualObject geometryInfo, double[] transformationMatrix) throws DatabaseException {
//		ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 8);
//		byteBuffer.order(ByteOrder.nativeOrder());
//		DoubleBuffer asDoubleBuffer = byteBuffer.asDoubleBuffer();
//		for (double d : transformationMatrix) {
//			asDoubleBuffer.put(d);
//		}
//		geometryInfo.setAttribute(GeometryPackage.eINSTANCE.getGeometryInfo_Transformation(), byteBuffer.array());
//	}
//}