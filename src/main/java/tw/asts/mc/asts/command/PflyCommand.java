package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.action.CommandAction;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PflyCommand extends CommandAction {

    private final Pfly pfly;

    public PflyCommand(@NotNull Pfly pfly) {
        super("玩家飛行");
        this.pfly = pfly;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final CommandExecuteAction executeAction = executeAction(stack, args);
        if (executeAction.onlyPlayer()) {
            return;
        }
        final Player player = Objects.requireNonNull(executeAction.getPlayer());
        if (args.length > 0 && args[0].equalsIgnoreCase("accept")) {
            pfly.accept(player);
            return;
        }
        if (args.length > 0) {
            executeAction.sendMessage(text.t("asts.cmd.unknownArg"), true);
            return;
        }
        pfly.toggle(player);
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        if (args.length <= 1) {
            return List.of("accept");
        }
        return List.of();
    }

    @Override
    public String permission() {
        return PluginPermission.commandPfly;
    }
}
