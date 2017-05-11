package cn.dlb.bim.service;

import java.io.File;
import java.util.List;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;

public interface BimService {
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid);
	public Integer addRevision(Long pid, File modelFile);
	public GlbVo queryGlbByRid(Integer rid);
	public Vector3d queryGlbLonlatByRid(Integer rid);
	public IfcModelInterface queryModelByRid(Integer rid);
	public List<Integer> queryModelInProject(Long pid);
}
