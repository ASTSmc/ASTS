package tw.asts.mc.asts.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tw.asts.mc.asts.util.UserConfig;

public class Event implements Listener {
    private final MobAttack mobAttack;
    private final AutoInvPick autoInvPick;
    public Event(Plugin plugin, PluginManager pluginManager, UserConfig userConfig) {
        mobAttack = new MobAttack(userConfig);
        autoInvPick = new AutoInvPick(userConfig);
        pluginManager.registerEvents(this, plugin);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        mobAttack.onMobAttack(event);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        autoInvPick.onAutoInvPick(event);
    }
}