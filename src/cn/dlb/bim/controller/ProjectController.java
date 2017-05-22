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
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.service.ProjectService;
import cn.dlb.bim.utils.IdentifyManager;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.web.ResultUtil;

@Controller
@RequestMapping("/project/")
public class ProjectController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
	private static final String PIC_UPLOAD_PATH = "upload/pic/";
	
	@Autowired
	@Qualifier("ProjectServiceImpl")
	private ProjectService projectService;
	
	@Autowired
	@Qualifier("BimServiceImpl")
	private BimService bimService;
	
	@RequestMapping(value = "addProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addProject(Project project, @RequestParam(value = "file", required = false) MultipartFile pic,
			HttpServletRequest request) {
		ResultUtil result = new ResultUtil();
		
		String path = request.getSession().getServletContext().getRealPath(PIC_UPLOAD_PATH);
		String picName = pic.getOriginalFilename();
		String[] split = picName.split("\\.");
		String suffix = null;
		if (split.length >= 2) {
			suffix = split[split.length - 1];
		}
		if (suffix != null && isPicture(suffix)) {
			String newPicName = picName.substring(0, picName.lastIndexOf("."));
			newPicName += "-" + IdentifyManager.getIdentifyManager().nextId(IdentifyManager.PICTURE_KEY);
			newPicName += "." + suffix;
			File targetFile = new File(path, newPicName);
			if (!targetFile.exists()) {
				targetFile.mkdirs();
			}
			try {
				pic.transferTo(targetFile);
				project.setPicUrl(PIC_UPLOAD_PATH + targetFile.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		project.setPid(IdentifyManager.getIdentifyManager().nextId(IdentifyManager.PID_KEY));
		projectService.addProject(project);
		result.setSuccess(true);
		return result.getResult();
	}
	
	@RequestMapping(value = "deleteProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> deleteProject(@RequestParam("pid") Long pid) {
		ResultUtil result = new ResultUtil();
		projectService.deleteProject(pid);
		result.setSuccess(true);
		return result.getResult();
	}
	
	@RequestMapping(value = "updateProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateProject(Project project) {
		ResultUtil result = new ResultUtil();
		projectService.updateProject(project);
		result.setSuccess(true);
		return result.getResult();
	}
	
	@RequestMapping(value = "queryProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryProject(@RequestParam("pid") Long pid) {
		ResultUtil result = new ResultUtil();
		Project project = projectService.queryProject(pid);
		result.setSuccess(true);
		result.setData(project);
		return result.getResult();
	}
	
	@RequestMapping(value = "queryAllProject", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryAllProject() {
		ResultUtil result = new ResultUtil();
		List<Project> projectList = projectService.queryAllProject();
		result.setSuccess(true);
		result.setData(projectList);
		return result.getResult();
	}
	
	@RequestMapping(value = "queryProjectByRid", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> queryProjectByRid(@RequestParam("rid") Integer rid) {
		ResultUtil result = new ResultUtil();
		Project project = projectService.queryProjectByRid(rid);
		result.setSuccess(true);
		result.setData(project);
		return result.getResult();
	}
	
	private boolean isPicture(String picName) {
		 String imageArray [] = {  
		   "bmp", "dib", "gif",  
		   "jfif", "jpe", "jpeg",  
		   "jpg", "png", "tif",  
		   "tiff", "ico",  
		 };
		 for (String image : imageArray) {
			 if (image.equals(picName)) {
				 return true;
			 }
		 }
		 return false;
	}
}
