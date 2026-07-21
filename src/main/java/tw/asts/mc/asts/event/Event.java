package tw.asts.mc.asts.event;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.text.Component;
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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataType;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.UserConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List; 

final public class Event implements Listener {
    private final MobAttack mobAttack;
    private final AutoInvPick autoInvPick;
    private final FileConfiguration config;
    private final Plugin plugin;
    private final NamespacedKey noCopyKey;
    public Event(Plugin plugin, PluginManager pluginManager, UserConfig userConfig, FileConfiguration config) {
        Log.info("正在載入事件");
        mobAttack = new MobAttack(userConfig);
        autoInvPick = new AutoInvPick(userConfig);
        this.config=config;
        pluginManager.registerEvents(this, plugin);
        this.plugin=plugin;
        this.noCopyKey=new NamespacedKey(this.plugin,"no_copy");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof CartographyInventory inv)) return;
        ItemStack input = inv.getItem(0);
        if (input == null) return;

        ItemMeta meta = input.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(noCopyKey, PersistentDataType.BYTE)) return;

        if (event.getRawSlot() == 2) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
     if (!(event.getRecipe() instanceof Keyed keyed) || !keyed.getKey().equals(new NamespacedKey(this.plugin, "totem_map"))) return;
        ItemStack input = null;
        for (ItemStack i : event.getInventory().getMatrix()) {
            if (i != null && i.getType() == Material.FILLED_MAP) input = i;
        }
        if (input == null) return;
        final ItemStack result = new ItemStack(Material.FILLED_MAP);

        if (input.hasData(DataComponentTypes.MAP_ID)) {
            result.setData(DataComponentTypes.MAP_ID, input.getData(DataComponentTypes.MAP_ID));
        }
        if (input.hasData(DataComponentTypes.MAP_DECORATIONS)) {
            result.setData(DataComponentTypes.MAP_DECORATIONS, input.getData(DataComponentTypes.MAP_DECORATIONS));
        }
        result.setData(DataComponentTypes.ITEM_NAME, Component.translatable("item.minecraft.totem_of_undying"));
        result.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta=result.getItemMeta();
        meta.getPersistentDataContainer().set(noCopyKey, PersistentDataType.BYTE, (byte) 1);
        result.setItemMeta(meta);
        result.setData(
            DataComponentTypes.DEATH_PROTECTION,
            DeathProtection.deathProtection(
                List.of(
                    ConsumeEffect.applyStatusEffects(
                        List.of(new PotionEffect(PotionEffectType.REGENERATION, 900, 1),
                                new PotionEffect(PotionEffectType.ABSORPTION, 100, 1),
                                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0)),
                        1f)
                )
            )
      );
      event.getInventory().setResult(result);
    }
    @EventHandler
    public void onCraft(CraftItemEvent event) {
      if (!(event.getRecipe() instanceof Keyed keyed) || !keyed.getKey().equals(new NamespacedKey(this.plugin, "totem_map"))) return;

        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();

        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item != null && item.getType() == Material.FILLED_MAP) {
                item.setAmount(item.getAmount() + 1);
                matrix[i] = item;
            }
        }
        inv.setMatrix(matrix);
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
