package tw.asts.mc.asts.event;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.UserConfig;

import java.util.*;
import java.util.stream.Stream;

final public class AutoInvPick {
    private final UserConfig userConfig;
    private final List<Material> dropBlock;
    public AutoInvPick(UserConfig userConfig) {
        this.userConfig = userConfig;
        final List<String> colors = List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink");
        final List<String> colorsBlock = List.of("stained_glass", "stained_glass_pane");
        final List<Material> normalBlock = List.of(Material.BOOKSHELF, Material.TINTED_GLASS, Material.SEA_LANTERN, Material.ENDER_CHEST, Material.BUDDING_AMETHYST, Material.PACKED_ICE, Material.BLUE_ICE, Material.ICE, Material.GLASS, Material.GLASS_PANE);
        dropBlock = Stream.concat(colorsBlock.stream().map(block -> colors.stream().map(color -> Material.getMaterial((color + "_" + block).toUpperCase())).toList()).flatMap(List::stream), normalBlock.stream()).filter(Objects::nonNull).toList();
    }
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        final int experience = event.getExpToDrop();
        event.setExpToDrop(0);
        if (dropBlock.contains(event.getBlock().getType())) {
            final ItemStack blockItem = new ItemStack(event.getBlock().getType(), 1);
            final HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(blockItem);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
            if (experience > 0) {
                event.getPlayer().giveExp(experience, true);
            }
            event.setDropItems(false);
            return;
        } else if (Tag.CROPS.isTagged(event.getBlock().getType())) {
            final Ageable ageable = (Ageable) event.getBlock().getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) {
                event.setCancelled(true);
                event.getBlock().getDrops(event.getPlayer().getActiveItem(), event.getPlayer()).forEach(itemStack -> {
                    final HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemStack);
                    remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
                });
                final Material ageableMaterial = ageable.getMaterial();
                final Material placementMaterial = ageable.getPlacementMaterial();
                event.getBlock().setType(Material.AIR, true);
                if (event.getPlayer().getInventory().contains(placementMaterial)) {
                    event.getPlayer().getInventory().removeItem(new ItemStack(placementMaterial, 1));
                    event.getBlock().setType(ageableMaterial, true);
                }
            }
        }
        if (experience > 0) {
            event.getPlayer().giveExp(experience, true);
        }
    }
    public void onBlockDropItem(@NotNull BlockDropItemEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        List<Item> items = event.getItems();
        Collection<ItemStack> blockItems = new ArrayList<>();
        items.forEach((item) -> blockItems.add(item.getItemStack()));
        blockItems.forEach(itemStack -> {
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemStack);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
        });
        event.setCancelled(true);
    }
}
