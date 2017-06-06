package cn.dlb.bim.ifc.serializers.binarygeometry;

import java.io.IOException;

import com.google.common.io.LittleEndianDataOutputStream;

import cn.dlb.bim.models.geometry.Vector3f;

public class Bounds {
	public Double3 min;
	public Double3 max;

	public Bounds(Double3 min, Double3 max) {
		this.min = min;
		this.max = max;
	}

	public Bounds() {
		this.min = new Double3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		this.max = new Double3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
	}

	public Bounds(Vector3f minBounds, Vector3f maxBounds) {
		this.min = new Double3(minBounds.getX(), minBounds.getY(), minBounds.getZ());
		this.max = new Double3(maxBounds.getX(), maxBounds.getY(), maxBounds.getZ());
	}

	public void integrate(Bounds objectBounds) {
		this.min.min(objectBounds.min);
		this.max.max(objectBounds.max);
	}

	public void writeTo(LittleEndianDataOutputStream dataOutputStream) throws IOException {
		min.writeTo(dataOutputStream);
		max.writeTo(dataOutputStream);
	}
}