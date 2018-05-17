package cn.apisium.beelogin.request;

public class Login extends Requestable {

	public Login(String name, String password) {
		this.put("name", name).put("password", password);
	}

	@Override
	public String url() {
		return "login";
	}

}
