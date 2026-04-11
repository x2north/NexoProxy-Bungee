package com.nexomc.nexoproxy.glyphs

import com.velocitypowered.api.TextHolder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.scoreboard.ObjectiveEvent
import com.velocitypowered.api.event.scoreboard.TeamEntryEvent
import com.velocitypowered.api.event.scoreboard.TeamEvent
import com.velocitypowered.api.scoreboard.NumberFormat
import com.velocitypowered.proxy.scoreboard.VelocityScoreboardManager

class ScoreboardListener() {

//    @Subscribe
//    fun TeamEntryEvent.Add.onScoreboardEvent() {
//        val scoreboard = VelocityScoreboardManager.getInstance().getProxyScoreboard(player)
//        scoreboard.teams.forEach { team ->
//            team.prefix = team.prefix.resolveGlyphs()
//            team.suffix = team.suffix.resolveGlyphs()
//            team.displayName = team.displayName.resolveGlyphs()
//        }
//    }
//
//    @Subscribe
//    fun TeamEvent.Register.onScoreboardEvent() {
//        val scoreboard = VelocityScoreboardManager.getInstance().getProxyScoreboard(player)
//        scoreboard.teams.forEach { team ->
//            team.prefix = team.prefix.resolveGlyphs()
//            team.suffix = team.suffix.resolveGlyphs()
//            team.displayName = team.displayName.resolveGlyphs()
//        }
//    }

    @Subscribe
    fun ObjectiveEvent.Register.onScoreboardEvent() {
        if (!isMutable) return
        title = title.resolveGlyphs()
        (numberFormat as? NumberFormat.FixedFormat)?.component?.resolveGlyphs()?.let {
            setNumberFormat(NumberFormat.fixed(it))
        }
    }
    @Subscribe
    fun ObjectiveEvent.Update.onScoreboardEvent() {
        if (!isMutable) return
        title = title.resolveGlyphs()
        (numberFormat as? NumberFormat.FixedFormat)?.component?.resolveGlyphs()?.let {
            setNumberFormat(NumberFormat.fixed(it))
        }
    }

    private fun TextHolder.resolveGlyphs(): TextHolder {
        return TextHolder.of(legacyText, modernText.resolveGlyphs())
    }
}