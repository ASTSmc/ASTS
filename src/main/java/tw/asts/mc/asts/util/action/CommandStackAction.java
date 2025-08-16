package tw.asts.mc.asts.util.action;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.text;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CommandStackAction extends ServerAction {
    final private @NotNull CommandSourceStack stack;
    public CommandStackAction(@NotNull CommandSourceStack stack) {
        super(stack.getSender().getServer());
        this.stack = stack;
    }
    public CommandStackAction(@NotNull CommandSourceStack stack, Component name) {
        this(stack);
        setName(name);
    }
    public @NotNull CommandSourceStack getStack() {
        return stack;
    }
    private Component name;
    public Component getName() {
        return name;
    }
    public void setName(Component name) {
        this.name = name;
    }
    public void setName(@NotNull String name) {
        setName(text.l(name));
    }
    public @NotNull CommandSender getSender() {
        return getStack().getSender();
    }
    public @Nullable Entity getExecutor() {
        return getStack().getExecutor();
    }
    public void sendMessage(@NotNull Component message) {
        getSender().sendMessage(message);
    }
    public void sendMessage(@NotNull Component message, boolean title) {
        if (title && getName() != null) {
            sendMessage(text.o(text.prefix(getName()), text.nl(), message));
            return;
        }
        sendMessage(message);
    }
    public boolean teleport(@NotNull Location location) {
        return teleport(getExecutor(), location);
    }
    public boolean isPlayer() {
        return isPlayer(getExecutor());
    }
    public boolean onlyPlayer() {
        if (!isPlayer()) {
            sendMessage(text.t("asts.omd.onlyPlayer"), true);
            return true;
        }
        return false;
    }
    public @Nullable Player getPlayer() {
        return getPlayer(getExecutor());
    }
    public @Nullable String getPlayerName() {
        return getPlayerName(getPlayer());
    }
    public boolean hasPermission(@NotNull String permission) {
        return hasPermission(getSender(), permission);
    }
    public boolean dispatchCommand(@Nullable Command command, @NotNull String... args) {
        return dispatchCommand(command, getExecutor(), args);
    }
    public boolean dispatchCommand(@Nullable Command command, @NotNull List<String> args) {
        return dispatchCommand(command, getExecutor(), args);
    }
    public boolean dispatchCommand(@NotNull String command, @NotNull String... args) {
        return dispatchCommand(getCommand(command), args);
    }
    public boolean dispatchCommand(@NotNull String command, @NotNull List<String> args) {
        return dispatchCommand(getCommand(command), args);
    }
}
