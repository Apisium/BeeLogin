package cn.apisium.beelogin.protocol

import cn.apisium.beelogin.api.AuthException
import cn.apisium.beelogin.api.BeeLoginApi
import cn.apisium.beelogin.api.GameProfile
import com.alibaba.fastjson.JSONObject
import io.socket.client.IO
import io.socket.client.Ack
import org.bukkit.Server
import java.net.URI
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocket(config: Map<String, String>, id: String, server: Server) : BeeLoginApi(config, id, server) {
  private val io = URI(config["url"]).let {
    val opts = IO.Options()
    opts.path = it.path
    opts.port = it.port
    opts.query = "id=$id"
    val io = IO.socket(
      "${it.scheme}://${it.host}",
      opts
    )
    io
      .on("kick", {
        val profile = this.list[JSONObject.parseObject(it[0] as String).getString("token")]
        if (profile != null) try {
          server.getPlayer(profile.id).kickPlayer("This name has been logined again.")
        } catch (ignored: Exception) {
        }
      })
    io.connect()
  }

  override fun join(token: String): GameProfile {
    val values = LinkedBlockingQueue<GameProfile>()
    io.emit("join", "{\"token\":\"$token\"}", Ack({
      val json = JSONObject.parseObject(it[0] as String)
      if (json.getBoolean("error")) throw AuthException(json.getString("reason"))
      val data = json.getJSONObject("data")
      values.offer(GameProfile(UUID.fromString(data.getString("id")), data.getString("name"), token))
    }))
    val result = values.poll(10, TimeUnit.SECONDS) ?: throw AuthException("Timeout")
    this.list[token] = result
    return result
  }

  override fun quit(token: String) {
    io.emit("quit", "{\"token\":\"$token\"}")
    this.list.remove(token)
  }

  override fun find(token: String): GameProfile? = this.list[token] ?: query("{\"token\":\"$token\"}")

  override fun findById(id: UUID): GameProfile? =
    this.list.values.find { it.id == id } ?: query("{\"id\":\"$id\"}")

  override fun findByName(name: String): GameProfile? =
    this.list.values.find { it.name == name } ?: query("{\"name\":\"$name\"}")

  private fun query(query: String): GameProfile? {
    val values = LinkedBlockingQueue<GameProfile>()
    io.emit("find", query, Ack({
      val json = JSONObject.parseObject(it[0] as String)
      if (!json.getBoolean("error")) {
        val data = json.getJSONObject("data")
        values.offer(GameProfile(UUID.fromString(
          data.getString("id")),
          data.getString("name"),
          json.getString("token")
        ))
      }
    }))
    return values.poll(10, TimeUnit.SECONDS)
  }
}