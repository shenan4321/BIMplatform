package cn.dlb.bim.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.database.queries.om.JsonQueryObjectModelConverter;
import cn.dlb.bim.ifc.database.queries.om.Query;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.service.IBimService;
import cn.dlb.bim.vo.GlbVo;

@Controller
@RequestMapping("/")
public class RootController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RootController.class);

	@Autowired
	@Qualifier("BimService")
	private IBimService bimService;
	
	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;

	@RequestMapping("index")
	public String index() {
		return "index";
	}

	// 前端路由专用页面
	@RequestMapping("app")
	public String app() {
		return "app/index.jsp";
	}

	@RequestMapping(value = "queryAllIfcModel", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> queryAllIfcModel() {
		LOGGER.info("call queryAllIfcModel");
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("success", "true");
		resMap.put("models", bimService.queryAllIfcModel());
		return resMap;
	}

	@RequestMapping(value = "queryGeometryInfo", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> queryGeometryInfo() {
		LOGGER.info("call queryGeometryInfo");
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("success", "true");
		resMap.put("geometries", bimService.queryGeometryInfo());
		return resMap;
	}

	@RequestMapping(value = "queryDbGeometryInfo", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> queryDbGeometryInfo(@RequestParam("rid") Integer rid) {
		LOGGER.info("call queryDbGeometryInfo");
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("success", "true");
		resMap.put("geometries", bimService.queryDbGeometryInfo(rid));
		return resMap;
	}

	@RequestMapping(value = "uploadAndDeserializeSave", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> upload(@RequestParam(value = "file", required = false) MultipartFile file,
			HttpServletRequest request// , ModelMap model
	) {

		LOGGER.info("upload file");
		String path = request.getSession().getServletContext().getRealPath("upload");
		String fileName = file.getOriginalFilename();
		LOGGER.info("file path: " + path);
		File targetFile = new File(path, fileName);
		if (!targetFile.exists()) {
			targetFile.mkdirs();
		}

		// 保存
		try {
			file.transferTo(targetFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int rid = bimService.deserializeModelFileAndSave(targetFile);
		// model.addAttribute("fileUrl", request.getContextPath() +
		// "/upload/"+fileName);
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("success", "true");
		return resMap;
	}
	
	@RequestMapping(value = "jsonApi", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> jsonApi(@RequestBody ObjectNode jsonNode) {
		PackageMetaData packageMetaData = server.getMetaDataManager()
				.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		JsonQueryObjectModelConverter converter = new JsonQueryObjectModelConverter(packageMetaData);
		Query query = null;
		try {
			query = converter.parseJson("query", jsonNode);
		} catch (QueryException e) {
			e.printStackTrace();
		}
		Map<String, Object> resMap = new HashMap<String, Object>();
		return resMap;
	}
	
	@RequestMapping(value = "queryGlbByRid", method = RequestMethod.GET)
	@ResponseBody
	public GlbVo queryGlbByRid(@RequestParam("rid")Integer rid) {
		GlbVo glbVo = bimService.queryGlbByRid(rid);
		return glbVo;
	}

}