package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.bukkit.inventory.InventoryHolder;
import tw.asts.mc.asts.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Menu implements BasicCommand {

    public File fileMenu;
    public YamlConfiguration configMenu;

    public Menu(Plugin plugin) {
        plugin.saveResource("menu.yml", false);
        fileMenu = new File(plugin.getDataFolder(), "menu.yml");
        configMenu = YamlConfiguration.loadConfiguration(fileMenu);
    }
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (stack.getExecutor() == null || stack.getExecutor().getType() != EntityType.PLAYER) {
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("選單") + "只有玩家可以使用此指令！")));
            return;
        }
        FloodgateApi floodgateApi = FloodgateApi.getInstance();
        if (floodgateApi.isFloodgatePlayer(stack.getExecutor().getUniqueId())) {
            MenuBedrock menuBedrock = new MenuBedrock(stack.getSender(), args, configMenu);
            floodgateApi.sendForm(stack.getExecutor().getUniqueId(), menuBedrock.getForm());
            return;
        }
        MenuInventory menuInventory = new MenuInventory(stack.getSender(), args, configMenu);
        String playerName = stack.getExecutor().getName();
        stack.getSender().getServer().getPlayer(playerName).openInventory(menuInventory.getInventory());
    }
}
final class MenuInventory implements InventoryHolder {
    private final Inventory inventory;
    private final Server server;
    private YamlConfiguration configMenu;
    public MenuInventory(@NotNull CommandSender sender, @NotNull String[] args, YamlConfiguration configMenu) {
        this.configMenu = configMenu;
        this.server = sender.getServer();
        this.inventory = sender.getServer().createInventory(this, 54, text.miniMessageComponent(text.miniMessage("§6[§4ASTS§6]§9伺服器§5選單")));
        Material materialFill = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack glass = new ItemStack(materialFill, 1);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(text.miniMessageComponent(""));
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        this.setMenu(sender, args);
    }
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    private void setMenu(@NotNull CommandSender sender, @NotNull String[] args) {
        // 沒有該選單
        if (!configMenu.contains("menu." + String.join(".", args) + ".default")) {
            return;
        }
        // 特殊類型
        else if (configMenu.isString("menu." + String.join(".", args) + ".default")) {
            String[] type = configMenu.getString("menu." + String.join(".", args) + ".default").split("\\.");
            if (type.length != 2) {
                return;
            }
            // 在線玩家
            else if (type[0].equals("player")) {
                List<Player> players = server.getOnlinePlayers().stream().filter(p -> !p.getName().equals(sender.getName())).collect(Collectors.toList());
                if (players.size() == 0) {
                    ItemStack item = new ItemStack(Material.BARRIER, 1);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.displayName(text.miniMessageComponent(text.miniMessage("§c沒有可用的玩家！")));
                    item.setItemMeta(itemMeta);
                    inventory.setItem((args.length + 1) * 9, item);
                }
                for (int i = 0; i < players.size() && i + (args.length + 1) * 9 < 54; i++) {
                    String command = type[1].replaceAll("%player%", players.get(i).getName());
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
                    itemMeta.setOwningPlayer(players.get(i));
                    itemMeta.displayName(text.miniMessageComponent(text.miniMessage("§6" + players.get(i).getName())));
                    item.setItemMeta(itemMeta);
                    item.lore(List.of(text.miniMessageComponent(text.miniMessage("§7/" + command))));
                    inventory.setItem(i + (args.length + 1) * 9, item);
                }
            }
            else {
                return;
            }
        }
        // 一般選單
        else if (configMenu.isList("menu." + String.join(".", args) + ".default")) {
            List<?> menuItems = configMenu.getList("menu." + String.join(".", args) + ".default");
            if (menuItems == null) return;
            for (int i = 0; i < menuItems.size() && i + (args.length + 1) * 9 < 54; i++) {
                if (!(menuItems.get(i) instanceof Map)) continue;
                Map<?, ?> menuItem = (Map<?, ?>) menuItems.get(i);
                String name = (String) menuItem.get("name");
                String itemMaterialName = (String) menuItem.get("item");
                Material material = Material.getMaterial(itemMaterialName.toUpperCase());
                if (material == null) continue;
                ItemStack item = new ItemStack(material, 1);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(text.miniMessageComponent(text.miniMessage("§6" + name)));
                String command = null;
                if (menuItem.containsKey("cmd")) {
                    command = (String) menuItem.get("cmd");
                } else if (menuItem.containsKey("menu")) {
                    String menuPath = (String) menuItem.get("menu");
                    command = "menu " + menuPath.replaceAll("\\.", " ");
                }
                if (command != null) {
                    if (menuItem.containsKey("desc")) {
                        String desc = (String) menuItem.get("desc");
                        meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7" + desc)), text.miniMessageComponent(text.miniMessage("§7/" + command))));
                    }
                    else {
                        meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7/" + command))));
                    }
                }
                else if (menuItem.containsKey("desc")) {
                    String desc = (String) menuItem.get("desc");
                    meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7" + desc))));
                }
                item.setItemMeta(meta);
                inventory.setItem(i + (args.length + 1) * 9, item);
            }
        }
        // 顯示上層分格欄
        Material materialLine = Material.PURPLE_STAINED_GLASS_PANE;
        ItemStack lineGlass = new ItemStack(materialLine, 1);
        ItemMeta lineGlassMeta = lineGlass.getItemMeta();
        lineGlassMeta.displayName(text.miniMessageComponent(""));
        lineGlass.setItemMeta(lineGlassMeta);
        for (int i = args.length * 9; i < (args.length + 1) * 9; i++) {
            inventory.setItem(i, lineGlass);
        }
        // 顯示上層選單
        String nowPath = String.join(".", args);
        for (int i = 0; i < args.length; i++) {
            if (configMenu.isList("menu." + String.join(".", Arrays.copyOfRange(args, 0, i)) + ".default")) {
                List<?> menuItems = configMenu.getList("menu." + String.join(".", Arrays.copyOfRange(args, 0, i)) + ".default");
                int itemsCount = menuItems.size();
                int startIndex = 4;
                // 計算偏移量
                for (int j = 0; j < itemsCount; j++) {
                    Map<?, ?> menuItem = (Map<?, ?>) menuItems.get(j);
                    if (menuItem.containsKey("menu")) {
                        String menuPath = (String) menuItem.get("menu");
                        if (nowPath.startsWith(menuPath)) {
                            startIndex -= j;
                            break;
                        }
                    }
                }
                // 顯示上層選單物品
                for (int j = 0; j < itemsCount && startIndex + j < 9; j++) {
                    int index = startIndex + j;
                    if (index < 0 || index > 8 || !(menuItems.get(j) instanceof Map)) continue;
                    Map<?, ?> menuItem = (Map<?, ?>) menuItems.get(j);
                    String name = (String) menuItem.get("name");
                    String itemMaterialName = (String) menuItem.get("item");
                    Material material = Material.getMaterial(itemMaterialName.toUpperCase());
                    if (material == null) continue;

                    ItemStack item = new ItemStack(material, 1);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(text.miniMessageComponent(text.miniMessage("§6" + name)));
                    String command = null;
                    if (menuItem.containsKey("cmd")) {
                        command = (String) menuItem.get("cmd");
                    } else if (menuItem.containsKey("menu")) {
                        String menuPath = (String) menuItem.get("menu");
                        command = "menu " + menuPath.replaceAll("\\.", " ");
                    }
                    if (command != null) {
                        if (menuItem.containsKey("desc")) {
                            String desc = (String) menuItem.get("desc");
                            meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7" + desc)), text.miniMessageComponent(text.miniMessage("§7/" + command))));
                        }
                        else {
                            meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7/" + command))));
                        }
                    }
                    else if (menuItem.containsKey("desc")) {
                        String desc = (String) menuItem.get("desc");
                        meta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7" + desc))));
                    }
                    item.setItemMeta(meta);
                    inventory.setItem(i * 9 + index, item);
                }
            }
        }
        // 關閉選單
        Material materialExit = Material.RED_STAINED_GLASS_PANE;
        ItemStack exitGlass = new ItemStack(materialExit, 1);
        ItemMeta exitMeta = exitGlass.getItemMeta();
        exitMeta.displayName(text.miniMessageComponent(text.miniMessage("§c關閉選單")));
        exitGlass.setItemMeta(exitMeta);
        inventory.setItem(8, exitGlass);
        if (args.length != 0) {
            Material materialTop = Material.ORANGE_STAINED_GLASS_PANE;
            ItemStack topGlass = new ItemStack(materialTop, 1);
            ItemMeta topMeta = exitGlass.getItemMeta();
            topMeta.displayName(text.miniMessageComponent(text.miniMessage("§e返回主選單")));
            topMeta.lore(List.of(text.miniMessageComponent(text.miniMessage("§7/menu"))));
            topGlass.setItemMeta(topMeta);
            inventory.setItem(0, topGlass);
        }
    }
}
final class MenuBedrock {
    private SimpleForm.Builder form = SimpleForm.builder();
    private YamlConfiguration configMenu;
    public MenuBedrock(@NotNull CommandSender sender, @NotNull String[] args, YamlConfiguration configMenu) {
        this.configMenu = configMenu;
        form.title("§6[§4ASTS§6]§9伺服器§5選單");
        setForm(sender, args);
    }

    public SimpleForm getForm() {
        return form.build();
    }

    public void setForm(@NotNull CommandSender sender, @NotNull String[] args) {
        // 沒有該選單
        if (!configMenu.contains("menu." + String.join(".", args) + ".default")) {
            return;
        }
        else if (args.length == 0) {
            form.content("請選擇類別");
        }
        else {
            String name = "§6選單";
            for (int i = 0; i < args.length; i++) {
                if (configMenu.isList("menu." + String.join(".", Arrays.copyOfRange(args, 0, i)) + ".default")) {
                    List<?> menuItems = configMenu.getList("menu." + String.join(".", Arrays.copyOfRange(args, 0, i)) + ".default");
                    int itemsCount = menuItems.size();
                    //
                    for (int j = 0; j < itemsCount; j++) {
                        Map<?, ?> menuItem = (Map<?, ?>) menuItems.get(j);
                        if (menuItem.containsKey("menu")) {
                            String menuPath = (String) menuItem.get("menu");
                            if (String.join(".", args).startsWith(menuPath)) {
                                name += ">" + (String) menuItem.get("name");
                                break;
                            }
                        }
                    }
                }
            }
            form.content(name);
        }
        // 特殊類型
        if (configMenu.isString("menu." + String.join(".", args) + ".default")) {
            String[] type = configMenu.getString("menu." + String.join(".", args) + ".default").split("\\.");
            if (type.length != 2) {
                return;
            }
            // 在線玩家
            else if (type[0].equals("player")) {
                List<Player> players = sender.getServer().getOnlinePlayers().stream().filter(p -> !p.getName().equals(sender.getName())).collect(Collectors.toList());
                if (players.size() == 0) {
                    form.button("§c沒有可用的玩家！");
                }
                for (int i = 0; i < players.size(); i++) {
                    String command = type[1].replaceAll("%player%", players.get(i).getName());
                    form.button(players.get(i).getName() + "\n/" + command);
                }
            }
            else {
                return;
            }
        }
        // 一般選單
        else if (configMenu.isList("menu." + String.join(".", args) + ".default")) {
            List<?> menuItems = configMenu.getList("menu." + String.join(".", args) + ".default");
            if (menuItems == null) return;
            for (int i = 0; i < menuItems.size(); i++) {
                if (!(menuItems.get(i) instanceof Map)) continue;
                Map<?, ?> menuItem = (Map<?, ?>) menuItems.get(i);
                String name = (String) menuItem.get("name");
                String command = null;
                if (menuItem.containsKey("cmd")) {
                    command = (String) menuItem.get("cmd");
                } else if (menuItem.containsKey("menu")) {
                    String menuPath = (String) menuItem.get("menu");
                    command = "menu " + menuPath.replaceAll("\\.", " ");
                }
                form.button(name + "\n/" + command);
            }
        }
        if (args.length != 0) {
            form.button("返回主選單");
        }
        form.validResultHandler(response -> {
            if (response == null) return;
            String[] result = response.clickedButton().text().split("\n");
            String command = result[result.length - 1].substring(1);
            String commandName = command.split(" ")[0];
            if (sender.getServer().getCommandMap().getCommand(commandName) != null) {
                sender.getServer().dispatchCommand(sender, command);
            }
            else if (commandName.equals("返回主選單".substring(1))) {
                sender.getServer().dispatchCommand(sender, "menu");
            }
        });
    }
}