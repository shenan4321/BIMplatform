package cn.dlb.bim.vo;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

public class Bound {
	public Vector3f max;
	public Vector3f min;
	public Bound() {
		max = new Vector3f(0, 0, 0);
		min = new Vector3f(0, 0, 0);
	}
	public void writeJson(JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();
		jsonWriter.name("max");
		max.writeJson(jsonWriter);
		jsonWriter.name("min");
		min.writeJson(jsonWriter);
		jsonWriter.endObject();
	}
}
