package cn.apisium.beelogin.request;

import org.json.JSONObject;

import cn.apisium.beelogin.variable.Variables;
import cn.apisium.util.http.JsonResponse;

public abstract class Requestable extends JSONObject {
	public abstract String url();

	protected String actualUrl() {
		return Variables.url.endsWith("/") ? Variables.url + url() : Variables.url + "/" + url();
	}

	public JSONObject send() {
		return JsonResponse.send(actualUrl(), this.put("secret", Variables.secret));
	}
}
