package tw.asts.mc.asts.command.menu.menuParser;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tw.asts.mc.asts.command.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final public class Basic implements Base {
    final private List<?> data;
    private List<MenuItem> items = null;
    public Basic(List<?> data) {
        this.data = data;
    }

    @Override
    public List<MenuItem> getItems() {
        if (items == null) {
            final ArrayList<MenuItem> newItems = new ArrayList<>();
            for (var d : data) {
                if (!(d instanceof Map<?, ?> dMap)) continue;
                final String dName = dMap.containsKey("name") && (dMap.get("name") instanceof String) ? (String) dMap.get("name") : null;
                if (dName == null) continue;
                final String dDesc = dMap.containsKey("desc") && (dMap.get("desc") instanceof String) ? (String) dMap.get("desc") : null;
                final String dCmd = dMap.containsKey("cmd") && (dMap.get("cmd") instanceof String) ? (String) dMap.get("cmd") : dMap.containsKey("menu") && (dMap.get("menu") instanceof String) ? "menu " + ((String) dMap.get("menu")).replaceAll("\\.", " ") : null;
                final String dItemStr = dMap.containsKey("item") && (dMap.get("item") instanceof String) ? (String) dMap.get("item") : null;
                final Material material = dItemStr != null ? Material.getMaterial(dItemStr.toUpperCase()): null;
                final ItemStack dItem = ItemStack.of(material != null ? material : Material.BARRIER, 1);
                final MenuItem menuItem = new MenuItem(dName, dDesc, dCmd, dItem);
                newItems.add(menuItem);
            }
            items = newItems.stream().toList();
        }
        return items;
    }
}
