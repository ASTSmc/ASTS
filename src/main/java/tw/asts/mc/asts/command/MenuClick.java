package tw.asts.mc.asts.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
            event.getWhoClicked().closeInventory();
            Command commandServer = event.getWhoClicked().getServer().getCommandMap().getCommand(command.split(" ")[0]);
            if (commandServer != null) {
                event.getWhoClicked().getServer().dispatchCommand(event.getWhoClicked(), command);
            }
        }
    }
}
