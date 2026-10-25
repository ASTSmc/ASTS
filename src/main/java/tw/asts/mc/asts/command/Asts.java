package tw.asts.mc.asts.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.UserConfig;
import tw.asts.mc.asts.util.action.CommandAction;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

public final class Asts extends CommandAction {

    private final Plugin plugin;
    private FileConfiguration config;
    private final UserConfig userConfig;
    private final Menu menu;
    private final Pfly pfly;

    public Asts(Plugin plugin, FileConfiguration config, UserConfig userConfig, Menu menu, Pfly pfly) {
        super("ASTS指令");
        this.plugin = plugin;
        this.config = config;
        this.userConfig = userConfig;
        this.menu = menu;
        this.pfly = pfly;
    }
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final CommandExecuteAction executeAction = executeAction(stack, args);
        if (args.length == 0) {
            final PluginMeta meta = plugin.getPluginMeta();
            executeAction.sendMessage(text.o(text.t("asts.server"), text.l("\n" + meta.getDescription() + "\n網站：" + meta.getWebsite() + "\n版本：" + meta.getVersion() + "\n作者：" + meta.getAuthors().stream().reduce((a, b) -> a + "、" + b).orElse("無") + "\n貢獻者：" + meta.getContributors().stream().reduce((a, b) -> a + "、" + b).orElse("無"))));
            return;
        }
        else if (executeAction.onlyPlayer()) {
            return;
        }
        else if (executeAction.inArg(0, "mob", "sb", "inv", "pflyinfo")) {
            if (args.length == 1) {
                executeAction.sendMessage(text.t("asts.cmd.enterArg"));
                return;
            }
            String setName = null;
            boolean setDefault = false;
            if (executeAction.inArg(0, "mob")) {
                setName = "mob_attack";
            }
            else if (executeAction.inArg(0, "sb")) {
                setName = "scoreboard";
                setDefault = true;
            }
            else if (executeAction.inArg(0, "inv")) {
                setName = "auto_inv_pick";
                setDefault = true;
            }
            else if (executeAction.inArg(0, "pflyinfo")) {
                setName = "pflyinfo";
                setDefault = true;
            }
            final String playerName = executeAction.getPlayerName();
            if (setName == null) {
                executeAction.sendMessage(text.t("asts.cmd.unknownArg"));
                return;
            }
            final String set = setName + "." + playerName;
            boolean setValue;
            if (executeAction.inArg(1, "true", "false")) {
                setValue = executeAction.inArg(1, "true");
            } else if (executeAction.inArg(1, "toggle")) {
                setValue = !userConfig.config.getBoolean(set, setDefault);
            } else {
                executeAction.sendMessage(text.t("asts.cmd.unknownArg"));
                return;
            }
            userConfig.config.set(set, setValue);
            userConfig.save();
            executeAction.sendMessage(text.t("asts.cmd.asts.set", text.t("asts.cmd.asts." + toCamelCase(setName)), text.t("asts.turnO" + (setValue ? "n" : "ff"))), true);
            if (executeAction.inArg(0, "sb")) {
                executeAction.dispatchCommand("asb", "toggle", setValue ? "on" : "off");
            }
            return;
        }
        else if (executeAction.inArg(0, "admin")) {
            if (!executeAction.hasPermission(PluginPermission.admin)) {
                executeAction.sendMessage(text.t("asts.cmd.noPermission"));
                return;
            }
            else if (args.length == 1) {
                executeAction.sendMessage(text.t("asts.cmd.enterArg"));
                return;
            }
            else if (args[1].equals("reload")) {
                boolean save = false;
                if (args.length == 3 && args[2].equals("save")) {
                    save = true;
                }
                else if (args.length != 2) {
                    executeAction.sendMessage(text.t("asts.cmd.unknownArg"));
                    return;
                }
                config = plugin.getConfig();
                config.options().copyDefaults(true);
                plugin.saveConfig();
                userConfig.reload(save);
                menu.reload();
                executeAction.sendMessage(text.l("§a重載設定檔成功"));
                return;
            }
            else if (args[1].equals("pfly") && args.length >= 4 && args[2].equals("cost")) {
                try {
                    final double amount = Double.parseDouble(args[3]);
                    if (amount < 0) {
                        throw new NumberFormatException();
                    }
                    pfly.setCost(amount);
                    executeAction.sendMessage(text.t("asts.cmd.asts.pfly.cost.set", text.l(Pfly.formatMoney(amount))));
                    return;
                } catch (NumberFormatException e) {
                    executeAction.sendMessage(text.t("asts.cmd.asts.pfly.cost.invalid"));
                    return;
                }
            }
        }
        executeAction.sendMessage(text.t("asts.cmd.unknownArg"));
    }
    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        List<String> arg0 = List.of("mob", "sb", "inv", "pflyinfo");
        if (stack.getSender().hasPermission(PluginPermission.admin)) {
            arg0 = List.of("mob", "sb", "inv", "pflyinfo", "admin");
        }
        if (args.length == 0) {
            return arg0;
        }
        if (args[0].equals("mob") || args[0].equals("sb") || args[0].equals("inv") || args[0].equals("pflyinfo")) {
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
            List<String> arg1 = List.of("reload", "pfly");
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
            else if (args[1].equals("pfly")) {
                if (args.length == 2) {
                    return List.of("cost");
                }
                else if (args[2].equals("cost")) {
                    return List.of();
                }
            }
        }
        else if (args.length == 1) {
            return arg0.stream().filter(a -> a.startsWith(args[0])).toList();
        }
        return List.of();
    }

    @Override
    public String permission() {
        return PluginPermission.commandAsts;
    }
    private @NotNull String toCamelCase(@NotNull String str) {
        StringBuilder result = new StringBuilder();
        for (String s : str.split("_")) {
            if (s.isEmpty()) continue; // 跳過空字符串
            if (result.isEmpty()) {
                result.append(s.toLowerCase());
            } else {
                result.append(s.substring(0, 1).toUpperCase());
                if (s.length() > 1) {
                    result.append(s.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }
}
