package cn.dlb.bim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.UserDao;
import cn.dlb.bim.dao.entity.User;
import cn.dlb.bim.service.UserService;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {
	
	@Autowired
	@Qualifier("UserDaoImpl")
	private UserDao userDao;

	@Override
	public User queryUser(String name) {
		return userDao.queryUser(name);
	}

	@Override
	public void addUser(User user) {
		userDao.addUser(user);
	}

	@Override
	public void deleteUser(User user) {
		userDao.deleteUser(user);
	}

	@Override
	public void updateUser(User user) {
		userDao.updateUser(user);
	}

}
