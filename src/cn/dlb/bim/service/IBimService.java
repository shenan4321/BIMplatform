package cn.dlb.bim.service;

import java.io.File;
import java.util.List;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;

public interface IBimService {
	public List<GeometryInfoVo> queryDbGeometryInfo(Integer rid);
	public List<IfcModelInterface> queryAllIfcModel();
	public List<GeometryInfoVo> queryGeometryInfo();
	public int deserializeModelFileAndSave(File modelFile);
	public GlbVo queryGlbByRid(Integer rid);
	public Vector3d queryGlbLonlatByRid(Integer rid);
}
