package cn.dlb.bim.ifc.engine.cells;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

public class Colord {

	public double r;
	public double g;
	public double b;
	public double a;
	
	public Colord() {}
	
	public Colord(double r, double g, double b, double a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public Colord(double d) {
		this.r = d;
		this.g = d;
		this.b = d;
		this.a = d;
	}
	
	public void writeJson(JsonWriter jsonWriter) throws IOException {
		jsonWriter.beginObject();
		jsonWriter.name("r").value(r);
		jsonWriter.name("g").value(g);
		jsonWriter.name("b").value(b);
		jsonWriter.name("a").value(a);
		jsonWriter.endObject();
	}

}
