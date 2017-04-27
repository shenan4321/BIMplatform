package cn.dlb.bim.ifc.engine.cells;

public class Material {
	
	private Colord ambient;
	private Colord diffuse;
	private Colord specular;
	private Colord emissive;
	private double transparency;
	private double shininess;
	
	public Material() {
		ambient = new Colord(0);
		diffuse = new Colord(0);
		specular = new Colord(0);
		emissive = new Colord(0);
	}
	
	public Colord getAmbient() {
		return ambient;
	}
	public void setAmbient(Colord ambient) {
		this.ambient = ambient;
	}
	public Colord getDiffuse() {
		return diffuse;
	}
	public void setDiffuse(Colord diffuse) {
		this.diffuse = diffuse;
	}
	public Colord getSpecular() {
		return specular;
	}
	public void setSpecular(Colord specular) {
		this.specular = specular;
	}
	public Colord getEmissive() {
		return emissive;
	}
	public void setEmissive(Colord emissive) {
		this.emissive = emissive;
	}
	public double getTransparency() {
		return transparency;
	}
	public void setTransparency(double transparency) {
		this.transparency = transparency;
	}
	public double getShininess() {
		return shininess;
	}
	public void setShininess(double shininess) {
		this.shininess = shininess;
	}
}
