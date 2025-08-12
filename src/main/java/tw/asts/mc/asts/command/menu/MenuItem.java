package tw.asts.mc.asts.command.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.text;

import java.util.ArrayList;
import java.util.List;

final public class MenuItem {
    @NotNull
    final private String name;
    final private String description;
    final private String command;
    final private ItemStack item;
    public MenuItem(@NotNull String name, String description, String command, ItemStack item) {
        this.name = name;
        this.description = description;
        this.command = command;
        this.item = item;
    }

    public @NotNull String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    private boolean gotItem = false;
    public ItemStack getItem() {
        if (item == null) {
            return null;
        } else if (gotItem) {
            return item;
        }
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.customName(text.miniMessageComponent(text.miniMessage("§6" + name)));
        final ArrayList<Component> lores = new ArrayList<>();
        List<Component> lore = itemMeta.lore();
        if (lore != null) {
            lores.addAll(lore);
        }
        if (description != null && !description.isEmpty()) {
            lores.add(text.miniMessageComponent(text.miniMessage("§7" + description)));
        }
        if (command != null && !command.isEmpty()) {
            lores.add(text.miniMessageComponent(text.miniMessage("§7/" + command)));
        }
        itemMeta.lore(lores.isEmpty() ? null : lores.stream().toList());
        item.setItemMeta(itemMeta);
        gotItem = true;
        return item;
    }
}
