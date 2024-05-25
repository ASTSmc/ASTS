package tw.asts.mc.asts;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.BasicCommand;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ArrayList;
import java.util.stream.Collectors;


public final class Wtp implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1 && stack.getExecutor().getType() == EntityType.PLAYER) {
            stack.getSender().getServer().getWorlds().forEach(world -> {
                if (world.getName().equals(args[0])) {
                    stack.getExecutor().sendMessage(TextComponent.fromLegacyText(config.prefix("世界傳送") + "正在把您傳送至" + args[0] + "..."));
                    stack.getExecutor().teleport(world.getSpawnLocation());
                    return;
                }
            });
        }
        else {
            stack.getSender().sendMessage(TextComponent.fromLegacyText(config.prefix("世界傳送") + "請輸入正確的世界名稱！\n目前可用世界：" + stack.getSender().getServer().getWorlds().stream().map((world) -> world.getName()).collect(Collectors.joining("、"))));
        }
    }
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        ArrayList<String> reply = new ArrayList<String>();
        if (args.length == 0) {
            stack.getSender().getServer().getWorlds().forEach(world -> {
                reply.add(world.getName());
            });
        }
        return reply;
    }
}