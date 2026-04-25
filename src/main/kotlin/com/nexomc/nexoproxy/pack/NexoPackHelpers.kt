package com.nexomc.nexoproxy.pack

import java.util.UUID

typealias PlayerUUID = UUID

object NexoPackHelpers {
    private val byObfHash: MutableMap<String, ResourcePackInfo> = mutableMapOf()
    private val byObfUuid: MutableMap<UUID, ResourcePackInfo> = mutableMapOf()

    internal val packHashTracker: MutableMap<PlayerUUID, String> = mutableMapOf()
    const val PACK_HASH_CHANNEL: String = "nexo:pack_hash"

    fun addMapping(pack: ResourcePackInfo) {
        byObfHash[pack.obfuscatedHash] = pack
        byObfUuid[pack.obfuscatedUuid] = pack
    }

    val allMappings: Collection<ResourcePackInfo> get() = byObfHash.values

    fun findMappingByHash(hash: String): ResourcePackInfo? =
        byObfHash[hash] ?: byObfHash.values.find { it.unobfuscatedHash == hash }

    fun findMappingByUUID(uuid: UUID): ResourcePackInfo? = byObfUuid[uuid]
}
