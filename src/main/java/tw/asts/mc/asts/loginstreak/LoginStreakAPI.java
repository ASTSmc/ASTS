package tw.asts.mc.asts.loginstreak;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.Log;

import java.util.UUID;

/**
 * 供其他插件使用的連續登入 API。
 *
 * <p>注意：{@link #getStreak} 回傳的日數若「今日已被列入連續登入」，
 * 會依列入前的數值計算。
 */
public final class LoginStreakAPI {

    private LoginStreakAPI() {
    }

    private static LoginStreak require() {
        final LoginStreak loginStreak = LoginStreak.get();
        if (loginStreak == null) {
            Log.warn("連續登入系統尚未初始化", null);
        }
        return loginStreak;
    }

    public static int getStreak(@NotNull Player player) {
        return getStreak(player.getUniqueId());
    }

    public static int getStreak(@NotNull UUID uuid) {
        final LoginStreak loginStreak = require();
        return loginStreak == null ? 0 : loginStreak.getStreak(uuid);
    }

    public static double getTodayMinutes(@NotNull Player player) {
        return getTodayMinutes(player.getUniqueId());
    }

    public static double getTodayMinutes(@NotNull UUID uuid) {
        final LoginStreak loginStreak = require();
        return loginStreak == null ? 0d : loginStreak.getTodayMinutes(uuid);
    }

    public static boolean hasQualifiedToday(@NotNull Player player) {
        return hasQualifiedToday(player.getUniqueId());
    }

    public static boolean hasQualifiedToday(@NotNull UUID uuid) {
        final LoginStreak loginStreak = require();
        return loginStreak != null && loginStreak.hasQualifiedToday(uuid);
    }
}
