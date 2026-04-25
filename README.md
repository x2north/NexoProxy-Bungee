## A BungeeCord Plugin Addon for [Nexo](https://nexomc.com/)

This plugin is a BungeeCord rewrite of the original Velocity-focused NexoProxy addon.
It keeps cross-server synchronization features that are available through standard Bungee plugin messaging:

- Nexo pack hash mapping synchronization (`nexo:pack_hash`)
- Glyph metadata synchronization (`nexo:glyph_info`)
- Proxy handshake forwarding (`nexo:proxy_handshake`)
- Config reload/debug command (`/nexoproxy`)

### Notes on parity

BungeeCord does not expose Velocity's resource-pack and scoreboard event APIs, so direct interception
of pack send/remove and Velocity packet pipeline transformations are intentionally not included in this rewrite.
