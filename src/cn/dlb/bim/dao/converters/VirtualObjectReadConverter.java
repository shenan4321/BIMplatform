package cn.dlb.bim.dao.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;

public class VirtualObjectReadConverter implements Converter<DBObject, VirtualObject>  {

	@SuppressWarnings("rawtypes")
	@Override
	public VirtualObject convert(DBObject source) {
		Integer rid = (Integer) source.get("rid");
		Integer classId = (Integer) source.get("eClassId");
		Long oid = (Long) source.get("oid");
		Object featuresObject = source.get("features");
		VirtualObject result = new VirtualObject(rid, classId.shortValue(), oid);
		
		if (featuresObject instanceof BasicDBObject) {
			Map map = (Map) featuresObject;
			processFeatures(map, result.getFeatures());
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processFeatures(Map originMap, Map result) {
		for (Object key : originMap.keySet()) {
			Object value = originMap.get(key);
			if (value instanceof BasicDBObject) {
				WrappedVirtualObject wrappedVirtualObject = convertWrappedVirtualObject((BasicDBObject) value);
				result.put(key, wrappedVirtualObject);
			} else if (value instanceof List) {
				List originList = (List) value;
				List newList = new ArrayList<>();
				for (Object originElement : originList) {
					if (originElement instanceof BasicDBObject) {
						WrappedVirtualObject wrappedVirtualInList = convertWrappedVirtualObject((BasicDBObject) originElement);
						newList.add(wrappedVirtualInList);
					} else {
						newList.add(originElement);
					}
				}
				result.put(key, newList);
			} else {
				result.put(key, value);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public WrappedVirtualObject convertWrappedVirtualObject(BasicDBObject source) {
		Integer classId = (Integer) source.get("eClassId");
		Object featuresObject = source.get("features");
		WrappedVirtualObject wrappedVirtualObject = new WrappedVirtualObject(classId.shortValue());
		processFeatures((Map) featuresObject, wrappedVirtualObject.getFeatures());
		return wrappedVirtualObject;
	}

}
