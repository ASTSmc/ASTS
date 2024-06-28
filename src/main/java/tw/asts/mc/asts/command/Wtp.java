package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.*;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

public final class Wtp implements BasicCommand {

    private List<String> disabledWorlds;

    public Wtp(FileConfiguration config) {
        this.disabledWorlds = config.getStringList("wtp.disable.worlds") == null ? List.of() : config.getStringList("wtp.disable.worlds");
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (stack.getExecutor() == null || stack.getExecutor().getType() != EntityType.PLAYER) {
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("世界傳送") + "只有玩家可以使用此指令！")));
            return;
        }
        if (args.length == 1) {
            String worldName = args[0];
            World world = stack.getSender().getServer().getWorlds().stream().filter(w -> w.getName().equals(worldName) && !disabledWorlds.contains(w.getName())).findFirst().orElse(null);
            stack.getExecutor().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("世界傳送") + "正在把您傳送至" + worldName + "...")));
            stack.getExecutor().teleport(world.getSpawnLocation());
        }
        else {
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("世界傳送") + "請輸入正確的世界名稱！\n目前可用世界：" + stack.getSender().getServer().getWorlds().stream().filter(world -> !disabledWorlds.contains(world.getName())).map((world) -> world.getName()).collect(Collectors.joining("、")))));
        }
    }
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            List<String> worlds = stack.getSender().getServer().getWorlds().stream().filter(w -> !disabledWorlds.contains(w.getName())).map(w -> w.getName()).toList();
            if (args.length == 1 && !disabledWorlds.contains(args[0])) {
                return worlds.stream().filter(w -> w.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
            return worlds;
        }
        return List.of();
    }
}