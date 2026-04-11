package com.nexomc.nexoproxy.glyphs

import com.nexomc.nexoproxy.NexoProxy
import com.velocitypowered.api.TextHolder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.scoreboard.ObjectiveEvent
import com.velocitypowered.api.event.scoreboard.ScoreboardEvent
import com.velocitypowered.api.event.scoreboard.TeamEntryEvent
import com.velocitypowered.api.event.scoreboard.TeamEvent
import com.velocitypowered.api.scoreboard.ProxyScoreboard

class ScoreboardListener(val plugin: NexoProxy) {

    @Subscribe
    fun ScoreboardEvent.ons() {
        scoreboard
    }

    @Subscribe
    fun TeamEntryEvent.Add.onScoreboardEvent() {
        val scoreboard = scoreboard as? ProxyScoreboard ?: return
        scoreboard.teams.forEach { team ->
            team.updateProperties { builder ->
                builder
                    .displayName(team.displayName.resolveGlyphs())
                    .prefix(team.prefix.resolveGlyphs())
                    .suffix(team.suffix.resolveGlyphs())
            }
        }
    }

    @Subscribe
    fun TeamEvent.Register.onScoreboardEvent() {
        val scoreboard = scoreboard as? ProxyScoreboard ?: return
        scoreboard.teams.forEach { team ->
            team.updateProperties { builder ->
                builder
                    .displayName(team.displayName.resolveGlyphs())
                    .prefix(team.prefix.resolveGlyphs())
                    .suffix(team.suffix.resolveGlyphs())
            }
        }
    }

    @Subscribe
    fun ObjectiveEvent.onScoreboardEvent() {
        if (this is ObjectiveEvent.Unregister) return
        val scoreboard = scoreboard as? ProxyScoreboard ?: return
        scoreboard.objectives.forEach { objective ->
            objective.title = objective.title.resolveGlyphs()
            objective.allScores.forEach { score ->
                score.displayName = score.displayName?.resolveGlyphs()
            }
        }
    }

    private fun TextHolder.resolveGlyphs(): TextHolder {
        return TextHolder.of(legacyText, modernText.resolveGlyphs())
    }
}