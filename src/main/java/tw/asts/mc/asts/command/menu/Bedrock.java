package tw.asts.mc.asts.command.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("unused")
final public class Bedrock extends Base {
    final private SimpleForm.Builder form = SimpleForm.builder();
    public Bedrock(@NotNull Player user, @NotNull String[] args, YamlConfiguration configMenu) {
        super(user, args, configMenu);
        form.title("§6[§4ASTS§6]§9伺服器§5選單");
        final StringBuilder name = new StringBuilder("§6選單");
        // 顯示上層
        for (int i = 0; i < args.length; i++) {
            final MenuParser menuTopList = new MenuParser(user, configMenu, Arrays.copyOfRange(args, 0, i));
            final List<MenuItem> menuTopItems = menuTopList.getItems();
            if (menuTopItems == null) continue;
            final String menuTopCmd = "menu " + String.join(" ", Arrays.copyOfRange(args, 0, i));
            final MenuItem menuTopItem = menuTopItems.stream().filter((mI) -> menuTopCmd.equals(mI.getCommand())).findFirst().orElse(null);
            if (menuTopItem == null) continue;
            name.append(">").append(menuTopItem.getName());
        }
        form.content(args.length == 0 ? "請選擇類別" : name.toString());
        final MenuParser menuParser = new MenuParser(user, configMenu, args);
        final List<MenuItem> menuItems = menuParser.getItems();
        if (menuItems != null) {
            for (MenuItem menuItem : menuItems) {
                String command = menuItem.getCommand();
                form.button(menuItem.getName() + (command != null ? "\n/" + command : ""));
            }
        }
        if (args.length > 0) {
            form.button("返回上層選單");
            if (args.length > 1) {
                form.button("返回主選單");
            }
        }
        form.validResultHandler((response) -> {
            if (response == null) return;
            String command = Arrays.stream(response.clickedButton().text().split("\n")).toList().getLast();
            String commandName = Arrays.stream(command.split(" ")).toList().getFirst();
            if (user.getServer().getCommandMap().getCommand(commandName.substring(1)) != null) {
                user.getServer().dispatchCommand(user, command.substring(1));
            }
            else if (commandName.equals("返回主選單")) {
                user.getServer().dispatchCommand(user, "menu");
            }
            else if (commandName.equals("返回上層選單")) {
                user.getServer().dispatchCommand(user, "menu " + String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1)));
            }
        });
    }

    @Override
    public @NotNull Boolean open() {
        try {
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi.isFloodgatePlayer(user.getUniqueId())) {
                floodgateApi.sendForm(user.getUniqueId(), form.build());
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.get().log(Level.WARNING, e.getMessage(), e.getCause());
            return false;
        }
    }
}
