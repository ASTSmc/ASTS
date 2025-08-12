package tw.asts.mc.asts.command.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.command.menu.menuParser.Base;
import tw.asts.mc.asts.command.menu.menuParser.Basic;
import tw.asts.mc.asts.command.menu.menuParser.Player;

import java.util.List;
import java.util.Objects;

public class MenuParser implements Base {
    private Base menuParser = null;
    @NotNull
    private final org.bukkit.entity.Player user;
    @NotNull
    private final YamlConfiguration configMenu;
    @NotNull
    private final String[] args;

    public MenuParser(@NotNull org.bukkit.entity.Player user, @NotNull YamlConfiguration configMenu, @NotNull String[] args) {
        this.user = user;
        this.configMenu = configMenu;
        this.args = args;
    }

    private Base getMenuParser() {
        if (menuParser != null) {
            return menuParser;
        } else if (!configMenu.contains("menu." + String.join(".", args) + ".default")) {
            return null;
        } else if (configMenu.isString("menu." + String.join(".", args) + ".default")) {
            final String[] type = Objects.requireNonNull(configMenu.getString("menu." + String.join(".", args) + ".default")).split("\\.", 2);
            final String description = configMenu.isString("menu." + String.join(".", args) + ".description")
                    ? configMenu.getString("menu." + String.join(".", args) + ".description")
                    : null;
            if (type[0].equals("player")) {
                menuParser = new Player(type.length > 1 ? type[1] : null, description, user);
            }
        } else if (configMenu.isList("menu." + String.join(".", args) + ".default")) {
            menuParser = new Basic(configMenu.getList("menu." + String.join(".", args) + ".default"));
        }
        return menuParser;
    }

    @Override
    public List<MenuItem> getItems() {
        final Base mP = getMenuParser();
        return mP == null ? null : mP.getItems();
    }
}
