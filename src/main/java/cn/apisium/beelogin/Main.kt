package cn.apisium.beelogin

import cn.apisium.beelogin.api.AuthException
import cn.apisium.beelogin.api.BeeLoginApi
import cn.apisium.beelogin.protocol.WebSocket
import cn.apisium.beelogin.variable.NonConfig
import cn.apisium.beelogin.variable.Variables
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result
import org.bukkit.plugin.java.JavaPlugin
import cn.apisium.util.bukkit.NmsHelper
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.reflect.Method

val OfflinePlayer.beeloginToken: String?
  get() {
    return Main.protocol?.findById(this.uniqueId)?.token
  }

class Main : JavaPlugin() {
  private val disconnect: Method
  private val gameProfile = Class.forName("com.mojang.authlib.GameProfile").constructors[0]

  init {
    try {
      disconnect = NmsHelper
        .getNmsClass("LoginListener")
        .getMethod("disconnect", String::class.java)
    } catch (e: Exception) {
      throw RuntimeException("Unknown exception, possibily because it is not a craftbukkit implantation.", e)
    }
  }

  override fun onEnable() {
    loadConfig()
    registerProtocol("WebSocket", WebSocket::class.java)
    protocol = PROTOCOLS[Variables.protocol]!!
      .getConstructor(Map::class.java, String::class.java, Server::class.java)
      .newInstance(Variables.protocolConfig, Variables.uuid, server)

    logger.info("BeeLogin Reloaded has loaded")
  }

  fun loadConfig(): Main {// dirty reflection stuffs to load config
    val variables = Variables::class.java
    for (variable in variables.declaredFields) {
      if (variable.isAnnotationPresent(NonConfig::class.java)) {
        continue
      }
      val name = variable.name
      val path = name.first().toUpperCase() + name.substring(1)
      try {
        val defaultValue = variable.get(null)
        val config = this.config.get(path, null)
        if (config != null)
          variable.set(null, config)
        else if (defaultValue != null)
          this.config.set(path, defaultValue)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    this.saveConfig()
    return this
  }

  @EventHandler
  private fun onQuit(event: PlayerQuitEvent) {
    val token = event.player.beeloginToken
    if (token != null) protocol?.quit(token)
  }

  @EventHandler(priority = EventPriority.LOWEST)
  private fun onAuth(event: AsyncPlayerPreLoginEvent) {
    var login: Any? = null
    try {
      val listeners = NmsHelper.getPossibleLoginListeners(event.address, event.uniqueId)
      if (listeners.isEmpty()) return
      login = listeners.first()
      listeners.drop(0).forEach {
        try {
          disconnect.invoke(it, Variables.unauthorizedMessage)
        } catch (ignored: Exception) {
        }
      }

      if (protocol!!.check(event.name)) {
        event.kickMessage = Variables.joinedMessage
        event.loginResult = Result.KICK_OTHER
        return
      }
      val f = NmsHelper.findFirstFieldByType(login.javaClass)!!
      f.isAccessible = true
      val profile = try {
        protocol!!.join(event.name)
      } catch (e: AuthException) {
        event.kickMessage = e.message
        event.loginResult = Result.KICK_OTHER
        return
      }
      f.set(login, gameProfile.newInstance(profile.id, profile.name))
    } catch (e: Throwable) {
      event.loginResult = Result.KICK_OTHER
      event.kickMessage = Variables.unauthorizedMessage
      if (login != null) disconnect.invoke(login, "Internal error")
      throw RuntimeException(
        "Can not change GameProfile instance, possibily because it is not a craftbukkit implantation")
    }
  }

  companion object {
    internal var protocol: BeeLoginApi? = null
    private val PROTOCOLS = hashMapOf<String, Class<out BeeLoginApi>>()
    fun registerProtocol(name: String, clazz: Class<out BeeLoginApi>) = PROTOCOLS.put(name, clazz)
  }
}
