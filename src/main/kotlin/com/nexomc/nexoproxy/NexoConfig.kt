package com.nexomc.nexoproxy

import kotlinx.serialization.Serializable

@Serializable
data class NexoConfig(
    val debug: Boolean = false,
    val resourcePacks: Boolean = true,
    val glyphs: Boolean = true,
)