package tw.asts.mc.asts.event;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Crafter;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

final public class Totem {
    private final Plugin plugin;
    private final NamespacedKey noCopyKey;

    public Totem(Plugin plugin) {
        this.plugin = plugin;
        this.noCopyKey = new NamespacedKey(this.plugin, "no_copy");
    }

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

    private boolean hasNoCopy(ItemStack[] items) {
        if (items == null) return false;
        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(noCopyKey, PersistentDataType.BYTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack createTotemMapResult(ItemStack[] items) {
        if (items == null) return null;
        ItemStack input = null;
        for (ItemStack i : items) {
            if (i != null && i.getType() == Material.FILLED_MAP) {
                input = i;
                break;
            }
        }
        if (input == null) return null;

        final ItemStack result = new ItemStack(Material.FILLED_MAP);

        if (input.hasData(DataComponentTypes.MAP_ID)) {
            result.setData(DataComponentTypes.MAP_ID, input.getData(DataComponentTypes.MAP_ID));
        }
        if (input.hasData(DataComponentTypes.MAP_DECORATIONS)) {
            result.setData(DataComponentTypes.MAP_DECORATIONS, input.getData(DataComponentTypes.MAP_DECORATIONS));
        }
        result.setData(DataComponentTypes.ITEM_NAME, Component.translatable("item.minecraft.totem_of_undying"));
        result.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        final ItemMeta meta = result.getItemMeta();
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
        return result;
    }

    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (hasNoCopy(matrix)) {
            event.getInventory().setResult(null);
            return;
        }
        if (!(event.getRecipe() instanceof Keyed keyed) || !keyed.getKey().equals(new NamespacedKey(this.plugin, "totem_map"))) return;
        ItemStack result = createTotemMapResult(matrix);
        if (result != null) {
            event.getInventory().setResult(result);
        }
    }

    public void onCraft(CraftItemEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        if (hasNoCopy(matrix)) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getRecipe() instanceof Keyed keyed) || !keyed.getKey().equals(new NamespacedKey(this.plugin, "totem_map"))) return;

        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item != null && item.getType() == Material.FILLED_MAP) {
                item.setAmount(item.getAmount() + 1);
                matrix[i] = item;
            }
        }
        inv.setMatrix(matrix);
    }

    public void onCrafterCraft(CrafterCraftEvent event) {
        if (!(event.getBlock().getState() instanceof Crafter crafter)) return;
        ItemStack[] contents = crafter.getInventory().getContents();
        if (hasNoCopy(contents)) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getRecipe() instanceof Keyed keyed) || !keyed.getKey().equals(new NamespacedKey(this.plugin, "totem_map"))) return;
        ItemStack result = createTotemMapResult(contents);
        if (result != null) {
            event.setResult(result);
        }
    }
}
