package com.nexomc.nexoproxy.listeners

import com.nexomc.nexoproxy.NexoProxy
import com.nexomc.nexoproxy.pack.NexoPackHelpers
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.api.plugin.Listener

class PlayerConnectionListener(private val plugin: NexoProxy) : Listener {

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        val server = event.server ?: return
        server.sendData(NexoProxy.HANDSHAKE_CHANNEL, ByteArray(0))
    }

    @EventHandler
    fun onDisconnect(event: PlayerDisconnectEvent) {
        NexoPackHelpers.packHashTracker.remove(event.player.uniqueId)
    }
}
