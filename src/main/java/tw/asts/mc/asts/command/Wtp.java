package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.action.CommandAction;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

public final class Wtp extends CommandAction {

    private final List<String> disabledWorlds;

    public Wtp(FileConfiguration config) {
        this.disabledWorlds = config.getStringList("wtp.disable.worlds");
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final CommandExecuteAction executeAction = executeAction(stack, args);
        if (executeAction.onlyPlayer()) {
            return;
        }
        if (args.length == 1) {
            final String worldName = args[0];
            final World world = executeAction.getServer().getWorlds().stream().filter(w -> w.getName().equals(worldName) && !disabledWorlds.contains(w.getName())).findFirst().orElse(null);
            if (world != null) {
                executeAction.sendMessage(text.t("asts.cmd.tp.teleportTo", text.l(worldName)), true);
                executeAction.teleport(world.getSpawnLocation());
                return;
            }
        }
        executeAction.sendMessage(text.o(text.t("asts.cmd.wtp.notFound"), text.nl(), text.t("asts.cmd.wtp.availableWorlds", text.l(stack.getSender().getServer().getWorlds().stream().map(WorldInfo::getName).filter(name -> !disabledWorlds.contains(name)).collect(Collectors.joining(", "))))), true);
    }
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        if (args.length <= 1) {
            List<String> worlds = stack.getSender().getServer().getWorlds().stream().map(WorldInfo::getName).filter(name -> !disabledWorlds.contains(name)).toList();
            if (args.length == 1 && !disabledWorlds.contains(args[0])) {
                return worlds.stream().filter(w -> w.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
            return worlds;
        }
        return List.of();
    }

    @Override
    public String permission() {
        return PluginPermission.commandWtp;
    }
}