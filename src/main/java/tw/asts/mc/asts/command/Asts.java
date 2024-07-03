package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import tw.asts.mc.asts.util.BasicConfig;
import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.UserConfig;
import tw.asts.mc.asts.util.text;

public final class Asts implements BasicCommand {

    private Plugin plugin;
    private FileConfiguration config;
    private UserConfig userConfig;
    private Menu menu;

    public Asts(Plugin plugin, FileConfiguration config, UserConfig userConfig, Menu menu) {
        this.plugin = plugin;
        this.config = config;
        this.userConfig = userConfig;
        this.menu = menu;
    }
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 0) {
            PluginMeta meta = plugin.getPluginMeta();
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.name + "\n版本：" + meta.getVersion() + "\n作者：" + meta.getAuthors().stream().reduce((a, b) -> a + "、" + b).orElse("無"))));
            return;
        }
        else if (stack.getExecutor() == null || stack.getExecutor().getType() != EntityType.PLAYER) {
            stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.name + "\n§cs只有玩家可以使用此指令！")));
            return;
        }
        else if (args[0].equals("mob") || args[0].equals("sb")) {
            if (args.length == 1) {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c請輸入子指令")));
                return;
            }
            else if (stack.getExecutor().getType() != EntityType.PLAYER) {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c只有玩家可以使用此指令")));
                return;
            }
            String setName = "unknown";
            String setType = "unknown";
            Boolean setDefault = false;
            if (args[0].equals("mob")) {
                setName = "mob_attack";
                setType = "怪物攻擊";
            }
            else if (args[0].equals("sb")) {
                setName = "scoreboard";
                setType = "計分板";
                setDefault = true;
            }
            String set = setName + "." + stack.getExecutor().getName();
            if (args[1].equals("true")) {
                userConfig.config.set(set, true);
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§a已開啟" + setType)));
            }
            else if (args[1].equals("false")) {
                userConfig.config.set(set, false);
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§a已關閉" + setType)));
            }
            else if (args[1].equals("toggle")) {
                if (userConfig.config.isSet(set)) {
                    setDefault = userConfig.config.getBoolean(set);
                }
                userConfig.config.set(set, !setDefault);
                if (setDefault) {
                    stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§a已切換關閉" + setType)));
                }
                else {
                    stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§a已切換開啟" + setType)));
                }
                if (args[0].equals("sb")) {
                    if (stack.getSender().getServer().getCommandMap().getCommand("asb") != null) {
                        String userName = stack.getExecutor().getName();
                        if (setDefault) {
                            stack.getExecutor().getServer().dispatchCommand(stack.getExecutor(), "asb toggle off");
                        }
                        else {
                            stack.getExecutor().getServer().dispatchCommand(stack.getExecutor(), "asb toggle on");
                        }
                    }
                }
            }
            else {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c未知子指令")));
                return;
            }
            userConfig.save();
            return;
        }
        else if (args[0].equals("admin")) {
            if (!stack.getSender().hasPermission(PluginPermission.admin())) {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c你沒有權限使用此指令")));
                return;
            }
            else if (args.length == 1) {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c請輸入子指令")));
                return;
            }
            else if (args[1].equals("reload")) {
                boolean save = false;
                if (args.length == 3 && args[2].equals("save")) {
                    save = true;
                }
                else if (args.length != 2) {
                    stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c未知子指令")));
                    return;
                }
                config = plugin.getConfig();
                config.options().copyDefaults(true);
                plugin.saveConfig();
                userConfig.reload(save);
                menu.reload();
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§a重載設定檔成功")));
            }
            else {
                stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c未知子指令")));
                return;
            }
        }
        stack.getSender().sendMessage(text.miniMessageComponent(text.miniMessage("§c未知子指令")));
        return;
    }
    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        List<String> arg0 = List.of("mob", "sb");
        if (stack.getSender().hasPermission(PluginPermission.admin())) {
            arg0 = List.of("mob", "sb", "admin");
        }
        if (args.length == 0) {
            return arg0;
        }
        if (args[0].equals("mob") || args[0].equals("sb")) {
            List<String> arg1 = List.of("true", "false", "toggle");
            if (args.length == 1) {
                return arg1;
            }
            else if (args[1].equals("true") || args[1].equals("false") || args[1].equals("toggle")) {
                return List.of();
            }
            else if (args.length == 2) {
                return arg1.stream().filter(a -> a.startsWith(args[1])).toList();
            }
        }
        else if (args[0].equals("admin")) {
            List<String> arg1 = List.of("reload");
            if (args.length == 1) {
                return arg1;
            }
            else if (args[1].equals("reload")) {
                List<String> arg2 = List.of("save");
                if (args.length == 2) {
                    return arg2;
                }
                else if (args[2].equals("save")) {
                    return List.of();
                }
                else if (args.length == 3) {
                    return arg2.stream().filter(a -> a.startsWith(args[2])).toList();
                }
            }
        }
        else if (args.length == 1) {
            return arg0.stream().filter(a -> a.startsWith(args[0])).toList();
        }
        return List.of();
    }
}
