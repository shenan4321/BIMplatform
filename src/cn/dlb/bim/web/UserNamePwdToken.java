package cn.dlb.bim.web;

public class UserNamePwdToken {
	
	private String name;
	private String password;
	
	public UserNamePwdToken(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
