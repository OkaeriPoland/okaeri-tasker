package eu.okaeri.tasker.bungee;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTasker extends Tasker {

    protected BungeeTasker(@NonNull TaskerPlatform platform) {
        super(platform);
    }

    public static BungeeTasker newPool(@NonNull Plugin plugin) {
        return new BungeeTasker(new BungeePlatform(new BungeeContext(plugin)));
    }
}
