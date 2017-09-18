package cn.dlb.bim.dao.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.stream.MinimalVirtualObject;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;
import cn.dlb.bim.service.CatalogService;

@Component("VirtualObjectReadConverter")
public class VirtualObjectReadConverter implements Converter<DBObject, VirtualObject>  {
	
	@Autowired
	@Qualifier("PlatformServer")
	@Lazy(true)
	private PlatformServer platformServer;
	@Autowired
	@Lazy(true)
	private CatalogService platformService;

	@SuppressWarnings("rawtypes")
	@Override
	public VirtualObject convert(DBObject source) {
		Integer rid = (Integer) source.get("rid");
		Integer classId = (Integer) source.get("eClassId");
		Long oid = (Long) source.get("oid");
		Object featuresObject = source.get("features");
		
		EClass eclass = platformService.getEClassForCid(classId.shortValue());
		
		VirtualObject result = new VirtualObject(rid, classId.shortValue(), oid, eclass);
		
		if (featuresObject instanceof BasicDBObject) {
			Map map = (Map) featuresObject;
			processFeatures(map, result);
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processFeatures(Map originMap, MinimalVirtualObject object) {
		for (Object key : originMap.keySet()) {
			Object value = originMap.get(key);
			if (value instanceof BasicDBObject) {
				WrappedVirtualObject wrappedVirtualObject = convertWrappedVirtualObject((BasicDBObject) value);
				object.getFeatures().put(Integer.valueOf((String) key), wrappedVirtualObject);
				EStructuralFeature eStructuralFeature = object.eClass().getEStructuralFeature(Integer.valueOf((String) key));
				object.addUseForSerialization(eStructuralFeature);
			} else if (value instanceof List) {
				List originList = (List) value;
				List newList = new ArrayList<>();
				for (int i = 0; i < originList.size();i++) {
					Object originElement = originList.get(i);
					if (originElement instanceof BasicDBObject) {
						WrappedVirtualObject wrappedVirtualInList = convertWrappedVirtualObject((BasicDBObject) originElement);
						EStructuralFeature eStructuralFeature = object.eClass().getEStructuralFeature(Integer.valueOf((String) key));
						object.addUseForSerialization(eStructuralFeature);
						newList.add(wrappedVirtualInList);
					} else {
						newList.add(originElement);
					}
				}
				object.getFeatures().put(Integer.valueOf((String) key), newList);
			} else {
				object.getFeatures().put(Integer.valueOf((String) key), value);
			}
		}
	}
	@SuppressWarnings("rawtypes")
	public WrappedVirtualObject convertWrappedVirtualObject(BasicDBObject source) {
		Integer classId = (Integer) source.get("eClassId");
		Object featuresObject = source.get("features");
		EClass eclass = platformService.getEClassForCid(classId.shortValue());
		WrappedVirtualObject wrappedVirtualObject = new WrappedVirtualObject(classId.shortValue(), eclass);
		processFeatures((Map) featuresObject, wrappedVirtualObject);
		return wrappedVirtualObject;
	}

}
