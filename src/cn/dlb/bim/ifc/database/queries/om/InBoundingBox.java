package cn.dlb.bim.ifc.database.queries.om;

import cn.dlb.bim.ifc.engine.cells.AxisAlignedBoundingBox;

public class InBoundingBox extends PartOfQuery {
	private double x;
	private double y;
	private double z;
	private double width;
	private double height;
	private double depth;

	public InBoundingBox(double x, double y, double z, double width, double height, double depth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	public InBoundingBox(AxisAlignedBoundingBox boundingBox) {
		this.x = boundingBox.getMin()[0];
		this.y = boundingBox.getMin()[1];
		this.z = boundingBox.getMin()[2];
		this.width = boundingBox.getMax()[0] - boundingBox.getMin()[0];
		this.height = boundingBox.getMax()[1] - boundingBox.getMin()[1];
		this.depth = boundingBox.getMax()[2] - boundingBox.getMin()[2];
	}

	public double getDepth() {
		return depth;
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void dump(int indent, StringBuilder sb) {
		sb.append(indent(indent) + "x: " + x + ", y: " + y + ", z: " + z + ", width: " + width + ", height: " + height + ", depth: " + depth);
	}
}