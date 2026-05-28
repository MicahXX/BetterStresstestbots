# BetterStresstestbots

Stress test your Minecraft server with server-side fake bots. Unlike external bot tools, this runs entirely as a Paper plugin, so doesnt require `online-mode=false`. Bots can fly or walk around the world, load chunks, and stress the game loop just like real players.

Supports **1.21.x** and **26.x**.

## Building

Clone the repo:

```
git clone https://github.com/yourusername/BetterStresstestbots.git
```

Build all version jars at once:

```
./gradlew :v1_21:assemble
./gradlew :v1_21_11:assemble
./gradlew :v26:assemble
```

Jars will be in each module's `build/libs/`. Use the `*-reobf.jar` for 1.21.x versions and the regular jar for 26.x.

## Installation

Drop the correct jar for your server version into your `plugins/` folder and restart.

| Jar | Version |
|-----|---------|
| `v1_21-x.x.x-reobf.jar` | 1.21 – 1.21.4 |
| `v1_21_11-x.x.x-reobf.jar` | 1.21.5 – 1.21.11 |
| `v26-x.x.x.jar` | 26.x |

Add this to your `server.properties`:

```
allow-flight=true
```

## Commands

Configure the bots first, then start them with `/start`:

```
/stress count 100       Set how many bots to spawn (does not spawn yet)
/stress speed 0.15      Set movement speed in blocks/tick (default: 0.1)
/stress radius 500      Set the wander radius in blocks (default: 500)
/stress mode fly        Set movement mode: fly or walk
/start                  Spawn bots with the current settings
/start 50               Spawn 50 bots immediately
/stress stop            Remove all bots
/stress chat <msg>      Make all bots send a chat message
/stress tp              Teleport all bots to your location
/stress status          Show current bot count and settings
```

## Notes

- Bots are real server-side `ServerPlayer` objects so they load chunks, trigger entity tracking, and count toward the player list just like real players
- Start small (10–20 bots) and check TPS etc.
- Hard cap of 1000 bots to prevent OOM crashes
- In **walk mode** bots snap to the terrain surface
