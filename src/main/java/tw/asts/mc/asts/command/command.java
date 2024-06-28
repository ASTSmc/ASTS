package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public final class command {
    public command(Plugin plugin,PluginManager pluginManager, FileConfiguration config) {
        pluginManager.registerEvents(new MenuClick(), plugin);
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("wtp", "世界傳送", List.of("worldtp", "wteleport", "worldteleport"), new Wtp(config));
            commands.register("testmenu", "開啟選單", List.of("testastsmenu"), new Menu());
        });
    }
}
