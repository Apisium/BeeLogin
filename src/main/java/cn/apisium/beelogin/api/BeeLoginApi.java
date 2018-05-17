package cn.apisium.beelogin.api;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import cn.apisium.beelogin.Main;
import cn.apisium.beelogin.request.Verify;

// TODO finish these stuffs, replace all stub methods to functional methods.
public class BeeLoginApi {

	static Map<String, JSONObject> loginStatue = new HashMap<>();

	static Map<String, String> uuidToken = new HashMap<>();
	static Map<String, String> nameToken = new HashMap<>();

	public static boolean joined(String token) {
		return loginStatue.containsKey(token);
	}

	public static boolean joined(UUID authedUUID, String authedName) {
		boolean joined = false;
		if (uuidToken.containsKey(authedUUID.toString())) {
			joined |= loginStatue.containsKey(uuidToken.get(authedUUID.toString()));
		}
		if (nameToken.containsKey(authedName)) {
			joined |= loginStatue.containsKey(nameToken.get(authedName));
		}
		return joined;
	}

	public static boolean authed(String token) {
		if (!token.startsWith(Main.tokenPerfix)) {
			return false;
		}
		if (loginStatue.containsKey(token)) {
			return true;
		}
		JSONObject result = new Verify(token).send();
		boolean authed = result.getBoolean("result");
		if (authed) {
			uuidToken.put(result.getString("uuid"), token);
			uuidToken.put(result.getString("name"), token);
		}
		return authed;
	}

	public static boolean authed(UUID authedUUID, String authedName) {
		if (!uuidToken.containsKey(authedUUID.toString()) || !nameToken.containsKey(authedName)) {
			return false;
		}
		String token;
		if (!(token = uuidToken.get(authedUUID.toString())).equals(nameToken.get(authedName))) {
			return false;
		}
		return authed(token);
	}

	public static void remove(UUID authedUUID, String authedName) {
		if (uuidToken.containsKey(authedUUID.toString())) {
			loginStatue.remove(uuidToken.get(authedUUID.toString()));
			uuidToken.remove(authedUUID.toString());
		}
		if (nameToken.containsKey(authedName)) {
			loginStatue.remove(nameToken.get(authedName));
			nameToken.remove(authedName);
		}
	}

	public static String getName(String token) {
		return "";
	}

	public static UUID getUUID(String token) {
		return UUID.nameUUIDFromBytes((token + ":APISIUM").getBytes(StandardCharsets.UTF_8));
	}
}
