package tw.asts.mc.asts.command.menu.menuParser;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.command.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final public class Player implements Base {
    private final String command;
    private final String description;
    @NotNull
    private final org.bukkit.entity.Player user;
    private List<MenuItem> items = null;
    public Player(String command, String description, @NotNull org.bukkit.entity.Player user) {
        this.command = command;
        this.description = description;
        this.user = user;
    }

    @Override
    public List<MenuItem> getItems() {
        if (items == null) {
            final ArrayList<MenuItem> newItems = new ArrayList<>();
            List<org.bukkit.entity.Player> players = user.getServer().getOnlinePlayers().stream().filter(p -> !p.getName().equals(user.getName())).collect(Collectors.toList());
            if (players.isEmpty()) {
                newItems.add(new MenuItem("§c沒有可用的玩家！", null, null, new ItemStack(Material.BARRIER, 1)));
            } else {
                for (org.bukkit.entity.Player player : players) {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
                    itemMeta.setOwningPlayer(player);
                    item.setItemMeta(itemMeta);
                    final String playerName = player.getName();
                    newItems.add(new MenuItem(playerName, description, command.replaceAll("%player%", playerName), item));
                }
            }
            items = newItems.stream().toList();
        }
        return items;
    }
}
