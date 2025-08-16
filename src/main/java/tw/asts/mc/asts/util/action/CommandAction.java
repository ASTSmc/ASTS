package tw.asts.mc.asts.util.action;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.text;

@SuppressWarnings("unused")
public abstract class CommandAction implements BasicCommand {
    public CommandAction() {}
    public CommandAction(Component name) {
        this();
        setName(name);
    }
    public CommandAction(@NotNull String name) {
        this(text.l(name));
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
    protected @NotNull CommandExecuteAction executeAction(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final Component name = getName();
        if (name != null) {
            return new CommandExecuteAction(stack, args, name);
        }
        return new CommandExecuteAction(stack, args);
    }
}
