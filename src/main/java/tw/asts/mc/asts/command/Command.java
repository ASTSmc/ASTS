package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.UserConfig;

import java.util.List;
import java.util.logging.Level;

public final class Command {
    public Command(Plugin plugin, PluginManager pluginManager, FileConfiguration config, UserConfig userConfig) {
        Log.get().log(Level.INFO, "正在載入指令");
        pluginManager.registerEvents(new MenuClick(), plugin);
        Menu menu = new Menu(plugin);
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("wtp", "選擇世界傳送", List.of("worldtp", "wteleport", "worldteleport"), new Wtp(config));
            commands.register("rtp", "隨機傳送", List.of("randomtp", "rteleport", "randomteleport"), new Rtp(config, plugin));
            commands.register("menu", "開啟[ASTS]Allen跨平台伺服器選單", List.of("astsmenu"), menu);
            commands.register("asts", "顯示插件資訊", new Asts(plugin, config, userConfig, menu));
        });
    }
}
