package tw.asts.mc.asts.util.action;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CommandExecuteAction extends CommandStackAction {
    final private @NotNull String @NotNull [] args;
    public CommandExecuteAction(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        super(stack);
        this.args = args;
    }
    public CommandExecuteAction(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args, Component name) {
        super(stack, name);
        this.args = args;
    }
    public @NotNull String @NotNull [] getArgs() {
        return args;
    }
    public boolean hasArg(int index) {
        return index >= 0 && index < args.length;
    }
    public @Nullable String getArg(int index) {
        return hasArg(index) ? args[index] : null;
    }
    public boolean inArg(int index, @NotNull String... values) {
        final String arg = getArg(index);
        if (arg == null) {
            return false;
        }
        for (String value : values) {
            if (arg.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
