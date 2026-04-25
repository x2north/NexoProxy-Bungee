package com.nexomc.nexoproxy

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command

class NexoProxyCommand(private val plugin: NexoProxy) : Command("nexoproxy", "nexoproxy.admin", "nxp") {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        when (args.firstOrNull()?.lowercase()) {
            "reload", "rl" -> plugin.reload(sender)
            "debug" -> {
                plugin.config = plugin.config.copy(debug = !plugin.config.debug)
                plugin.config.saveConfig(plugin.dataFolder.toPath())
                sender.sendMessage(TextComponent("[NexoProxy] Debug set to ${plugin.config.debug}"))
            }
            else -> sender.sendMessage(TextComponent("Usage: /nexoproxy reload|rl|debug"))
        }
    }
}
