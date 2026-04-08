# AntiFrecam

A server-side Fabric mod for Minecraft 1.21.1 that prevents freecam exploits by
only sending players the chunk sections near their actual Y level. Players using
freecam to look underground will only see the mask block (default: stone) instead
of real world data.

## How it works

Minecraft sends chunks as arrays of 16-tall "sections". This mod intercepts the
outbound chunk packet for each player and replaces any sections that are more than
`sectionRadius` sections away from the player's current Y level with a solid layer
of `maskBlock`. Surface players (above `surfaceThreshold`) always get full data.

## Building

Requirements: Java 21, internet connection (Gradle downloads Fabric toolchain).

```bash
./gradlew build
```

The jar will be at `build/libs/antifreecam-1.0.0.jar`. Drop it in your server's
`mods/` folder alongside Fabric API.

## Config

On first run, `config/antifreecam.properties` is created:

```properties
enabled=true

# Sections revealed above AND below the player's section.
# Each section is 16 blocks tall.
#   1 = ±16 blocks   (very tight — may cause pop-in)
#   2 = ±32 blocks   (recommended default)
#   3 = ±48 blocks   (more lenient)
sectionRadius=2

# Y level at or above which players receive full unmasked data.
# 62 = sea level. Players on the surface don't need masking.
surfaceThreshold=62

# Block used to fill hidden sections. Must be a valid vanilla block ID.
maskBlock=stone
```

Reload without restart: `/antifreecam reload`
Check current settings: `/antifreecam status`
(Requires op level 2)

## Limitations (v1.0 — basics)

- Does not re-send sections as the player moves vertically (sections already
  sent stay as-is until the chunk is re-sent or the player re-enters the chunk).
  A future version will track player Y movement and push section updates.
- MaskedChunk wrapper copies heightmaps and block entities but does not copy
  fluid/tick data — surface rendering should look normal but edge cases may exist.
- `sendChunkDataPackets` method name may need updating for minor Minecraft versions.
