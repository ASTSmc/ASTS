package tw.asts.mc.asts;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

final class basicConfig {
    public static final String name = "§6[§4ASTS§6]§eAllen§a跨平台§b插件§f伺服器§r";
    public static String prefix(String type) {
        return name + " §e-§r §6" + type + "§r\n";
    }
}

public final class ASTS extends JavaPlugin implements Listener {

    public FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "正在啟動插件");
        config = getConfig();
        config.addDefault("wtp.disable.worlds", List.of("world_nether", "world_the_end"));
        config.options().copyDefaults(true);
        saveConfig();
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new MenuClick(), this);
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("wtp", "隨機傳送", List.of("worldtp", "wteleport", "worldteleport"), new Wtp(config));
            commands.register("testmenu", "開啟選單", List.of("testastsmenu"), new Menu());
        });
        getLogger().log(Level.INFO, "插件已啟動");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "關閉插件");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(text.miniMessageComponent(text.miniMessage(basicConfig.name + " - 歡迎來到" + basicConfig.name + "！")));
    }

}