package cn.dlb.bim.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.tree.BuildingCellContainer;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.ifc.tree.ProjectTree;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.vo.ModelLabelVo;

public interface BimService {
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid, ProgressReporter progressReporter);
	public Integer addRevision(ModelInfoVo modelInfo, File modelFile);
	public GlbVo queryGlbByRid(Integer rid);
	public Vector3d queryGlbLonlatByRid(Integer rid);
	public IfcModelInterface queryModelByRid(Integer rid, ProgressReporter progressReporter);
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
	
	public ProjectTree queryModelTree(Integer rid);
	public List<BuildingStorey> queryModelBuildingStorey(Integer rid);
	public List<BuildingCellContainer> queryBuildingCells(Integer rid);
	public List<PropertySet> queryProperty(Integer rid, Long oid);
}
