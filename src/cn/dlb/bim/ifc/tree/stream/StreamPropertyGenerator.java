package cn.dlb.bim.ifc.tree.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.emf.ecore.EClass;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;
import cn.dlb.bim.ifc.stream.query.JsonQueryObjectModelConverter;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryObjectProvider;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.multithread.MultiThreadQueryObjectProvider;
import cn.dlb.bim.ifc.tree.Propertry;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.ifc.tree.PropertySetCollection;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class StreamPropertyGenerator {
	private VirtualObject product;
	private PackageMetaData packageMetaData;
	private Map<Short, List<VirtualObject>> cidContainer = new HashMap<>();
	private Map<Long, VirtualObject> oidContainer = new HashMap<>();
	private final ThreadPoolTaskExecutor executor;
	private final CatalogService catalogService;
	private final VirtualObjectService virtualObjectService;
	private final ConcreteRevision concreteRevision;
	
	public StreamPropertyGenerator(ThreadPoolTaskExecutor executor, PackageMetaData packageMetaData, CatalogService catalogService,
			VirtualObjectService virtualObjectService, ConcreteRevision concreteRevision, VirtualObject product) {
		this.product = product;
		this.packageMetaData = packageMetaData;
		this.executor = executor;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.concreteRevision = concreteRevision;
		try {
			String queryNameSpace = "validifc";
			if (packageMetaData.getSchema() == Schema.IFC4) {
				queryNameSpace = "ifc4stdlib";
			}
			Query query = new Query(packageMetaData);
			JsonQueryObjectModelConverter jsonQueryObjectModelConverter = new JsonQueryObjectModelConverter(packageMetaData);
			QueryPart queryPart = query.createQueryPart();
			queryPart.addType(product.eClass(), false);
			queryPart.addOid(product.getOid());
			queryPart.addInclude(jsonQueryObjectModelConverter.getDefineFromFile(queryNameSpace + ":AllProperties"));
			
			MultiThreadQueryObjectProvider objectProvider = new MultiThreadQueryObjectProvider(executor, catalogService, virtualObjectService, query, concreteRevision.getRevisionId(), packageMetaData);
			VirtualObject next = objectProvider.next();
			while (next != null) {
				if (!cidContainer.containsKey(next.getEClassId())) {
					cidContainer.put(next.getEClassId(), new ArrayList<>());
				}
				cidContainer.get(next.getEClassId()).add(next);
				oidContainer.put(next.getOid(), next);
				next = objectProvider.next();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<PropertySet> getProperty() {
		
		EClass productClass = packageMetaData.getEClass("IfcProduct");
		if (!productClass.isSuperTypeOf(product.eClass())) {
			return null;
		}
		List<PropertySet> result = new ArrayList<PropertySet>();
		PropertySetCollection collection = new PropertySetCollection();
		Object isDefinedByReference = product.get("IsDefinedBy");
		if (isDefinedByReference != null) {
			List isDefinedByList = (List) isDefinedByReference;
			for (Object isDefinedBy : isDefinedByList) {
				VirtualObject ifcRelDefinesByProperties = oidContainer.get((Long) isDefinedBy);
				Object relatingPropertyDefinitionRef = ifcRelDefinesByProperties.get("RelatingPropertyDefinition");
				if (relatingPropertyDefinitionRef != null) {
					VirtualObject relatingPropertyDefinition = oidContainer.get((Long) relatingPropertyDefinitionRef);
					PropertySet propertySet = generatePropertySet(packageMetaData, relatingPropertyDefinition, collection);
					if (propertySet != null) {
						result.add(propertySet);
					}
					
				}
				
			}
		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public PropertySet generatePropertySet(PackageMetaData packageMetaData, VirtualObject ifcPropertySet, PropertySetCollection collection) {
		
		EClass ifcPropertySetClass = packageMetaData.getEClass("IfcPropertySet");
		if (!ifcPropertySetClass.isSuperTypeOf(ifcPropertySet.eClass())) {
			return null;
		}
		
		Long setOid = ifcPropertySet.getOid();
		String setName = (String) ifcPropertySet.eGet(ifcPropertySetClass.getEStructuralFeature("Name"));
		
		PropertySet propertySet = new PropertySet();
		propertySet.setOid(setOid);
		propertySet.setName(setName);
		
		Object hasProperties = ifcPropertySet.get("HasProperties");
		if (hasProperties != null) {
			List hasPropertiesList = (List) hasProperties;
			for (Object hasProperty : hasPropertiesList) {
				VirtualObject ifcPropertySingleValue = oidContainer.get((Long) hasProperty);
					generateProperty(packageMetaData, ifcPropertySingleValue, propertySet);
			}
		}
		
		
		return propertySet;
		
	}
	
	public void generateProperty(PackageMetaData packageMetaData, VirtualObject ifcPropertySingleValue, PropertySet propertySet) {
		EClass ifcPropertySingleValueClass = packageMetaData.getEClass("IfcPropertySingleValue");
		if (!ifcPropertySingleValueClass.isSuperTypeOf(ifcPropertySingleValue.eClass())) {
			return;
		}
		String name = (String) ifcPropertySingleValue.get("Name");
		Object nominalValue = ifcPropertySingleValue.get("NominalValue");
		if (nominalValue != null) {
			WrappedVirtualObject ifcValue = (WrappedVirtualObject) nominalValue;
			if (ifcValue.eClass().getEAnnotation("wrapped") != null) {
				Object value = ifcValue.eGet("wrappedValue");
				Propertry property = new Propertry();
				property.setName(name);
				property.setValue(value);
				propertySet.getPropertiySet().add(property);
			}
			
		}
	}
	
}
