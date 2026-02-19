package com.nexomc.nexoproxy

import com.google.gson.JsonObject
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import java.util.UUID

typealias PlayerUUID = UUID
typealias PackUUID = UUID

object NexoPackHelpers {
    val nexoObfuscationMappings = mutableListOf<ObfuscatedResourcePack>()
    val packHashTracker: MutableMap<PlayerUUID, ObfuscatedResourcePack> = mutableMapOf()
    val HASH_CHANNEL = MinecraftChannelIdentifier.from("nexo:pack_hash")
}

data class ObfuscatedResourcePack(val uuid: PackUUID, val url: String, val hash: String, val unobfuscated: PackUUID?) {
    constructor(jsonObject: JsonObject) : this(
        uuid = UUID.fromString(jsonObject.get("uuid").asString),
        url = jsonObject.get("url").asString,
        hash = jsonObject.get("hash").asString,
        unobfuscated = UUID.fromString(jsonObject.get("unobfuscated").asString),
    )
}