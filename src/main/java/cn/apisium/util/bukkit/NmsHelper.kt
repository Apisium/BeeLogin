package cn.apisium.util.bukkit

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.ArrayList
import java.util.Collections
import java.util.UUID

import com.google.common.collect.Lists
import org.bukkit.Bukkit

object NmsHelper {
  private val networkManagers: List<*>
    get() {
      var managers: List<*> = Collections.synchronizedList(Lists.newArrayList<Any>())
      for (f in serverConnection.javaClass.declaredFields) {
        if (!f.isAccessible) {
          f.isAccessible = true
        }
        try {
          if (f.type.isAssignableFrom(managers.javaClass)) {
            val original = f.get(nmsServer) as List<*>
            ((f.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)
              .isAssignableFrom(NmsHelper.getNmsClass("NetworkManager"))
            managers = original
            break
          }
        } catch (ignored: Exception) {
          throw RuntimeException(
            "Can not get NetworkManager instance,  possibily because it is not a craftbukkit implantation")
        }
      }
      return managers
    }

  private val serverConnection: Any
    get() {
      val mc = nmsServer
      var serverConnection: Any? = null
      for (f in mc.javaClass.declaredFields) {
        if (!f.isAccessible) {
          f.isAccessible = true
        }
        try {
          if (!f.type.isAssignableFrom(NmsHelper.getNmsClass("ServerConnection"))) {
            continue
          }
          serverConnection = f.get(mc)
          break
        } catch (ignored: Exception) {
          throw RuntimeException(
            "Can not get ServerConnection instance,  possibily because it is not a craftbukkit implantation")
        }
      }
      if (serverConnection == null) {
        throw RuntimeException(
          "Can not get ServerConnection instance,  possibily because it is not a craftbukkit implantation")
      }
      return serverConnection
    }

  private val nmsServer: Any
    get() {
      try {
        val craftServerClass = getCraftClass("CraftServer")
        val craftServer = craftServerClass.cast(Bukkit.getServer())
        val nmsServerClass = craftServer.javaClass
        val getServerMethod = nmsServerClass.getDeclaredMethod("getServer", *arrayOfNulls(0))
        if (!getServerMethod.isAccessible) {
          getServerMethod.isAccessible = true
        }
        return getServerMethod.invoke(craftServer, *arrayOfNulls(0))
      } catch (ignored: Exception) {
        throw RuntimeException(
          "Can not find MinecraftServer instance, possibily because it is not a craftbukkit implantation")
      }
    }

  private fun findMethodByType(original: Class<*>, vararg parameters: Class<*>): Method? {
    Methods@ for (method in original.declaredMethods) {
      if (method.returnType.name == original.name && parameters.size == method.parameterCount) {
        for (i in parameters.indices) {
          if (parameters[i].name != method.parameters[i].name) {
            continue@Methods
          }
        }
        return method
      }
    }
    return null
  }

  fun findFirstFieldByType(original: Class<*>): Field? {
    for (method in original.declaredFields) {
      if (method.type.name == original.name)
        return method
    }
    return null
  }

  fun getPossibleLoginListeners(address: InetAddress, uuid: UUID): List<Any> {
    val listeners = ArrayList<Any>()
    var loginListener: Class<*>
    var login: Any?
    for (manager in networkManagers) {
      try {
        loginListener = NmsHelper.getNmsClass("LoginListener")
        if ((NmsHelper.findFirstFieldByType(manager!!::class.java)!!
            .get(manager) as InetSocketAddress).address != address) {
          continue
        }
        if (NmsHelper.findFirstFieldByType(manager::class.java)!!.get(manager) != uuid) {
          continue
        }
        val packetListenerClass = NmsHelper.getNmsClass("PacketListener")
        login = NmsHelper.findMethodByType(manager.javaClass, packetListenerClass)!!
          .invoke(manager, *arrayOfNulls(0))
        listeners.add(login)
        if (!loginListener.isInstance(login)) {
          continue
        }
      } catch (e: Exception) {
        throw RuntimeException(
          "Can not get LoginListener instance,  possibily because it is not a craftbukkit implantation")
      }
    }
    if (listeners.isEmpty())
      throw RuntimeException(
        "Can not get LoginListener instance,  possibily because it is not a craftbukkit implantation")
    return listeners
  }

  @Throws(ClassNotFoundException::class)
  private fun getCraftClass(className: String): Class<*> {
    return getClass("org.bukkit.craftbukkit", className)
  }

  @Throws(ClassNotFoundException::class)
  private fun getClass(packagePrefix: String, className: String): Class<*> {
    val version = Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3] + "."
    val name = if (packagePrefix.endsWith(".")) packagePrefix else "$packagePrefix.$version$className"
    return Class.forName(name)
  }

  @Throws(ClassNotFoundException::class)
  fun getNmsClass(className: String): Class<*> {
    return getClass("net.minecraft.server", className)
  }
}