package tw.asts.mc.asts.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@SuppressWarnings("unused")
final public class Placeholder extends PlaceholderExpansion {

    private final Plugin plugin;
    private final UserConfig userConfig;
    private String bedrockVersion = null;
    private final ExternalClass geyser;
    private String getBedrockVersion() {
        if (bedrockVersion == null) {
            bedrockVersion = (String) geyser.runMethod("getLastSupportedVersion");
        }
        return bedrockVersion;
    }

    public Placeholder(Plugin plugin, UserConfig userConfig) {
        Log.get().log(Level.INFO, "正在註冊PlaceholderAPI");
        this.plugin = plugin;
        this.userConfig = userConfig;
        geyser = ExternalClass.plugin(plugin, "Geyser-Spigot", "tw.asts.mc.asts.util.placeholder.Geyser");
    }

    @Override
    public @NotNull String getIdentifier() {
        return "asts";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream().reduce((a, b) -> a + ", " + b).orElse("None");
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; //
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.equals("name")) {
            return BasicConfig.name;
        } else if (identifier.startsWith("user_")) {
            String username = player.getName();
            if (identifier.equals("user_mob_attack")) {
                return userConfig.config.getBoolean("mob_attack." + username) ? "§a開啟§r" : "§c關閉§r";
            } else if (identifier.equals("user_scoreboard")) {
                if (!userConfig.config.contains("scoreboard." + username)) {
                    return "§a已開啟§r";
                }
                return userConfig.config.getBoolean("scoreboard." + username) ? "§a開啟§r" : "§c關閉§r";
            } else if (identifier.equals("user_auto_inv_pick")) {
                if (!userConfig.config.contains("auto_inv_pick." + username)) {
                    return "§a背包§r";
                }
                return userConfig.config.getBoolean("auto_inv_pick." + username) ? "§a背包§r" : "§c掉落§r";
            }
        } else if (identifier.equals("bedrock")) {
            return getBedrockVersion();
        }
        return null;
    }
}
