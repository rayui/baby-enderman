# Baby Enderman 🟣👾

A NeoForge mod for **Minecraft 1.21.11** that adds an adorable, tamable **baby Enderman** companion.

> Designed by a kid, built together. 💚

## Features
- A small (cute!) baby Enderman that spawns from its own black-and-purple **spawn egg**.
- Walks around like an Enderman — but **can't teleport** and **can't hurt you**.
- Does occasional **cute little jumps** when untamed.
- **Picks up and carries** grass, dirt, stone, and sand blocks.
- **Glowing green eyes** and quiet, shy sounds.
- **Tame it with 3 emeralds** (green gems for its green eyes) — with a munch sound, purple sparkles, and on-screen progress.
- Once tamed, **right-click to perch it on your shoulder**, where it occasionally **gifts you blocks**.
- Sneak-right-click to tell it to sit/stand.

## Build
Requires JDK 21.

```bash
./gradlew build
# jar is written to build/libs/babyenderman-<version>.jar
```

## Install (single player or a NeoForge server)
Drop `babyenderman-<version>.jar` into your `mods/` folder. It's a **both-side** mod, so it must be installed on the client *and* the server, with matching versions.

## Releases
CI builds every push. Pushing a tag `v<version>` (matching `mod_version` in `gradle.properties`) publishes the jar to **Modrinth** and a **GitHub Release** automatically.

## License
[MIT](LICENSE)
