package tw.asts.mc.asts.event;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import tw.asts.mc.asts.util.UserConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class AutoInvPick {
    private UserConfig userConfig;
    public AutoInvPick(UserConfig userConfig) {
        this.userConfig = userConfig;
    }
    public void onAutoInvPick(BlockBreakEvent event) {
        if (userConfig.config.isSet("auto_inv_pick." + event.getPlayer().getName()) && !userConfig.config.getBoolean("auto_inv_pick." + event.getPlayer().getName())) {
            return;
        }
        Collection<ItemStack> blockItems = event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer());
        BlockState blockState = event.getBlock().getState();
        int experience = event.getExpToDrop();
        if (blockState instanceof Container && !(blockState instanceof ShulkerBox)) {
            Container container = (Container) blockState;
            Arrays.stream(container.getInventory().getStorageContents()).toList().forEach(itemStack -> {
                if (itemStack != null) {
                    blockItems.add(itemStack);
                }
            });
        }
        event.setDropItems(false);
        blockItems.forEach(itemStack -> {
            HashMap<Integer, ItemStack> remaining = event.getPlayer().getInventory().addItem(itemStack);
            remaining.values().forEach(item -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
        });
        event.getPlayer().giveExp(experience, true);
    }
}
