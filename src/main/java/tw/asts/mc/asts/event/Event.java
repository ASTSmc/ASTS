package tw.asts.mc.asts.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tw.asts.mc.asts.util.UserConfig;

public class Event implements Listener {
    private UserConfig userConfig;
    private MobAttack mobAttack;
    public Event(Plugin plugin, PluginManager pluginManager, UserConfig userConfig) {
        this.userConfig = userConfig;
        mobAttack = new MobAttack(userConfig);
        pluginManager.registerEvents(this, plugin);
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        mobAttack.onMobAttack(event);
    }
}