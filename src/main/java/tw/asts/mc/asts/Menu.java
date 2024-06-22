package tw.asts.mc.asts;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.kyori.adventure.text.Component;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.inventory.InventoryHolder;
import java.util.List;

public final class Menu implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (stack.getExecutor() == null || stack.getExecutor().getType() != EntityType.PLAYER) {
            stack.getSender().sendMessage(TextComponent.fromLegacyText(basicConfig.prefix("選單") + "只有玩家可以使用此指令！"));
            return;
        }
        MenuInventory menuInventory = new MenuInventory(stack.getSender(), args);
        String playerName = stack.getExecutor().getName();
        stack.getSender().getServer().getPlayer(playerName).openInventory(menuInventory.getInventory());
    }
}
final class MenuInventory implements InventoryHolder {
    private final Inventory inventory;
    public MenuInventory(@NotNull CommandSender sender, @NotNull String[] args) {
        this.inventory = sender.getServer().createInventory(this, 54, text.miniMessageComponent(text.miniMessage("§6[§4ASTS§6]§9伺服器§5選單")));
        Material materialFill = Material.getMaterial("GRAY_STAINED_GLASS_PANE");
        if (materialFill != null) {
            ItemStack glass = new ItemStack(materialFill, 1);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.displayName(text.miniMessageComponent(""));
            glass.setItemMeta(glassMeta);
            glass.lore(List.of(text.miniMessageComponent(text.miniMessage("§7/help"))));
            for (int i = 0; i < 54; i++) {
                inventory.setItem(i, glass);
            }
        }
    }
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
final class MenuClick implements Listener {
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