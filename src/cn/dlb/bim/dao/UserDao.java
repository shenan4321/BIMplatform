package cn.dlb.bim.dao;

import cn.dlb.bim.dao.entity.User;

public interface UserDao {
	public User queryUser(String name);
	public void addUser(User user);
	public void deleteUser(User user);
	public void updateUser(User user);
}
