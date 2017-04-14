package cn.dlb.bim.ifc.collada;

import java.util.Arrays;

import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.geometry.Vector3f;

public class Extends {
	public double[] min = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
	public double[] max = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
	
	public void addToMinExtents(double[] vertex) {
		for (int i=0; i<vertex.length; i++) {
			min[i] = Math.min(vertex[i], min[i]);
		}
	}
	
	public void addToMaxExtents(double[] vertex) {
		for (int i=0; i<vertex.length; i++) {
			max[i] = Math.max(vertex[i], max[i]);
		}
	}
	
	@Override
	public String toString() {
		return "min: " + Arrays.toString(min) + ", max: " + Arrays.toString(max);
	}

	public void integrate(GeometryInfo geometryInfo) {
		Vector3f min = geometryInfo.getMinBounds();
		Vector3f max = geometryInfo.getMaxBounds();
		addToMinExtents(new double[]{min.getX(), min.getY(), min.getZ()});
		addToMaxExtents(new double[]{max.getX(), max.getY(), max.getZ()});
	}
}