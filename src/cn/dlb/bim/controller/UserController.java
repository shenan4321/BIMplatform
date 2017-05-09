package cn.dlb.bim.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import cn.dlb.bim.dao.entity.User;
import cn.dlb.bim.service.UserService;
import cn.dlb.bim.utils.IdentifyManager;
import cn.dlb.bim.vo.UserVo;
import cn.dlb.bim.web.ResultUtil;
import cn.dlb.bim.web.UserNamePwdToken;

@Controller
@RequestMapping("/user")
@SessionAttributes("userToken")
public class UserController {
	
	@Autowired
	@Qualifier("UserServiceImpl")
	private UserService userService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome(HttpServletRequest request) {
		
		if (request.getSession().getAttribute("userToken") != null) {
			return "/index";
		}

		return "login";
	}

	@RequestMapping(value = "/toLogin", method = RequestMethod.GET)
	public String toLogin(HttpServletRequest request) {

		if (request.getSession().getAttribute("userToken") != null) {
			return "/index";
		}

		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> login(@RequestParam(required = true, value = "loginName") String loginName,
			@RequestParam(required = true, value = "pwd") String pwd, HttpServletRequest request) {
		ResultUtil result = new ResultUtil();
		try {
			User user = userService.queryUser(loginName);
			if (user != null && user.getPassword().equals(pwd)) {

				HttpSession session = request.getSession();

				UserNamePwdToken userToken = new UserNamePwdToken(user.getUserName(), user.getPassword());

				session.setAttribute("userToken", userToken);

				// 设置超时无效
				// session.setMaxInactiveInterval(20);
				result.setSuccess(true);
			} else {
				result.setSuccess(false);
				result.setMsg("username or password error");
			}

		} catch (Exception e) {
			result.setSuccess(false);
			result.setMsg("unhold error");
		}
		return result.getResult();
	}
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> register(UserVo userVo, HttpServletRequest request) {
		ResultUtil result = new ResultUtil();
		User user = userService.queryUser(userVo.getUserName());
		if (user != null) {
			result.setSuccess(false);
			result.setMsg("username is already used");
		} else {
			user = new User();
			user.setCompany(userVo.getCompany());
			user.setEmail(userVo.getEmail());
			user.setFirstName(userVo.getFirstName());
			user.setLastName(userVo.getLastName());
			user.setPassword(userVo.getPassword());
			user.setUserId(IdentifyManager.getIdentifyManager().nextId(IdentifyManager.USER_ID_KEY));
			user.setUserName(userVo.getUserName());
			userService.addUser(user);
			
			HttpSession session = request.getSession();
			UserNamePwdToken userToken = new UserNamePwdToken(user.getUserName(), user.getPassword());
			session.setAttribute("userToken", userToken);
			
			result.setSuccess(true);
		}
		return result.getResult();
	}
	
	@RequestMapping(value = "/updateUser", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateUser(UserVo userVo, HttpServletRequest request) {
		
		ResultUtil result = new ResultUtil();
		
		if (request.getSession().getAttribute("userToken") == null) {
			result.setSuccess(false);
			result.setMsg("please login");
			return result.getResult();
		}
		
		User user = userService.queryUser(userVo.getUserName());
		if (user != null) {
			result.setSuccess(false);
			result.setMsg("username is already used");
		} else {
			user = new User();
			user.setCompany(userVo.getCompany());
			user.setEmail(userVo.getEmail());
			user.setFirstName(userVo.getFirstName());
			user.setLastName(userVo.getLastName());
			user.setPassword(userVo.getPassword());
			user.setUserName(userVo.getUserName());
			
			UserNamePwdToken token = (UserNamePwdToken) request.getSession().getAttribute("userToken");
			token.setName(user.getUserName());
			token.setPassword(user.getPassword());
			
			userService.updateUser(user);
			result.setSuccess(true);
		}
		return result.getResult();
	}
}
