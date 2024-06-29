package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public final class Command {
    public Command(Plugin plugin, PluginManager pluginManager, FileConfiguration config) {
        pluginManager.registerEvents(new MenuClick(), plugin);
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("wtp", "選擇世界傳送", List.of("worldtp", "wteleport", "worldteleport"), new Wtp(config));
            commands.register("menu", "開啟[ASTS]Allen跨平台伺服器選單", List.of("astsmenu"), new Menu(plugin));
        });
    }
}
