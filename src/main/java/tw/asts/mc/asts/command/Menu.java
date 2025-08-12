package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.BasicConfig;
import tw.asts.mc.asts.util.ExternalClass;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.text;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public final class Menu implements BasicCommand {

    private Plugin plugin;
    public File fileMenu;
    public YamlConfiguration configMenu;

    public Menu(Plugin plugin) {
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
            Log.get().log(Level.WARNING, "無法重新載入選單設定檔案: " + e.getMessage(), e.getCause());
        }
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        if (stack.getExecutor() == null || stack.getExecutor().getType() != EntityType.PLAYER) {
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("選單") + "只有玩家可以使用此指令！")));
            return;
        }
        Player player = (Player) stack.getExecutor();
        if (open("Bedrock", player, args)) {
            return;
        } else if (open("Inventory", player, args)) {
            return;
        }
        player.sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("無法開啟選單"))));
    }

    private boolean open(@NotNull String type, Player player, @NotNull String[] args) {
        final ExternalClass externalClass = ExternalClass.plugin(
                plugin,
                List.of("Geyser-Spigot", "floodgate"),
                "tw.asts.mc.asts.command.menu." + type,
                List.of(Player.class, args.getClass(), YamlConfiguration.class),
                List.of(player, args, configMenu)
        );
        Boolean result = (Boolean) externalClass.runMethod("open");
        if (result == null) {
            return false;
        }
        return result;
    }
}