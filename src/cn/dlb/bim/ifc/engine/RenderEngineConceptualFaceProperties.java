package cn.dlb.bim.ifc.engine;

public class RenderEngineConceptualFaceProperties {
	private int startIndexTriangles;
	private int noIndicesTriangles;
	private int startIndexLines;
	private int noIndicesLines;
	private int startIndexPoints;
	private int noIndicesPoints;
	private int startIndexFacesPolygons;
	private int noIndicesFacesPolygons;
	private int startIndexConceptualFacePolygons;
	private int noIndicesConceptualFacePolygons;
	
	public RenderEngineConceptualFaceProperties(int startIndexTriangles, int noIndicesTriangles, int startIndexLines, int noIndicesLines, 
			int startIndexPoints, int noIndicesPoints, int startIndexFacesPolygons, int noIndicesFacesPolygons,
			int startIndexConceptualFacePolygons, int noIndicesConceptualFacePolygons) {
		this.startIndexTriangles = startIndexTriangles;
		this.noIndicesTriangles = noIndicesTriangles;
		this.startIndexLines = startIndexLines;
		this.noIndicesLines = noIndicesLines;
		this.startIndexPoints = startIndexPoints;
		this.noIndicesPoints = noIndicesPoints;
		this.startIndexFacesPolygons = startIndexFacesPolygons;
		this.noIndicesFacesPolygons = noIndicesFacesPolygons;
		this.startIndexConceptualFacePolygons = startIndexConceptualFacePolygons;
		this.noIndicesConceptualFacePolygons = noIndicesConceptualFacePolygons;
	}

	public int getStartIndexTriangles() {
		return startIndexTriangles;
	}

	public int getNoIndicesTriangles() {
		return noIndicesTriangles;
	}

	public int getStartIndexLines() {
		return startIndexLines;
	}

	public int getNoIndicesLines() {
		return noIndicesLines;
	}

	public int getStartIndexPoints() {
		return startIndexPoints;
	}

	public int getNoIndicesPoints() {
		return noIndicesPoints;
	}

	public int getStartIndexFacesPolygons() {
		return startIndexFacesPolygons;
	}

	public int getNoIndicesFacesPolygons() {
		return noIndicesFacesPolygons;
	}

	public int getStartIndexConceptualFacePolygons() {
		return startIndexConceptualFacePolygons;
	}

	public int getNoIndicesConceptualFacePolygons() {
		return noIndicesConceptualFacePolygons;
	}
}
