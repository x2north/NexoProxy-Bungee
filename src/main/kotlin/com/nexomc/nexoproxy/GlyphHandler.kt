package com.nexomc.nexoproxy

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

object GlyphStore {
    @Volatile var enabled: Boolean = true
    val glyphComponents: MutableMap<String, Component> = mutableMapOf()
    const val GLYPH_CHANNEL = "nexo:glyph_info"
}

private val GLYPH_TAG = Regex("<glyph:([^>]+)>")

/**
 * Recursively resolves <glyph:id> tags in a Component tree, replacing them
 * with the real Components from GlyphStore.
 *
 * Two cases are handled:
 *
 * 1. The tag appears as literal text inside a single TextComponent (the common case
 *    when the surrounding formatting is a simple colour like <red>).
 *
 * 2. The tag is split across multiple single-character TextComponent siblings
 *    (happens with <gradient:…> and <rainbow> because MiniMessage applies a
 *    per-character colour, producing one TextComponent per character). In this
 *    case the sibling list is accumulated into a buffer, the buffer text is
 *    scanned for complete tags, and matching character-spans are replaced.
 */
fun Component.resolveGlyphs(): Component {
    val resolvedChildren = resolveChildrenGlyphs(children())

    return when (this) {
        is TextComponent -> {
            val content = content()
            val matches = GLYPH_TAG.findAll(content).toList()

            if (matches.isEmpty()) {
                if (resolvedChildren == children()) return this
                return children(resolvedChildren)
            }

            // Tag(s) present in a single TextComponent — split around them
            val parts = mutableListOf<Component>()
            var lastEnd = 0
            for (match in matches) {
                if (match.range.first > lastEnd)
                    parts += Component.text(content.substring(lastEnd, match.range.first))
                parts += GlyphStore.glyphComponents[match.groupValues[1]] ?: Component.text(match.value)
                lastEnd = match.range.last + 1
            }
            if (lastEnd < content.length)
                parts += Component.text(content.substring(lastEnd))
            parts += resolvedChildren

            Component.text("").style(style()).children(parts)
        }
        else -> when (resolvedChildren) {
            children() -> this
            else -> children(resolvedChildren)
        }
    }
}

/**
 * Processes a list of sibling components, handling the gradient/rainbow case where
 * a glyph tag is fragmented across consecutive single-character TextComponents.
 *
 * Non-single-char components and components with children are never buffered;
 * they flush the current buffer and are recursed into normally.
 */
private fun resolveChildrenGlyphs(children: List<Component>): List<Component> {
    if (children.isEmpty()) return children

    // Fast path: no lone single-char nodes — normal recursive resolve is enough
    if (children.none { it is TextComponent && it.children().isEmpty() && it.content().length == 1 }) {
        val resolved = children.map { it.resolveGlyphs() }
        return if (resolved == children) children else resolved
    }

    val result = mutableListOf<Component>()
    val charBuffer = StringBuilder()
    val charComps = mutableListOf<TextComponent>()

    fun flushBuffer() {
        if (charComps.isEmpty()) return
        val text = charBuffer.toString()
        val matches = GLYPH_TAG.findAll(text).toList()

        if (matches.isEmpty()) {
            result.addAll(charComps)
        } else {
            var pos = 0
            for (match in matches) {
                // Keep chars before this tag
                repeat(match.range.first - pos) { result.add(charComps[pos++]) }

                val glyphComp = GlyphStore.glyphComponents[match.groupValues[1]]
                if (glyphComp != null) {
                    result.add(glyphComp)
                    pos += match.value.length
                } else {
                    // Unknown glyph id — preserve the literal chars unchanged
                    repeat(match.value.length) { result.add(charComps[pos++]) }
                }
            }
            // Remaining chars after the last tag
            while (pos < charComps.size) result.add(charComps[pos++])
        }

        charBuffer.clear()
        charComps.clear()
    }

    for (child in children) {
        if (child is TextComponent && child.children().isEmpty() && child.content().length == 1) {
            charBuffer.append(child.content())
            charComps.add(child)
        } else {
            flushBuffer()
            result.add(child.resolveGlyphs())
        }
    }
    flushBuffer()

    return if (result == children) children else result
}

object GlyphPacketListener : PacketListenerAbstract() {

    override fun onPacketSend(event: PacketSendEvent) {
        if (!GlyphStore.enabled || GlyphStore.glyphComponents.isEmpty()) return

        when (event.packetType) {

            PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER -> {
                val packet = WrapperPlayServerPlayerListHeaderAndFooter(event)
                val header = packet.header.resolveGlyphs()
                val footer = packet.footer.resolveGlyphs()
                if (header !== packet.header || footer !== packet.footer) {
                    packet.header = header
                    packet.footer = footer
                }
            }

            // Individual player name entries in the tab list (used by TAB/Velocitab
            // to set custom display names per player row)
            PacketType.Play.Server.PLAYER_INFO_UPDATE -> {
                val packet = WrapperPlayServerPlayerInfoUpdate(event)
                var dirty = false
                val updatedEntries = packet.entries.map { entry ->
                    val original = entry.displayName ?: return@map entry
                    val resolved = original.resolveGlyphs()
                    if (resolved === original) return@map entry
                    dirty = true
                    entry.displayName = resolved
                    entry
                }
                if (dirty) packet.entries = updatedEntries
            }

            PacketType.Play.Server.SCOREBOARD_OBJECTIVE -> {
                val packet = WrapperPlayServerScoreboardObjective(event)
                if (packet.mode == WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE) return
                packet.displayName = packet.displayName.resolveGlyphs()
            }

            PacketType.Play.Server.TEAMS -> {
                val packet = WrapperPlayServerTeams(event)
                packet.teamInfo.ifPresent { info ->
                    var dirty = false

                    val displayName = info.displayName.resolveGlyphs()
                    val prefix = info.prefix.resolveGlyphs()
                    val suffix = info.suffix.resolveGlyphs()

                    if (displayName !== info.displayName) { info.displayName = displayName; dirty = true }
                    if (prefix !== info.prefix) { info.prefix = prefix; dirty = true }
                    if (suffix !== info.suffix) { info.suffix = suffix; dirty = true }

                    if (dirty) packet.setTeamInfo(info)
                }
            }

            else -> return
        }
    }
}
