package cn.apisium.beelogin.request;

public class Quit extends Requestable {

	public Quit(String name) {
		this.put("name", name);
	}

	@Override
	public String url() {
		return "quit";
	}

}
