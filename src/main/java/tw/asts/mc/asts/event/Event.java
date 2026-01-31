package tw.asts.mc.asts.event;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.UserConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final public class Event implements Listener {
    private final MobAttack mobAttack;
    private final AutoInvPick autoInvPick;
    private final FileConfiguration config;
    public Event(Plugin plugin, PluginManager pluginManager, UserConfig userConfig, FileConfiguration config) {
        Log.info("正在載入事件");
        mobAttack = new MobAttack(userConfig);
        autoInvPick = new AutoInvPick(userConfig);
        this.config=config;
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
            final Method getHandle = bukkitRaid.getClass().getMethod("getHandle");
            final Object nmsRaid = getHandle.invoke(bukkitRaid);

            final Field numGroupsField = nmsRaid.getClass().getDeclaredField("numGroups");
            numGroupsField.setAccessible(true);
            numGroupsField.setInt(nmsRaid, 7);
        } catch (Exception ignored){
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        final EntityDamageEvent damageEvent= victim.getLastDamageCause();
        if (damageEvent instanceof EntityDamageByEntityEvent entityDamage) {
            final Entity damager = entityDamage.getDamager();
            if (damager instanceof LivingEntity killer) {
                if (killer.getEquipment() == null) return;
                final ItemStack weapon = killer.getEquipment().getItemInMainHand();
                final int looting = weapon.getEnchantmentLevel(Enchantment.LOOTING);
                double rate = config.getDouble("player_head.rate");
                if (looting == 1)
                    rate = config.getDouble("player_head.looting.1");
                else if (looting == 2)
                    rate = config.getDouble("player_head.looting.2");
                else if (looting >= 3)
                    rate = config.getDouble("player_head.looting.3");
                final double rng = Math.random();
                if (rng < rate / 100){
                    final ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    final SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                    if (meta != null) {
                        meta.setOwningPlayer(victim);
                        playerHead.setItemMeta(meta);
                        event.getDrops().add(playerHead);
                    }
                }
            }
        }

    }
}