package cn.dlb.bim.ifc.engine.cells;

public class Colord {

	public double r;
	public double g;
	public double b;
	private double a;
	
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

	public double r() {
		return r;
	}

	public double g() {
		return g;
	}

	public double b() {
		return b;
	}
	
	public double a() {
		return a;
	}
}
