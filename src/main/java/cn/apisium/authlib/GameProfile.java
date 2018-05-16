package cn.apisium.authlib;

import java.util.UUID;

import cn.apisium.beelogin.api.BeeLoginApi;

public class GameProfile {
	public static UUID getID(UUID originalID, String originalName) {
		if (BeeLoginApi.authed(originalName)) {
			return BeeLoginApi.getUUID(originalName);
		}
		return originalID;
	}

	public static String getName(UUID originalID, String originalName) {
		if (BeeLoginApi.authed(originalName)) {
			return BeeLoginApi.getName(originalName);
		}
		return originalName;
	}
}
