package cn.dlb.bim.ifc.collada;

public class MaterialConvertor {
	private final String ifcType;
	private final double[] colors;
	private final double opacity;
	
	public MaterialConvertor(String ifcType, double[] colors, double opacity) {
		this.ifcType = ifcType;
		this.colors = colors;
		this.opacity = opacity;
	}
	
	public String getIfcType() {
		return ifcType;
	}

	public double[] getColors() {
		return colors;
	}
	
	public double getOpacity() {
		return opacity;
	}
}