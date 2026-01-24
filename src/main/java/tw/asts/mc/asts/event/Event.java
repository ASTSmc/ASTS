package tw.asts.mc.asts.event;

import net.minecraft.world.entity.raid.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.UserConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final public class Event implements Listener {
    private final MobAttack mobAttack;
    private final AutoInvPick autoInvPick;
    public Event(Plugin plugin, PluginManager pluginManager, UserConfig userConfig) {
        Log.info("正在載入事件");
        mobAttack = new MobAttack(userConfig);
        autoInvPick = new AutoInvPick(userConfig);
        pluginManager.registerEvents(this, plugin);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityTarget(EntityTargetEvent event) {
        mobAttack.onMobAttack(event);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        autoInvPick.onBlockBreak(event);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        autoInvPick.onBlockDropItem(event);
    }
    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent event) {
        try {
            org.bukkit.Raid bukkitRaid = event.getRaid();
            Method getHandle = bukkitRaid.getClass().getMethod("getHandle");
            Object nmsRaid = getHandle.invoke(bukkitRaid);

            Field numGroupsField = nmsRaid.getClass().getDeclaredField("numGroups");
            numGroupsField.setAccessible(true);
            numGroupsField.setInt(nmsRaid, 7);
        }catch (Exception e){
        }
    }
}