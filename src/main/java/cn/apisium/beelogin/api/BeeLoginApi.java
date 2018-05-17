package cn.apisium.beelogin.api;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// TODO finish these stuffs, replace all stub methods to functional methods.
public class BeeLoginApi {

	public static boolean authed(String token) {
		return false;
	}

	public static boolean authed(UUID authedUUID, String authedName) {
		return false;
	}

	public static String getName(String token) {
		return "";
	}

	public static UUID getUUID(String token) {
		return UUID.nameUUIDFromBytes((token + ":APISIUM").getBytes(StandardCharsets.UTF_8));
	}
}
