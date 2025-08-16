package tw.asts.mc.asts.command.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.action.CommandExecuteAction;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
final public class Bedrock extends Base {
    final private SimpleForm.Builder form = SimpleForm.builder();
    public Bedrock(@NotNull CommandExecuteAction executeAction, YamlConfiguration configMenu) {
        super(executeAction, configMenu);
        form.title("§6[§4ASTS§6]§9伺服器§5選單");
        final StringBuilder name = new StringBuilder("§6選單");
        // 顯示上層
        for (int i = 0; i < executeAction.getArgs().length; i++) {
            final MenuParser menuTopList = new MenuParser(Objects.requireNonNull(executeAction.getPlayer()), configMenu, Arrays.copyOfRange(executeAction.getArgs(), 0, i));
            final List<MenuItem> menuTopItems = menuTopList.getItems();
            if (menuTopItems == null) continue;
            final String menuTopCmd = "menu " + String.join(" ", Arrays.copyOfRange(executeAction.getArgs(), 0, i));
            final MenuItem menuTopItem = menuTopItems.stream().filter((mI) -> menuTopCmd.equals(mI.getCommand())).findFirst().orElse(null);
            if (menuTopItem == null) continue;
            name.append(">").append(menuTopItem.getName());
        }
        form.content(executeAction.getArgs().length == 0 ? "請選擇類別" : name.toString());
        final MenuParser menuParser = new MenuParser(Objects.requireNonNull(executeAction.getPlayer()), configMenu, executeAction.getArgs());
        final List<MenuItem> menuItems = menuParser.getItems();
        if (menuItems != null) {
            for (MenuItem menuItem : menuItems) {
                String command = menuItem.getCommand();
                form.button(menuItem.getName() + (command != null ? "\n/" + command : ""));
            }
        }
        if (executeAction.getArgs().length > 0) {
            form.button("返回上層選單");
            if (executeAction.getArgs().length > 1) {
                form.button("返回主選單");
            }
        }
        form.validResultHandler((response) -> {
            if (response == null) return;
            List<String> command = Arrays.stream(Arrays.stream(response.clickedButton().text().split("\n")).toList().getLast().split(" ")).toList();
            String commandName = Arrays.stream(Arrays.stream(response.clickedButton().text().split("\n")).toList().getLast().split(" ")).toList().getFirst();
            List<String> commandArgs = command.subList(1, command.size());
            if (executeAction.hasCommand(commandName.substring(1))) {
                executeAction.dispatchCommand(commandName, commandArgs);
            }
            else if (commandName.equals("返回主選單")) {
                executeAction.dispatchCommand("menu");
            }
            else if (commandName.equals("返回上層選單")) {
                executeAction.dispatchCommand("menu", Arrays.copyOfRange(executeAction.getArgs(), 0, executeAction.getArgs().length - 1));
            }
        });
    }

    @Override
    public @NotNull Boolean open() {
        try {
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi.isFloodgatePlayer(Objects.requireNonNull(executeAction.getPlayer()).getUniqueId())) {
                floodgateApi.sendForm(Objects.requireNonNull(executeAction.getPlayer()).getUniqueId(), form.build());
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.warn(e);
            return false;
        }
    }
}
