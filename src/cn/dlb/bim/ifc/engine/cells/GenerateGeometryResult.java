package cn.dlb.bim.ifc.engine.cells;

import cn.dlb.bim.models.geometry.GeometryFactory;
import cn.dlb.bim.models.geometry.Vector3f;

public class GenerateGeometryResult {

	private double[] min;
	private double[] max;

	public GenerateGeometryResult() {
		min = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		max = new double[]{-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
	}

	public double[] getMin() {
		return min;
	}
	
	public double[] getMax() {
		return max;
	}
	
	public Vector3f getMinBoundsAsVector3f() {
		Vector3f min = GeometryFactory.eINSTANCE.createVector3f();
		min.setX(this.min[0]);
		min.setY(this.min[1]);
		min.setZ(this.min[2]);
		return min;
	}

	public Vector3f getMaxBoundsAsVector3f() {
		Vector3f max = GeometryFactory.eINSTANCE.createVector3f();
		max.setX(this.max[0]);
		max.setY(this.max[1]);
		max.setZ(this.max[2]);
		return max;
	}

	public double getMinX() {
		return min[0];
	}

	public double getMinY() {
		return min[1];
	}
	
	public double getMinZ() {
		return min[2];
	}

	public double getMaxX() {
		return max[0];
	}
	
	public double getMaxY() {
		return max[1];
	}

	public double getMaxZ() {
		return max[2];
	}

	public void setMinX(double value) {
		min[0] = value;
	}
	
	public void setMinY(double value) {
		min[1] = value;
	}
	
	public void setMinZ(double value) {
		min[2] = value;
	}
	
	public void setMaxX(double value) {
		max[0] = value;
	}
	
	public void setMaxY(double value) {
		max[1] = value;
	}
	
	public void setMaxZ(double value) {
		max[2] = value;
	}
}