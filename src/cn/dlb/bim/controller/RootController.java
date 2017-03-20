package cn.dlb.bim.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.dlb.bim.ifc.GeometryGenerator;
import cn.dlb.bim.service.IBimService;


@Controller
@RequestMapping("/")
public class RootController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RootController.class);
	
	@Autowired
	@Qualifier("BimService")
	private IBimService bimService;

	@RequestMapping("index")
	public String index() {
		return "index";
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

}