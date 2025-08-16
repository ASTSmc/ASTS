package tw.asts.mc.asts.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public final class text {
    public static @NotNull Component m(@NotNull String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
    public static @NotNull Component l(@NotNull String message) {
        return m(legacy(message));
    }
    public static @NotNull Component o(@NotNull Component... components) {
        return Component.empty().append(components);
    }
    public static @NotNull Component nl() {
        return Component.newline();
    }
    public static @NotNull String legacy(@NotNull String message) {
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
    public static @NotNull Component prefix(@NotNull Component type) {
        return t("asts.prefix", t("asts.server"), type);
    }
    public static @NotNull Component t(@NotNull String keyId) {
        return Component.translatable(keyId);
    }
    public static @NotNull Component t(@NotNull String keyId, @NotNull Component... args) {
        return Component.translatable(keyId, args);
    }
}
