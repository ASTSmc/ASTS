package tw.asts.mc.asts;

import java.time.ZoneId;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import tw.asts.mc.asts.command.Command;
import tw.asts.mc.asts.command.Pfly;
import tw.asts.mc.asts.event.Event;
import tw.asts.mc.asts.loginstreak.LoginStreak;
import tw.asts.mc.asts.recipe.Recipe;
import tw.asts.mc.asts.util.*;

public final class ASTS extends JavaPlugin implements Listener {

    public Command command;
    public Event event;
    public FileConfiguration config;
    public UserConfig userConfig;
    public LoginStreak loginStreak;
    public Pfly pfly;

    @Override
    public void onEnable() {
        Log.init(getLogger());
        Log.info("正在啟動插件");
        new Bundle();
        config = getConfig();
        config.addDefault("wtp.disable.worlds", List.of("world_nether", "world_the_end"));
        config.addDefault("rtp.disable.worlds", List.of("ASTS"));
        config.addDefault("rtp.disable.far", false);
        config.addDefault("rtp.radius.default", 10000);
        config.addDefault("rtp.radius.far", 100000);
        config.addDefault("player_head.rate", 20);
        config.addDefault("player_head.looting.1", 30);
        config.addDefault("player_head.looting.2", 40);
        config.addDefault("player_head.looting.3", 50);
        config.addDefault("login_streak.minutes", 10);
        config.addDefault("login_streak.grace_days", 1);
        config.addDefault("login_streak.timezone", ZoneId.systemDefault().getId());
        config.addDefault("pfly.cost", 200.0);
        config.addDefault("pfly.discount", 0.9);
        config.options().copyDefaults(true);
        saveConfig();
        userConfig = new UserConfig(this);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        pfly = new Pfly(this, config, userConfig);
        command = new Command(this, pluginManager, config, userConfig, pfly);
        event = new Event(this, pluginManager, userConfig,config);
        new Recipe(this);
        loginStreak = new LoginStreak(this, config);
        pluginManager.registerEvents(loginStreak, this);
        ExternalClass.plugin(this, "PlaceholderAPI", "tw.asts.mc.asts.util.Placeholder", List.of(Plugin.class, UserConfig.class), List.of(this, userConfig)).runMethod("register");
        ExternalClass.plugin(this, "Residence", "tw.asts.mc.asts.util.LightBlock", List.of(Plugin.class), List.of(this));
        Log.info("插件已啟動");
    }

    @Override
    public void onDisable() {
        if (pfly != null) {
            pfly.disable();
        }
        if (loginStreak != null) {
            loginStreak.flushAll();
        }
        Log.info("關閉插件");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(text.o(text.t("asts.server"), text.l(" - 歡迎來到"), text.t("asts.server"), text.l("！")));
    }

}
