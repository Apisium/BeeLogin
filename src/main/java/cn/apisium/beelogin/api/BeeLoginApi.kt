package cn.apisium.beelogin.api

import org.bukkit.Server
import java.util.UUID

abstract class BeeLoginApi(val config: Map<String, String>, val id: String, val server: Server) {
  val list = hashMapOf<String, GameProfile>()

  abstract fun join(token: String): GameProfile

  abstract fun quit(token: String)

  abstract fun find(token: String): GameProfile?

  abstract fun findByName(name: String): GameProfile?

  abstract fun findById(id: UUID): GameProfile?

  fun check(id: String): Boolean = list.containsKey(id)
}
