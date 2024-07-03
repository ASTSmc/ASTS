package tw.asts.mc.asts.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class text {
    public static String miniMessage(String message) {
        return message
                .replaceAll("§0", "<black>")
                .replaceAll("§1", "<dark_blue>")
                .replaceAll("§2", "<dark_green>")
                .replaceAll("§3", "<dark_aqua>")
                .replaceAll("§4", "<dark_red>")
                .replaceAll("§5", "<dark_purple>")
                .replaceAll("§6", "<gold>")
                .replaceAll("§7", "<gray>")
                .replaceAll("§8", "<dark_gray>")
                .replaceAll("§9", "<blue>")
                .replaceAll("§a", "<green>")
                .replaceAll("§b", "<aqua>")
                .replaceAll("§c", "<red>")
                .replaceAll("§d", "<light_purple>")
                .replaceAll("§e", "<yellow>")
                .replaceAll("§f", "<white>")
                .replaceAll("§k", "<obfuscated>")
                .replaceAll("§l", "<bold>")
                .replaceAll("§m", "<strikethrough>")
                .replaceAll("§n", "<underline>")
                .replaceAll("§o", "<italic>")
                .replaceAll("§r", "<reset>");
    }
    public static Component miniMessageComponent(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
