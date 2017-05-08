package cn.dlb.bim.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.dlb.bim.dao.entity.Project;
import cn.dlb.bim.ifc.collada.KmzSerializer;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.tree.ProjectTree;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.service.ProjectService;
import cn.dlb.bim.vo.GlbVo;

@Controller
@RequestMapping("/model/")
public class ModelController {
	
	@Autowired
	@Qualifier("BimServiceImpl")
	private BimService bimService;
	
	@Autowired
	@Qualifier("ProjectServiceImpl")
	private ProjectService projectService;
	
	@RequestMapping(value = "addRevision", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addRevision(@RequestParam("pid") Long pid, @RequestParam(value = "file", required = true) MultipartFile file,
			HttpServletRequest request// , ModelMap model
	) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		Project project = projectService.queryProject(pid);
		if (project == null) {
			resMap.put("error", true);
			resMap.put("msg", "project with pid = " + pid + " is null");
			return resMap;
		}
		String path = request.getSession().getServletContext().getRealPath("upload/ifc/");
		String fileName = file.getOriginalFilename();
		String[] split = fileName.split("\\.");
		String suffix = null;
		if (split.length >= 2) {
			suffix = split[split.length - 1];
		}
		if (suffix == null || !suffix.equals("ifc")) {
			resMap.put("error", true);
			resMap.put("msg", "suffix : " + suffix + " is not be supported");
			return resMap;
		} 
		String newFileName = fileName.substring(0, fileName.lastIndexOf("."));
		newFileName += "-" + System.currentTimeMillis();
		newFileName += "." + suffix;
		File targetFile = new File(path, newFileName);
		if (!targetFile.exists()) {
			targetFile.mkdirs();
		}
		try {
			file.transferTo(targetFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int rid = bimService.addRevision(pid, targetFile);
		resMap.put("success", "true");
		resMap.put("rid", rid);
		return resMap;
	}
	
	@RequestMapping(value = "queryGeometryInfo", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryGeometryInfo(@RequestParam("rid") Integer rid) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("success", true);
		resMap.put("geometries", bimService.queryGeometryInfo(rid));
		return resMap;
	}
	
	@RequestMapping(value = "queryModelTree", method = RequestMethod.GET)
	public ProjectTree queryModelTree(@RequestParam("rid")Integer rid) {
		IfcModelInterface model = bimService.queryModelByRid(rid);
		ProjectTree tree = new ProjectTree(model.getPackageMetaData());
		tree.buildProjectTree(model);
		return tree;
	}
	
	@RequestMapping(value = "queryGlbByRid", method = RequestMethod.POST)
	public void queryGlbByRid(@RequestParam("rid")Integer rid, HttpServletResponse response) {
		GlbVo glbVo = bimService.queryGlbByRid(rid);
		try {
			OutputStream os = response.getOutputStream();
			os.write(glbVo.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "queryGlbLonlatByRid", method = RequestMethod.POST)
	@ResponseBody
	public Vector3d queryGlbLonlatByRid(@RequestParam("rid")Integer rid) {
		return bimService.queryGlbLonlatByRid(rid);
	}
	
	@RequestMapping(value = "kml", method = RequestMethod.GET)
	public void kml(@RequestParam("rid")Integer rid) {
		IfcModelInterface model = bimService.queryModelByRid(rid);
		KmzSerializer serializer = new KmzSerializer();
		ProjectInfo projectInfo = new ProjectInfo();
		projectInfo.setName("bim");
		projectInfo.setAuthorName("linfujun");
		try {
			serializer.init(model, projectInfo, true);
			serializer.writeToFile(new File("test.kmz").toPath(), null);
		} catch (SerializerException e) {
			e.printStackTrace();
		}
	}
}
