package tw.asts.mc.asts.event;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import tw.asts.mc.asts.util.UserConfig;

import java.util.*;
import java.util.stream.Stream;

public class AutoInvPick {
    private final UserConfig userConfig;
    private final List<Material> dropBlock;
    public AutoInvPick(UserConfig userConfig) {
        this.userConfig = userConfig;
        List<String> colors = List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink");
        List<String> colorsBlock = List.of("stained_glass", "stained_glass_pane");
        List<Material> normalBlock = List.of(Material.BOOKSHELF, Material.TINTED_GLASS, Material.SEA_LANTERN, Material.ENDER_CHEST, Material.BUDDING_AMETHYST, Material.PACKED_ICE, Material.BLUE_ICE, Material.ICE, Material.GLASS, Material.GLASS_PANE);
        dropBlock = Stream.concat(colorsBlock.stream().map(block -> colors.stream().map(color -> Material.getMaterial((color + "_" + block).toUpperCase())).toList()).flatMap(List::stream), normalBlock.stream()).filter(Objects::nonNull).toList();
    }
    public void onBlockBreak(BlockBreakEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        int experience = event.getExpToDrop();
        event.setExpToDrop(0);
        if (dropBlock.contains(event.getBlock().getType())) {
            ItemStack blockItem = new ItemStack(event.getBlock().getType(), 1);
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(blockItem);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
            if (experience > 0) {
                event.getPlayer().giveExp(experience, true);
            }
            event.setDropItems(false);
            return;
        }
        if (experience > 0) {
            event.getPlayer().giveExp(experience, true);
        }
    }
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        List<Item> items = event.getItems();
        Collection<ItemStack> blockItems = new ArrayList<>();
        items.forEach((item) -> {
            blockItems.add(item.getItemStack());
        });
        blockItems.forEach(itemStack -> {
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemStack);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
        });
        event.setCancelled(true);
    }
}
