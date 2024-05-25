package tw.asts.mc.asts;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

final class config {
    public static final String name = "§6[§4ASTS§6]§eAllen§a跨平台§b插件§f伺服器§r";
    public static String prefix(String type) {
        return name + " §e-§r §6" + type + "§r\n";
    }
}

public final class ASTS extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("wtp", "隨機傳送", List.of("worldtp", "wteleport", "worldteleport"), new Wtp());
        });
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Shutting Down Plugin");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(TextComponent.fromLegacyText(config.name + " - 歡迎來到" + config.name + "！"));
    }

}