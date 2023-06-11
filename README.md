# Okaeri Tasker

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-tasker)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-tasker)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-tasker)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-tasker)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Fluent API for gameserver schedulers targeting Minecraft server software like Spigot/Paper. Part of the [okaeri-platform](https://github.com/OkaeriPoland/okaeri-platform).

## Installation

### Maven

Add repository to the `repositories` section:

```xml
<repository>
    <id>okaeri-repo</id>
    <url>https://storehouse.okaeri.eu/repository/maven-public/</url>
</repository>
```

Add dependency to the `dependencies` section:

```xml
<!-- for bukkit (sync & async) -->
<dependency>
  <groupId>eu.okaeri</groupId>
  <artifactId>okaeri-tasker-bukkit</artifactId>
  <version>2.1.0-beta.3</version>
</dependency>
<!-- for bungee (experimental async only) -->
<dependency>
  <groupId>eu.okaeri</groupId>
  <artifactId>okaeri-tasker-bungee</artifactId>
  <version>2.1.0-beta.3</version>
</dependency>
<!-- for velocity (experimental async only) -->
<dependency>
  <groupId>eu.okaeri</groupId>
  <artifactId>okaeri-tasker-velocity</artifactId>
  <version>2.1.0-beta.3</version>
</dependency>
```

## Example usage

See [TaskerTest](https://github.com/OkaeriPoland/okaeri-tasker/blob/master/core/src/test/java/eu/okaeri/taskertest/TaskerTest.java) and visit okaeri-platform-bukkit's
example [here](https://github.com/OkaeriPoland/okaeri-platform/blob/master/bukkit-example/src/main/java/org/example/okaeriplatformtest/TestListener.java) or below.

```java
// create instance, preferably one per plugin
Tasker tasker = BukkitTasker.newPool(pluginInstance);

// standard access, create new chain
this.tasker.newChain()
        // get data from service into chain stack asynchronously
        .supplyAsync(() -> this.playerPersistence.get(event.getPlayer()))
        // manipulate the data synchronously
        .acceptSync(playerProperties -> {
            Instant lastJoined = playerProperties.getLastJoined();
            event.getPlayer().sendMessage("Your last join time: " + lastJoined);
            playerProperties.setLastJoined(Instant.now());
            playerProperties.setLastJoinedLocation(event.getPlayer().getLocation());
        })
        // asynchronously save the data - you may notice
        // that previous method does not need to return value
        // because stack data persists between chain parts
        .acceptAsync(playerProperties -> {
            playerProperties.save();
        })
        .execute();
```

## Real-life uses

Various snippets directly from the internal okaeri codebase. May and will include 
code interacting with other libraries like [okaeri-i18n](okaeri-i18n) or references
to other internal code. All of these were built with [okaeri-platform](https://github.com/OkaeriPoland/okaeri-platform).

### Delayed teleport

Delayed teleportation for warps and similar. Checks if distance between starting and current 
location is below 1. Executes action when there was no rejected state for the delayed time.

```java
Duration teleportDuration = this.spawnConfig.getTeleportDuration();
Location startingLocation = player.getLocation().clone();

this.tasker.newDelayer(teleportDuration, Duration.ofSeconds(1))
    .abortIfNot(player::isOnline)
    .abortIfThen(
        () -> startingLocation.distanceSquared(player.getLocation()) > 0.25, // sqrt(1)=0.25
        () -> this.i18n.get(this.messages.getTeleportationCancelledError()).sendTo(player)
    )
    .delayed(() -> {
        this.module.teleportToSpawn(player, true);
        this.i18n.get(this.messages.getTeleportationSuccess()).sendTo(player);
    })
    .executeAsync();
```

### Detecting hook landing

Wait-loop for detecting hook landing in the `PlayerFishEvent` for state `FISHING`.
Executes code on condition only using #forced or timeouts with no action.
Note that using #delayed would allow execution on force or after timeout.

```java
this.tasker.newDelayer(Duration.ofSeconds(10))
    .abortIf(hook::isDead)
    .abortIfNot(player::isOnline)
    .forceIf(hook::isInWater)
    .forced(() -> {
        this.i18n.get(this.messages.getFishingStateInfo())
            .with("chance", (1d / (double) fishesCaughtAtHook) * 100d)
            .target(BukkitMessageTarget.ACTION_BAR)
            .sendTo(player);
    })
    .executeSync();
```

### Async book processing

Allows book filtering in `PlayerEditBookEvent` using [OK! AI.Censor](https://www.okaeri.eu/services/aicensor) to 
take place without doing blocking I/O in the main thread. Can also be applied in similar fashion to sign editing.

```java
this.tasker.newChain()
    .supplyAsync(() -> {
        String contents = this.bookToString(event.getNewBookMeta());
        return new AsyncPlayerTextEvent(player, contents, "Book").call();
    })
    .abortIfNot(AsyncPlayerTextEvent::isCancelled)
    .sync(() -> {
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setPages(event.getPreviousBookMeta().getPages());
        book.setItemMeta(bookMeta);
        book.setType(Material.WRITABLE_BOOK);
    })
    .execute();
```
