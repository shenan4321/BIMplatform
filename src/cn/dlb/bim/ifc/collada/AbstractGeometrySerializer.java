package cn.dlb.bim.ifc.collada;

import java.util.HashMap;
import org.slf4j.LoggerFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.ifc.serializers.EmfSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;

public abstract class AbstractGeometrySerializer extends EmfSerializer {
	private Extends sceneExtends = new Extends();
	private HashMap<String, Extends> geometryExtents = new HashMap<String, Extends>();
	
	protected void calculateGeometryExtents() throws RenderEngineException, SerializerException {
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			try {
				calculateExtents(ifcProduct.getGlobalId(), ifcProduct);
			} catch (Exception e) {
				LoggerFactory.getLogger(AbstractGeometrySerializer.class).error("", e);
			}
		}
	}
	
	public Extends getSceneExtends() {
		return sceneExtends;
	}
	
	private void calculateExtents(String id, IfcProduct ifcObject) throws RenderEngineException, SerializerException {
		if (!geometryExtents.containsKey(id)) {
			geometryExtents.put(id, new Extends());
		}
		Extends extents = geometryExtents.get(id);

		GeometryInfo geometryInfo = ifcObject.getGeometry();
		if (geometryInfo != null) {
			extents.integrate(geometryInfo);
			sceneExtends.addToMinExtents(extents.min);
			sceneExtends.addToMaxExtents(extents.max);
		}
	}
}