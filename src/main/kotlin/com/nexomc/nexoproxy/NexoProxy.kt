package com.nexomc.nexoproxy

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.nexomc.nexoproxy.glyphs.GlyphStore
import com.nexomc.nexoproxy.listeners.GlyphMessageListener
import com.nexomc.nexoproxy.listeners.PackMessageListener
import com.nexomc.nexoproxy.listeners.PlayerConnectionListener
import com.nexomc.nexoproxy.pack.NexoPackHelpers
import com.nexomc.nexoproxy.pack.ResourcePackInfo
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.CommandSender
import org.bstats.bungeecord.Metrics
import java.nio.file.Files

class NexoProxy : Plugin() {

    lateinit var config: NexoConfig
        internal set

    private val packsFile get() = dataFolder.toPath().resolve(".packs.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onEnable() {
        Metrics(this, 30155)
        config = NexoConfig.loadConfig(dataFolder.toPath())
        loadPacks()
        GlyphStore.enabled = config.glyphs

        val pluginManager: PluginManager = proxy.pluginManager
        pluginManager.registerListener(this, PlayerConnectionListener(this))
        pluginManager.registerListener(this, PackMessageListener(this))
        pluginManager.registerListener(this, GlyphMessageListener(this))
        pluginManager.registerCommand(this, NexoProxyCommand(this))

        proxy.registerChannel(NexoPackHelpers.PACK_HASH_CHANNEL)
        proxy.registerChannel(GlyphStore.GLYPH_CHANNEL)
        proxy.registerChannel(HANDSHAKE_CHANNEL)

        logger.info("NexoProxy enabled on BungeeCord")
    }

    override fun onDisable() {
        savePacks()
        proxy.unregisterChannel(NexoPackHelpers.PACK_HASH_CHANNEL)
        proxy.unregisterChannel(GlyphStore.GLYPH_CHANNEL)
        proxy.unregisterChannel(HANDSHAKE_CHANNEL)
    }

    fun reload(source: CommandSender) {
        savePacks()
        config = NexoConfig.loadConfig(dataFolder.toPath())
        GlyphStore.enabled = config.glyphs
        source.sendMessage(TextComponent("[NexoProxy] Reloaded config and saved pack cache."))
        logger.info("Reloaded by ${source.name}")
    }

    private fun loadPacks() {
        if (Files.notExists(packsFile)) return
        runCatching {
            val array = JsonParser.parseReader(packsFile.toFile().reader()).asJsonArray
            for (element in array) {
                NexoPackHelpers.addMapping(ResourcePackInfo(element.asJsonObject))
            }
            logger.info("Loaded ${array.size()} cached pack mapping(s) from ${packsFile.fileName}")
        }.onFailure { logger.warning("Failed to load pack cache: ${it.message}") }
    }

    private fun savePacks() {
        runCatching {
            Files.createDirectories(dataFolder.toPath())
            val entries = NexoPackHelpers.allMappings.toList().takeLast(20)
            val array = JsonArray()
            entries.forEach { array.add(it.toJson()) }
            packsFile.toFile().writeText(gson.toJson(array))
            logger.info("Saved ${entries.size} pack mapping(s) to ${packsFile.fileName}")
        }.onFailure { logger.warning("Failed to save pack cache: ${it.message}") }
    }

    companion object {
        const val HANDSHAKE_CHANNEL = "nexo:proxy_handshake"
    }
}
