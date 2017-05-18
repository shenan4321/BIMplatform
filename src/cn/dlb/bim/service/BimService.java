package cn.dlb.bim.service;

import java.io.File;
import java.util.List;

import cn.dlb.bim.dao.entity.ModelLabel;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.vo.ModelLabelVo;

public interface BimService {
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid);
	public Integer addRevision(ModelInfoVo modelInfo, File modelFile);
	public GlbVo queryGlbByRid(Integer rid);
	public Vector3d queryGlbLonlatByRid(Integer rid);
	public IfcModelInterface queryModelByRid(Integer rid);
	public List<ModelInfoVo> queryModelInfoByPid(Long pid);
	public void deleteModel(Integer rid);
	public ModelInfoVo queryModelInfoByRid(Integer rid);
	public void setGlbLonlat(Integer rid, Double lon, Double lat);
	
	/**
	 * 标签操作
	 */
	public void insertModelLabel(ModelLabelVo modelLabel);
	public void deleteModelLabel(Integer labelId);
	public void modifyModelLabel(ModelLabelVo modelLabel);
	public List<ModelLabelVo> queryAllModelLabelByRid(Integer rid);
}
