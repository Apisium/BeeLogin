package cn.apisium.beelogin;

import java.lang.reflect.Field;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import cn.apisium.beelogin.api.BeeLoginApi;
import cn.apisium.beelogin.variable.NonConfig;
import cn.apisium.beelogin.variable.Variables;

public class Main extends JavaPlugin {
	private final String tokenPerfix = "$A(";
	private final String kickedName = "$K(kicked";

	@Override
	public void onEnable() {
		getLogger().info("BeeLogin Reloaded has loaded");
	}

	public Main loadConfig() {// dirty reflection stuffs to load config
		Class<?> variables = Variables.class;
		for (Field variable : variables.getDeclaredFields()) {
			if (variable.isAnnotationPresent(NonConfig.class)) {
				continue;
			}
			String path = capitalFirst(variable.getName());
			try {
				Object defaultValue = variable.get(null);
				Object config = this.getConfig().get(path, null);
				if (config != null)
					variable.set(null, config);
				else if (defaultValue != null)
					this.getConfig().set(path, defaultValue);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		this.saveConfig();
		return this;
	}

	public String capitalFirst(String string) {
		char[] cs = string.toCharArray();
		cs[0] = Character.toUpperCase(cs[0]);
		return String.valueOf(cs);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onAuth(AsyncPlayerPreLoginEvent event) {
		if (event.getName().equalsIgnoreCase(kickedName)) {// well, at this stage it should not happened actually, just
															// for making sure...
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage(Variables.unauthorizedMessage);
		} else if (event.getName().startsWith(tokenPerfix)) {// kicked as what the name said
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage(Variables.tokenKickMessage);
		} else if (!BeeLoginApi.authed(event.getUniqueId(), event.getName())) {// kicked if unauthorized
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage(Variables.unauthorizedMessage);
		}
	}
}
