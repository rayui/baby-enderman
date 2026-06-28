# 🟣 Baby Enderman — How to Play

You made a Minecraft mod! It adds an adorable, tamable **baby Enderman** companion.

## ▶️ Starting the game
1. Open the **Minecraft Launcher**.
2. Pick the **NeoForge** profile (the one that says `neoforge-21.11.42`).
3. Click **Play**.

The mod is already installed at `~/.minecraft/mods/babyenderman-1.0.0.jar`.

## 🥚 Getting your baby Enderman
- In **Creative mode**, open your inventory and find the **Baby Enderman Spawn Egg**
  (it's black with purple spots, in the "Spawn Eggs" tab). Right-click the ground to place one.
- In **Survival**, use the spawn egg if you have one (`/give @s babyenderman:baby_enderman_spawn_egg`).

## 🎮 What it does
- 👣 It **walks around** like an Enderman (but it can't teleport and **can't hurt you**).
- 🦘 When it's not tamed, it does **cute little jumps**.
- 🧱 It **picks up and carries** grass, dirt, stone, and sand blocks it finds.
- 👀 It has **glowing green eyes**.
- 🤫 It's **quiet and shy** (very soft sounds).

## 💜 Taming it
- Hold **Emeralds** and **right-click** the baby Enderman **3 times** (one emerald each time).
  (Green gems, for its green eyes! 💚)
- Each time it eats one you'll **hear a munch**, see **purple sparkles**, and get a little
  message like *"Baby Enderman likes the emerald… (2/3)"* so you know it worked.
- On the third one it becomes **tamed** — a burst of **purple hearts**! 🎉
- Tip: the baby is small, so aim right *at* it.
- (We use Emeralds, not Ender Pearls — throwing an ender pearl would teleport *you*!)

## 🧍 Once it's tamed (it's yours!)
- **Right-click it with an empty hand** → it hops up and **rides on your shoulder**.
- **Right-click it again** (empty hand) → it hops **off** your shoulder.
- **Sneak + right-click** → tell it to **sit / stand** (and it gets off your shoulder).
- While it's on your shoulder, it will **occasionally give you a present** — a random
  grass, dirt, stone, or sand block — with a little puff of purple sparkles. 🎁

## 🛠️ For grown-ups: rebuilding the mod
If you change the code, rebuild and reinstall with:

```bash
cd /home/ray/Code/custom-minecraft-mob
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build
cp build/libs/babyenderman-1.0.0.jar ~/.minecraft/mods/
```

To test it loads without launching the full game (headless server check):
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew runServer
```

Have fun! 🟣👾
