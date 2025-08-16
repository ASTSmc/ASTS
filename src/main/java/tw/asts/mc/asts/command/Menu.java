package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.ExternalClass;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.action.CommandAction;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

import java.io.File;
import java.util.List;

public final class Menu extends CommandAction {

    private final Plugin plugin;
    public File fileMenu;
    public YamlConfiguration configMenu;

    public Menu(Plugin plugin) {
        super("選單");
        this.plugin = plugin;
        plugin.saveResource("menu.yml", false);
        fileMenu = new File(plugin.getDataFolder(), "menu.yml");
        configMenu = YamlConfiguration.loadConfiguration(fileMenu);
    }

    public void reload() {
        try {
            configMenu = YamlConfiguration.loadConfiguration(fileMenu);
        }
        catch (Exception e) {
            Log.warn("無法重新載入選單設定檔案: " + e.getMessage(), e.getCause());
        }
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final CommandExecuteAction executeAction = executeAction(stack, args);
        if (executeAction.onlyPlayer()) {
            return;
        }
        if (open("Bedrock", executeAction)) {
            return;
        } else if (open("Inventory", executeAction)) {
            return;
        }
        executeAction.sendMessage(text.prefix(text.l("無法開啟選單")));
    }

    private boolean open(@NotNull String type, @NotNull CommandExecuteAction executeAction) {
        final ExternalClass externalClass = ExternalClass.plugin(
                plugin,
                List.of("Geyser-Spigot", "floodgate"),
                "tw.asts.mc.asts.command.menu." + type,
                List.of(CommandExecuteAction.class, YamlConfiguration.class),
                List.of(executeAction, configMenu)
        );
        Boolean result = (Boolean) externalClass.runMethod("open");
        if (result == null) {
            return false;
        }
        return result;
    }

    @Override
    public String permission() {
        return PluginPermission.commandMenu;
    }
}