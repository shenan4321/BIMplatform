package cn.dlb.bim.service;

import java.util.List;

import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.models.geometry.GeometryInfo;

public interface IBimService {
	
	public List<IfcModelInterface> queryAllIfcModel();
	public List<GeometryInfo> queryGeometryInfo();
}
