package cn.apisium.beelogin.request;

public class Verify extends Requestable {

	public Verify(String token) {
		this.put("token", token);
	}

	@Override
	public String url() {
		return "verify";
	}
}
