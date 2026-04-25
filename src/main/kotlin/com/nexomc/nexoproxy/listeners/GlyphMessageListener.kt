package com.nexomc.nexoproxy.listeners

import com.google.gson.JsonParser
import com.nexomc.nexoproxy.NexoProxy
import com.nexomc.nexoproxy.glyphs.GlyphStore
import com.nexomc.nexoproxy.glyphs.ProxyGlyph
import com.nexomc.nexoproxy.glyphs.Shift
import net.kyori.adventure.key.Key
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import team.unnamed.creative.serialize.minecraft.font.FontSerializer

class GlyphMessageListener(private val plugin: NexoProxy) : Listener {

    @EventHandler
    fun onPluginMessage(event: PluginMessageEvent) {
        if (!plugin.config.glyphs) return
        if (event.tag != GlyphStore.GLYPH_CHANNEL) return
        if (event.sender !is Server) return

        runCatching {
            val json = JsonParser.parseString(event.data.decodeToString()).asJsonObject

            json.remove("__shift_font")?.asJsonObject?.let {
                val key = Key.key(it["key"].asString)
                val font = it["font"].asJsonObject.toString()
                Shift.shiftFont = FontSerializer.INSTANCE.deserializeFromJsonString(font, key)
            }

            var count = 0
            json.entrySet().forEach { (id, glyphEl) ->
                val obj = glyphEl.asJsonObject
                GlyphStore.glyphs[id] = ProxyGlyph(
                    id = id,
                    font = Key.key(obj.get("font").asString),
                    unicodes = obj.getAsJsonArray("unicodes").map { it.asString },
                    defaultColor = obj.get("color")?.takeUnless { it.isJsonNull }?.asInt,
                    defaultShadowColor = obj.get("shadow")?.takeUnless { it.isJsonNull }?.asInt,
                    permission = obj.get("permission")?.asString ?: "",
                    placeholders = obj.getAsJsonArray("placeholders")?.map { it.asString } ?: emptyList(),
                    type = obj.get("type")?.takeUnless { it.isJsonNull }?.asString,
                    texture = obj.get("texture")?.takeUnless { it.isJsonNull }?.asString,
                    atlas = obj.get("atlas")?.takeUnless { it.isJsonNull }?.asString,
                )
                count++
            }

            event.isCancelled = true
            if (plugin.config.debug) {
                val serverName = (event.sender as Server).info.name
                plugin.logger.info("Registered $count glyph(s) from $serverName")
            }
        }.onFailure {
            plugin.logger.warning("Failed to parse glyph plugin message: ${it.message}")
        }
    }
}
