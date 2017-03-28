package cn.dlb.bim.service;

import java.util.List;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.vo.GeometryInfoVo;

public interface IBimService {
	public List<GeometryInfoVo> queryDbGeometryInfo(Integer rid);
	public List<IfcModelInterface> queryAllIfcModel();
	public List<GeometryInfoVo> queryGeometryInfo();
}
