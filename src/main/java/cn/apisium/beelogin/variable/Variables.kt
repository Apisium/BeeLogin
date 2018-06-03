package cn.apisium.beelogin.variable

import java.util.UUID

object Variables {
  var uuid: UUID = UUID.randomUUID()
  var protocol = "WebSocket"
  var protocolConfig = mapOf(Pair("url", "http://127.0.0.1:26437/api/v1/socket"))
  var debug = false
  var joinedMessage = "You has in this server."
  var unauthorizedMessage = "You have not logged in this server."
}
