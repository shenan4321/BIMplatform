package cn.dlb.bim.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.UserDao;
import cn.dlb.bim.dao.entity.User;

@Repository("UserDaoImpl")
public class UserDaoImpl implements UserDao {
	
	@Autowired  
    private MongoTemplate mongoTemplate; 

	@Override
	public User queryUser(String name) {
		Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, User.class);
	}

	@Override
	public void addUser(User user) {
		mongoTemplate.insert(user);
	}

	@Override
	public void deleteUser(User user) {
		mongoTemplate.remove(user);
	}

	@SuppressWarnings("static-access")
	@Override
	public void updateUser(User user) {
		Query query = new Query();
		query.addCriteria(Criteria.where("userId").is(user.getUserId()));
		mongoTemplate.updateFirst(query, Update.update("company", user.getCompany())
				.update("email", user.getEmail())
				.update("firstName", user.getFirstName())
				.update("lastName", user.getLastName())
				.update("password", user.getPassword())
				.update("userName", user.getUserName()), User.class);
	}
	
}
