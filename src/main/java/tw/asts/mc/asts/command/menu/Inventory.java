package tw.asts.mc.asts.command.menu;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

final public class Inventory extends Base implements InventoryHolder {
    @NotNull
    private final org.bukkit.inventory.Inventory inventory;
    public Inventory(@NotNull CommandExecuteAction executeAction, @NotNull YamlConfiguration configMenu) {
        super(executeAction, configMenu);
        this.inventory = executeAction.getServer().createInventory(this, 54, text.m(text.legacy("§6[§4ASTS§6]§9伺服器§5選單")));
        // 覆蓋灰玻璃片
        final Material materialFill = Material.GRAY_STAINED_GLASS_PANE;
        final ItemStack glass = new ItemStack(materialFill, 1);
        final ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.customName(text.m(""));
        glass.setItemMeta(glassMeta);
        fillItems(glass, 0, inventory.getSize());
        // 目前選單
        final MenuParser menuList = new MenuParser(Objects.requireNonNull(executeAction.getPlayer()), configMenu, executeAction.getArgs());
        final List<MenuItem> menuItems = menuList.getItems();
        if (menuItems != null) {
            setMenuItems(menuItems, (executeAction.getArgs().length + 1) * 9, inventory.getSize(), 0);
        }
        // 顯示上層選單
        for (int i = 0; i < executeAction.getArgs().length; i++) {
            final MenuParser menuTopList = new MenuParser(Objects.requireNonNull(executeAction.getPlayer()), configMenu, Arrays.copyOfRange(executeAction.getArgs(), 0, i));
            final List<MenuItem> menuTopItems = menuTopList.getItems();
            if (menuTopItems == null) continue;
            final String menuTopCmd = "menu " + String.join(" ", Arrays.copyOfRange(executeAction.getArgs(), 0, i + 1));
            final MenuItem menuTopItem = menuTopItems.stream().filter((mI) -> menuTopCmd.equals(mI.getCommand())).findFirst().orElse(null);
            if (menuTopItem == null) continue;
            final int menuTopItemIndex = menuTopItems.indexOf(menuTopItem);
            if (menuTopItemIndex < 0) continue;
            setMenuItems(menuTopItems, i * 9, (i + 1) * 9, 4 - menuTopItemIndex);
        }
        // 顯示上層分格欄
        final Material materialLine = Material.PURPLE_STAINED_GLASS_PANE;
        final ItemStack lineGlass = new ItemStack(materialLine, 1);
        final ItemMeta lineGlassMeta = lineGlass.getItemMeta();
        lineGlassMeta.displayName(text.m(""));
        lineGlass.setItemMeta(lineGlassMeta);
        fillItems(lineGlass, executeAction.getArgs().length * 9, (executeAction.getArgs().length + 1) * 9);
        // 關閉選單
        final Material materialExit = Material.RED_STAINED_GLASS_PANE;
        final ItemStack exitGlass = new ItemStack(materialExit, 1);
        final ItemMeta exitMeta = exitGlass.getItemMeta();
        exitMeta.customName(text.l("§c關閉選單"));
        exitMeta.lore(List.of(text.l("§7點我以關閉選單")));
        exitGlass.setItemMeta(exitMeta);
        setItem(8, exitGlass);
        if (executeAction.getArgs().length != 0) {
            // 返回選單
            final Material materialTop = Material.ORANGE_STAINED_GLASS_PANE;
            final ItemStack topGlass = new ItemStack(materialTop, 1);
            final ItemMeta topMeta = exitGlass.getItemMeta();
            topMeta.customName(text.l("§e返回主選單"));
            topMeta.lore(List.of(text.l("§7/menu")));
            topGlass.setItemMeta(topMeta);
            setItem(0, topGlass);
        }
    }
    @Override
    public org.bukkit.inventory.@NotNull Inventory getInventory() {
        return inventory;
    }
    private void setMenuItems(List<MenuItem> menuItems, int invStartIndex, int invEndIndex, int offset) {
        setItems(menuItems.stream().map(MenuItem::getItem).toList(), invStartIndex, invEndIndex, offset);
    }
    private void fillItems(ItemStack items, int invStartIndex, int invEndIndex) {
        setItems(IntStream.range(0, invEndIndex - invStartIndex).mapToObj((i) -> items).toList(), invStartIndex, invEndIndex, 0);
    }
    private void setItems(List<ItemStack> items, int invStartIndex, int invEndIndex, int offset) {
        for (int i = 0; i + offset + invStartIndex < invEndIndex; i++) {
            if (i < 0 || i >= items.size() || i + offset < 0) continue;
            ItemStack item = items.get(i);
            if (item == null) continue;
            setItem(i + offset + invStartIndex, item);
        }
    }
    private void setItem(int index, @Nullable ItemStack item) {
        if (index < 0 || index >= inventory.getSize() || item == null) {
            return;
        }
        inventory.setItem(index, item);
    }

    @Override
    public @NotNull Boolean open() {
        try {
            Objects.requireNonNull(executeAction.getPlayer()).openInventory(inventory);
            return true;
        } catch (Exception e) {
            Log.warn(e);
            return false;
        }
    }
}
