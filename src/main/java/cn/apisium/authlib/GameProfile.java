package cn.apisium.authlib;

import java.lang.instrument.Instrumentation;
import java.util.UUID;

import cn.apisium.authlib.transformer.GameProfileTransformer;
import cn.apisium.beelogin.api.BeeLoginApi;

public class GameProfile {

	public static void premain(String agentOps, Instrumentation inst) {
		inst.addTransformer(new GameProfileTransformer());
		System.out.println("BeeLogin Agent loaded");
	}

	public static UUID getID(UUID originalID, String originalName) {
		System.out.println(originalID);
		if (originalName != null && BeeLoginApi.authed(originalName)) {
			return BeeLoginApi.getUUID(originalName);
		}
		return originalID;
	}

	public static String getName(UUID originalID, String originalName) {
		System.out.println(originalName);
		if (originalName != null && BeeLoginApi.authed(originalName)) {
			return BeeLoginApi.getName(originalName);
		}
		return originalName;
	}
}
