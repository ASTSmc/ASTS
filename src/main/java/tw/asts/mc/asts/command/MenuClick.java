package tw.asts.mc.asts.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.block.Action;
import tw.asts.mc.asts.util.text;

import java.util.List;
import java.util.logging.Level;

public final class MenuClick implements Listener {
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof MenuInventory) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.getItemMeta().hasLore()) {
                return;
            }
            List<Component> lore = item.getItemMeta().lore();
            if (lore == null || lore.isEmpty()) {
                return;
            }
            String command = PlainTextComponentSerializer.plainText().serialize(lore.getLast()).substring(1);
            String commandName = command.split(" ")[0];
            Command commandServer = event.getWhoClicked().getServer().getCommandMap().getCommand(commandName);
            if (commandServer != null) {
                if (!commandName.equals("menu")) {
                    event.getWhoClicked().closeInventory();
                }
                event.getWhoClicked().getServer().dispatchCommand(event.getWhoClicked(), command);
            }
            else if (commandName.equals("點我以關閉選單".substring(1))) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(text.miniMessageComponent(text.miniMessage("§6[§4ASTS§6]§eAllen§a跨平台§b插件§f伺服器§r - §6選單已關閉§r")));
            }
            else {
                event.getWhoClicked().getServer().getLogger().log(Level.INFO, "Command not found: " + commandName);
            }
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event){
        if (event.getHand() == EquipmentSlot.HAND && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR)) {
            Player player = event.getPlayer();
            Block block = player.getTargetBlock(null, 100);
            if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS && (block == null || block.getType() != Material.LODESTONE)) {
                event.setCancelled(true);
                event.getPlayer().getServer().dispatchCommand(event.getPlayer(), "menu");
            }
        }
    }
}
