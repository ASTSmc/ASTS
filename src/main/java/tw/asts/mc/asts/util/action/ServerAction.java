package tw.asts.mc.asts.util.action;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.text;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ServerAction {
    @NotNull
    private final Server server;
    private CommandExecuteAction commandExecuteAction = null;
    public ServerAction(@NotNull Server server) {
        this.server = server;
    }
    public ServerAction(@NotNull Server server, @NotNull CommandExecuteAction commandExecuteAction) {
        this(server);
        this.commandExecuteAction = commandExecuteAction;
    }
    public void sendMessage(@NotNull Component message) {
        if (commandExecuteAction != null) {
            commandExecuteAction.sendMessage(message);
        }
    }
    public void sendMessage(@NotNull Component message, boolean title) {
        if (title && commandExecuteAction != null && commandExecuteAction.getName() != null) {
            sendMessage(text.o(text.prefix(commandExecuteAction.getName()), text.nl(), message));
            return;
        }
        sendMessage(message);
    }
    public @NotNull Server getServer() {
        return server;
    }
    public boolean teleport(@Nullable Entity entity, @NotNull Location location) {
        if (entity == null || !entity.isValid()) {
            return false;
        }
        return entity.teleport(location);
    }
    public boolean isPlayer(@Nullable Entity entity) {
        return entity != null && entity.getType() == EntityType.PLAYER;
    }
    public @Nullable Player getPlayer(@Nullable Entity entity) {
        return isPlayer(entity) ? (Player) entity : null;
    }
    public @Nullable String getPlayerName(Player player) {
        return player != null ? player.getName() : null;
    }
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }
    public boolean hasPermission(@Nullable Player player, @NotNull String permission) {
        return player != null && player.hasPermission(permission);
    }
    public @NotNull CommandMap getCommands() {
        return getServer().getCommandMap();
    }
    public @Nullable Command getCommand(@NotNull String name) {
        return getCommands().getCommand(name);
    }
    public boolean hasCommand(@Nullable Command command) {
        return command != null;
    }
    public boolean hasCommand(@NotNull String name) {
        return hasCommand(getCommand(name));
    }
    public boolean dispatchCommand(@Nullable Command command, @Nullable CommandSender sender, @NotNull String... args) {
        if (sender == null || !hasCommand(command)) {
            return false;
        } else if (command != null && command.testPermissionSilent(sender)) {
            return command.execute(sender, command.getName(), args);
        }
        sendMessage(text.t("asts.cmd.noPermission"));
        return false;
    }
    public boolean dispatchCommand(@Nullable Command command, @Nullable CommandSender sender, @NotNull List<String> args) {
        return dispatchCommand(command, sender, args.toArray(new String[0]));
    }
    public boolean dispatchCommand(@NotNull String command, @Nullable CommandSender sender, @NotNull String... args) {
        return dispatchCommand(getCommand(command), sender, args);
    }
    public boolean dispatchCommand(@NotNull String command, @Nullable CommandSender sender, @NotNull List<String> args) {
        return dispatchCommand(getCommand(command), sender, args);
    }
}
