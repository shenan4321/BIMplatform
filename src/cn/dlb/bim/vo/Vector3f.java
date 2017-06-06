package cn.dlb.bim.vo;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.stream.JsonWriter;

public class Vector3f {
	public double x, y, z;
	public Vector3f() {
	}
	public Vector3f(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void writeJson(JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();
		jsonWriter.name("x").value(x);
		jsonWriter.name("y").value(y);
		jsonWriter.name("z").value(z);
		jsonWriter.endObject();
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
}
