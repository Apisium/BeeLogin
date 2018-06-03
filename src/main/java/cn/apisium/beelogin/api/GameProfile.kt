package cn.apisium.beelogin.api

import java.util.UUID
import java.io.Serializable

class GameProfile(val id: UUID, val name: String, val token: String?) : Serializable
