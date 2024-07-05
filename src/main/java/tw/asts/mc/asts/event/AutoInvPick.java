package tw.asts.mc.asts.event;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import tw.asts.mc.asts.util.UserConfig;

import java.util.*;
import java.util.stream.Stream;

public class AutoInvPick {
    private UserConfig userConfig;
    private List<Material> dropBlock;
    public AutoInvPick(UserConfig userConfig) {
        this.userConfig = userConfig;
        List<String> colors = List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink");
        List<String> colorsBlock = List.of("stained_glass", "stained_glass_pane");
        List<Material> normalBlock = List.of(Material.BOOKSHELF, Material.TINTED_GLASS, Material.SEA_LANTERN, Material.ENDER_CHEST, Material.BUDDING_AMETHYST, Material.PACKED_ICE, Material.BLUE_ICE, Material.ICE, Material.GLASS, Material.GLASS_PANE);
        dropBlock = Stream.concat(colorsBlock.stream().map(block -> colors.stream().map(color -> Material.getMaterial((color + "_" + block).toUpperCase())).toList()).flatMap(List::stream), normalBlock.stream()).filter(Objects::nonNull).toList();
    }
    public void onAutoInvPick(BlockBreakEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        int experience = event.getExpToDrop();
        event.setDropItems(false);
        event.setExpToDrop(0);
        if (dropBlock.contains(event.getBlock().getType())) {
            ItemStack blockItem = new ItemStack(event.getBlock().getType(), 1);
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(blockItem);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
            event.getPlayer().giveExp(experience, true);
            return;
        }
        Collection<ItemStack> blockItems = event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer());
        BlockState blockState = event.getBlock().getState();
        if (blockState instanceof Container && !(blockState instanceof ShulkerBox)) {
            Container container = (Container) blockState;
            Arrays.stream(container.getInventory().getStorageContents()).toList().forEach(itemStack -> {
                if (itemStack != null) {
                    blockItems.add(itemStack);
                }
            });
        }
        blockItems.forEach(itemStack -> {
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemStack);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
        });
        event.getPlayer().giveExp(experience, true);
    }
}
