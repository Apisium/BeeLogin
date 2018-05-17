package cn.apisium.beelogin.request;

public class Register extends Requestable {

	public Register(String name, String password) {
		this.put("name", name).put("password", password);
	}

	@Override
	public String url() {
		return "register";
	}

}
