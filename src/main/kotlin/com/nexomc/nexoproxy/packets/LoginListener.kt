package com.nexomc.nexoproxy.packets

import com.nexomc.nexoproxy.NexoConfig
import com.nexomc.nexoproxy.NexoProxy
import com.velocitypowered.api.event.AwaitingEventExecutor
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.network.Connections
import org.slf4j.Logger

/**
 * Async logic here inspired by https://github.com/4drian3d/VPacketEvents
 */
class LoginListener(val plugin: NexoProxy) : AwaitingEventExecutor<LoginEvent> {

    override fun executeAsync(event: LoginEvent): EventTask? {
        if (!plugin.config.glyphs) return null
        return EventTask.async { injectPlayer(event.player) }
    }

    private fun injectPlayer(player: Player?) {
        val channel = (player as ConnectedPlayer).connection.channel
        val handler = NexoChannelHandler(player, plugin)
        channel.pipeline().addBefore(Connections.HANDLER, NexoChannelHandler.PACKET_KEY, handler)
        if (plugin.config.debug) plugin.logger.info("Injected ${player.username}")
    }
}