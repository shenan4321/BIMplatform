package cn.dlb.bim.service;

import java.util.List;

import cn.dlb.bim.adaptors.GeometryInfoAdaptor;
import cn.dlb.bim.emf.IfcModelInterface;

public interface IBimService {
	
	public List<IfcModelInterface> queryAllIfcModel();
	public List<GeometryInfoAdaptor> queryGeometryInfo();
}
