package tw.asts.mc.asts;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import tw.asts.mc.asts.command.Command;
import tw.asts.mc.asts.event.Event;
import tw.asts.mc.asts.util.BasicConfig;
import tw.asts.mc.asts.util.Placeholder;
import tw.asts.mc.asts.util.UserConfig;
import tw.asts.mc.asts.util.text;

import java.util.List;
import java.util.logging.Level;

public final class ASTS extends JavaPlugin implements Listener {

    public Command command;
    public Event event;
    public FileConfiguration config;
    public UserConfig userConfig;

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "正在啟動插件");
        config = getConfig();
        config.addDefault("wtp.disable.worlds", List.of("world_nether", "world_the_end"));
        config.addDefault("rtp.disable.worlds", List.of("ASTS"));
        config.addDefault("rtp.disable.far", false);
        config.addDefault("rtp.radius.default", 10000);
        config.addDefault("rtp.radius.far", 100000);
        config.options().copyDefaults(true);
        saveConfig();
        userConfig = new UserConfig(this);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        command = new Command(this, pluginManager, config, userConfig);
        event = new Event(this, pluginManager, userConfig);
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            getLogger().log(Level.INFO, "正在註冊PlaceholderAPI");
            new Placeholder(this, userConfig).register();
        }
        getLogger().log(Level.INFO, "插件已啟動");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "關閉插件");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.name + " - 歡迎來到" + BasicConfig.name + "！")));
    }

}

