package cn.dlb.bim.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.dlb.bim.dao.entity.Project;
import cn.dlb.bim.service.ProjectService;
import cn.dlb.bim.utils.IdentifyUtil;
import cn.dlb.bim.utils.PicIdentifyUtil;
import cn.dlb.bim.utils.PidUtil;

@Controller
@RequestMapping("/project/")
public class ProjectController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
	
	@Autowired
	@Qualifier("ProjectServiceImpl")
	private ProjectService projectService;
	
	@RequestMapping(value = "addProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addProject(Project project, @RequestParam(value = "file", required = false) MultipartFile pic,
			HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String path = request.getSession().getServletContext().getRealPath("upload/pic/");
		String picName = pic.getOriginalFilename();
		String[] split = picName.split("\\.");
		String suffix = null;
		if (split.length >= 2) {
			suffix = split[split.length - 1];
		}
		if (suffix != null && isPicture(suffix)) {
			String newPicName = picName.substring(0, picName.lastIndexOf("."));
			newPicName += "-" + PicIdentifyUtil.nextId();
			newPicName += "." + suffix;
			File targetFile = new File(path, newPicName);
			if (!targetFile.exists()) {
				targetFile.mkdirs();
			}
			try {
				pic.transferTo(targetFile);
				project.setPicUrl(targetFile.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		project.setPid(PidUtil.nextId());
		projectService.addProject(project);
		result.put("success", true);
		return result;
	}
	
	@RequestMapping(value = "deleteProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> deleteProject(@RequestParam("pid") Long pid) {
		Map<String, Object> result = new HashMap<String, Object>();
		projectService.deleteProject(pid);
		result.put("success", true);
		return result;
	}
	
	@RequestMapping(value = "updateProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateProject(Project project) {
		Map<String, Object> result = new HashMap<String, Object>();
		projectService.updateProject(project);
		result.put("success", true);
		return result;
	}
	
	@RequestMapping(value = "queryProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryProject(@RequestParam("pid") Long pid) {
		Map<String, Object> result = new HashMap<String, Object>();
		projectService.queryProject(pid);
		result.put("success", true);
		return result;
	}
	
	@RequestMapping(value = "queryAllProject", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> queryAllProject() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Project> projectList = projectService.queryAllProject();
		result.put("success", true);
		result.put("projects", projectList);
		return result;
	}
	
	private boolean isPicture(String picName) {
		 String imageArray [] = {  
		   "bmp", "dib", "gif",  
		   "jfif", "jpe", "jpeg",  
		   "jpg", "png", "tif",  
		   "tiff", "ico",  
		 };
		 for (String image : imageArray) {
			 if (image.equals("picName")) {
				 return true;
			 }
		 }
		 return false;
	}
}
