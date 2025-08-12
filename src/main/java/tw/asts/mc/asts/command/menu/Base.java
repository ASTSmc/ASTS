package tw.asts.mc.asts.command.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Base {
    @NotNull
    protected final Player user;
    @NotNull
    protected final String[] args;
    @NotNull
    protected final YamlConfiguration configMenu;
    public Base(@NotNull Player user, @NotNull String[] args, @NotNull YamlConfiguration configMenu) {
        this.user = user;
        this.args = args;
        this.configMenu = configMenu;
    }
    @NotNull
    public abstract Boolean open();
}
