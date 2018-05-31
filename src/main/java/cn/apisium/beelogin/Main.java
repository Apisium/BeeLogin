package cn.apisium.beelogin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import cn.apisium.authlib.GameProfile;
import cn.apisium.beelogin.api.BeeLoginApi;
import cn.apisium.beelogin.variable.NonConfig;
import cn.apisium.beelogin.variable.Variables;
import net.minecraft.server.v1_12_R1.LoginListener;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.PacketListener;
import net.minecraft.server.v1_12_R1.ServerConnection;

public class Main extends JavaPlugin {
	public static final String tokenPerfix = "$A(";
	public static final String kickedName = "$K(kicked";

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

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAuth(AsyncPlayerPreLoginEvent event) {
		MinecraftServer mc = ((CraftServer) (this.getServer())).getServer();
		Object serverConnection = null;
		for (Field f : mc.getClass().getDeclaredFields()) {
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			try {
				if (!(f.getType().isAssignableFrom(ServerConnection.class))) {
					continue;
				}
				serverConnection = f.get(mc);
				return;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		List<NetworkManager> mangers = Collections.synchronizedList(Lists.newArrayList());

		for (Field f : serverConnection.getClass().getDeclaredFields()) {
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}
			try {
				if ((f.getType().isAssignableFrom(mangers.getClass()))) {
					List<?> original = (List<?>) f.get(mc);
					((Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0])
							.isAssignableFrom(NetworkManager.class);
					mangers = (List<NetworkManager>) original;
					return;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		for (NetworkManager manager : mangers) {
			if (!((InetSocketAddress) manager.l).getAddress().equals(event.getAddress())) {
				continue;
			}
			PacketListener p = manager.i();
			if (!(p instanceof LoginListener)) {
				continue;
			}
			LoginListener login = (LoginListener) p;
			for (Field f : login.getClass().getDeclaredFields()) {
				if (f.getType().isAssignableFrom(GameProfile.class)) {
					f.setAccessible(true);
					try {
						f.set(login,
								new com.mojang.authlib.GameProfile(
										GameProfile.getID(event.getUniqueId(), event.getName()),
										GameProfile.getName(event.getUniqueId(), event.getName())));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
