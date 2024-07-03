package tw.asts.mc.asts.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class UserConfig {

    public YamlConfiguration config;
    private final File file;

    public UserConfig(Plugin plugin) {
        plugin.saveResource("user_config.yml", false);
        file = new File(plugin.getDataFolder(), "user_config.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }
    public void reload(boolean saveFile) {
        if (saveFile) {
            save();
        }
        try {
            config = YamlConfiguration.loadConfiguration(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
