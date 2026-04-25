package com.nexomc.nexoproxy.listeners

import com.google.gson.JsonParser
import com.nexomc.nexoproxy.NexoProxy
import com.nexomc.nexoproxy.pack.NexoPackHelpers
import com.nexomc.nexoproxy.pack.ResourcePackInfo
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class PackMessageListener(private val plugin: NexoProxy) : Listener {

    @EventHandler
    fun onPluginMessage(event: PluginMessageEvent) {
        if (!plugin.config.resourcePacks) return
        if (event.tag != NexoPackHelpers.PACK_HASH_CHANNEL) return
        if (event.sender !is Server) return

        runCatching {
            val json = JsonParser.parseString(event.data.decodeToString()).asJsonObject
            val pack = ResourcePackInfo(json)
            NexoPackHelpers.addMapping(pack)
            event.isCancelled = true

            if (plugin.config.debug) {
                val serverName = (event.sender as Server).info.name
                plugin.logger.info("Registered pack mapping: ${pack.unobfuscatedHash} -> ${pack.obfuscatedHash} from $serverName")
            }
        }.onFailure {
            plugin.logger.warning("Failed to parse pack mapping plugin message: ${it.message}")
        }
    }
}
