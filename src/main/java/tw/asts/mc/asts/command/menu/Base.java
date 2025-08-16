package tw.asts.mc.asts.command.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.action.CommandExecuteAction;

public abstract class Base {
    @NotNull
    protected final CommandExecuteAction executeAction;
    @NotNull
    protected final YamlConfiguration configMenu;
    public Base(@NotNull CommandExecuteAction executeAction, @NotNull YamlConfiguration configMenu) {
        this.executeAction = executeAction;
        this.configMenu = configMenu;
    }
    @NotNull
    public abstract Boolean open();
}
