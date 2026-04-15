# StickyFix Mod

**Minecraft 1.21.1 | NeoForge**

Prevents **piston spitting** — blocks always stay attached to sticky pistons, regardless of redstone pulse speed (including 0-tick pulses).

---

## What is piston spitting?

Piston spitting occurs when a sticky piston retracts too fast (e.g. via a short redstone pulse). Instead of pulling the block back, the piston "spits" it forward and leaves it floating. This mod fixes that.

---

## Installation

1. Install **NeoForge 21.1.86** (or compatible) for Minecraft 1.21.1
2. Place `stickyfix-1.0.0.jar` in your `mods/` folder
3. Launch Minecraft

---

## Building from source

### Requirements
- Java 21 (JDK)
- Internet connection (to download NeoForge gradle toolchain on first run)

### Steps

```bash
# Clone / navigate to the mod folder
cd sticky-fix-mod

# Build the mod jar
./gradlew build        # Linux/Mac
gradlew.bat build      # Windows
```

The compiled `.jar` will be at:
```
build/libs/stickyfix-1.0.0.jar
```

Copy it to your `mods/` folder.

---

## How it works

The mod uses a **Mixin** to intercept `PistonBaseBlock#triggerEvent`.

When a sticky piston retracts (`eventId=1`, `eventParam=1`), vanilla Minecraft runs a `PistonStructureResolver` to check if the retraction move is valid. If it fails (due to fast timing), vanilla "spits" the block — detaching it instead of pulling it.

This mod catches that failed case and **cancels the spit**, keeping the block safely attached.

---

## License

MIT
