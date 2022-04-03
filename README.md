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
<dependency>
  <groupId>eu.okaeri</groupId>
  <artifactId>okaeri-tasker-bukkit</artifactId>
  <version>1.1.3</version>
</dependency>
```

### Gradle

Add repository to the `repositories` section:

```groovy
maven { url "https://storehouse.okaeri.eu/repository/maven-public/" }
```

Add dependency to the `maven` section:

```groovy
implementation 'eu.okaeri:okaeri-tasker-bukkit:1.1.3'
```

## Example Usage

See [TaskerTest](https://github.com/OkaeriPoland/okaeri-tasker/blob/master/core/src/test/java/eu/okaeri/taskertest/TaskerTest.java) and visit okaeri-platform-bukkit's
example [here](https://github.com/OkaeriPoland/okaeri-platform/blob/master/bukkit-example/src/main/java/org/example/okaeriplatformtest/TestListener.java) or below.

```java
// create instance, preferably one per plugin
Tasker tasker = BukkitTasker.newPool(pluginInstance);

// standard access, create new chain
this.tasker.newChain()
        // get data from service into chain stack asynchronously
        .async(() -> this.playerPersistence.get(event.getPlayer()))
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
