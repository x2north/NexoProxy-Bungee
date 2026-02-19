package com.nexomc.nexoproxy;

import com.google.gson.JsonParser
import com.google.inject.Inject
import com.nexomc.nexoproxy.NexoPackHelpers.nexoObfuscationMappings
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerResourcePackSendEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.player.ResourcePackInfo
import org.slf4j.Logger


@Plugin(
    id = "nexoproxy", name = "NexoProxy", version = BuildConstants.VERSION, authors = ["boy0000"]
)
class NexoProxy @Inject constructor(val logger: Logger, val server: ProxyServer) {



    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        server.channelRegistrar.register(NexoPackHelpers.HASH_CHANNEL)
        server.eventManager.register(this, ResourcePackListener(logger))
    }


}

class ResourcePackListener(val logger: Logger) {

    @Subscribe
    fun PluginMessageEvent.onHashUplpoad() {
        if (identifier != NexoPackHelpers.HASH_CHANNEL) return


        // {"uuid": "X", "url": "y", "hash": "hash", "unobfuscated": "un-uuid"}
        val jsonObject = JsonParser.parseString(dataAsDataStream().readUTF()).asJsonObject
        val obfuscated = ObfuscatedResourcePack(jsonObject)
        nexoObfuscationMappings += obfuscated
        logger.warn(obfuscated.toString() + " has been recieved from ${this.source}")
        result = PluginMessageEvent.ForwardResult.handled()
    }

    @Subscribe
    fun DisconnectEvent.onDisconnect() {
        NexoPackHelpers.packHashTracker.remove(player.uniqueId)
        logger.warn("Removed tracking for ${player.uniqueId}")
    }

    @Subscribe
    fun ServerResourcePackSendEvent.onPackSend() {
        val obfPack = nexoObfuscationMappings.find {
            it.uuid == receivedResourcePack.id || it.unobfuscated == receivedResourcePack.id
        } ?: return

        if (receivedResourcePack.originalOrigin == ResourcePackInfo.Origin.PLUGIN_ON_PROXY)
        if (obfPack.unobfuscated == receivedResourcePack.id) result = ResultedEvent.GenericResult.denied()

        val builder = providedResourcePack.asBuilder(obfPack.url).setId(obfPack.uuid).setHash(obfPack.hash.toByteArray())

        providedResourcePack = builder.build()
        logger.warn("Intercepted RP and swapped from ${receivedResourcePack.id} to ${providedResourcePack.id}")
    }
}
