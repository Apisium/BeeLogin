package cn.apisium.util.http;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

public class JsonResponse {
	public static JSONObject send(String url, JSONObject data) {
		try {
			return new JSONObject(getRawResponse(url, data).asString());
		} catch (IOException e) {
			return new JSONObject().put("error", true).put("data", new JSONObject().put("error", e.getClass())
					.put("message", e.getMessage()).put("localized", e.getLocalizedMessage()));
		}
	}

	public static Content getRawResponse(String url, JSONObject data) throws ClientProtocolException, IOException {
		return Request.Post(url).bodyString(data.toString(), ContentType.APPLICATION_JSON).execute().returnContent();
	}
}
