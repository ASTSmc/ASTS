package tw.asts.mc.asts.loginstreak;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.Log;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 連續登入計算。
 *
 * <ul>
 *   <li>依伺服器時區，以每日 00:00 為界；不處理玩家跨日，單一上線時段全數計入加入（登入）當天。</li>
 *   <li>當日累計上線達 {@code thresholdMinutes} 分鐘（可分段累計）即列入連續登入。</li>
 *   <li>離線時間不計入上線時長。</li>
 *   <li>玩家資料存於 {@code login_streak.yml}（key 為玩家 UUID）。</li>
 *   <li>若最近一次列入日距今超過 (d+1) 天，連續登入歸零重新起算。</li>
 * </ul>
 */
public final class LoginStreak implements Listener {

    private static LoginStreak instance;

    private final Plugin plugin;
    private final long thresholdMinutes;
    private final long graceDays;
    private final ZoneId zone;

    private final File file;
    private final YamlConfiguration data;

    /** 各玩家目前上線時段的起點（毫秒），僅存在於記憶體。 */
    private final Map<UUID, Long> sessionStarts = new ConcurrentHashMap<>();

    public LoginStreak(@NotNull Plugin plugin, @NotNull FileConfiguration config) {
        this.plugin = plugin;
        this.thresholdMinutes = Math.max(0, config.getLong("login_streak.minutes", 10));
        this.graceDays = Math.max(0, config.getLong("login_streak.grace_days", 1));
        this.zone = parseZone(config.getString("login_streak.timezone"));
        plugin.saveResource("login_streak.yml", false);
        this.file = new File(plugin.getDataFolder(), "login_streak.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        instance = this;
        Log.info("已載入連續登入系統（n=" + thresholdMinutes + " 分鐘，d=" + graceDays + " 天，時區=" + zone + "）");
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        sessionStarts.put(uuid, System.currentTimeMillis());
        final ConfigurationSection section = playerSection(uuid);
        rolloverIfNeeded(section);
        checkQualify(section); // 支援 n=0：登入即達標
        save();
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        accumulate(event.getPlayer().getUniqueId(), System.currentTimeMillis(), true);
        save();
    }

    /** 取得玩家目前的連續登入日數（若今日已列入，回傳列入前的數值）。 */
    public int getStreak(@NotNull UUID uuid) {
        refresh(uuid);
        final ConfigurationSection section = playerSection(uuid);
        final String today = today();
        final String last = readLastQualified(section);
        if (last == null) {
            return 0;
        }
        final long gap = daysBetween(last, today);
        if (gap > graceDays + 1) {
            return 0; // 已超過寬限天數，視為中斷歸零
        }
        if (gap == 0) {
            return (int) Math.max(0, readStreak(section) - 1); // 今日已列入 → 依列入前計算
        }
        return (int) readStreak(section);
    }

    /** 取得玩家今日累計上線分鐘數。 */
    public double getTodayMinutes(@NotNull UUID uuid) {
        refresh(uuid);
        final ConfigurationSection section = playerSection(uuid);
        if (!today().equals(section.getString("today"))) {
            return 0d; // 資料中的時長屬於其他日期
        }
        return readMinutes(section);
    }

    /** 今日是否已列入連續登入。 */
    public boolean hasQualifiedToday(@NotNull UUID uuid) {
        refresh(uuid);
        return today().equals(readLastQualified(playerSection(uuid)));
    }

    /** 結算所有在線玩家的進行中時段（供插件停用時呼叫）。 */
    public void flushAll() {
        final long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            accumulate(player.getUniqueId(), now, true);
        }
        save();
    }

    public void save() {
        try {
            data.save(file);
        } catch (Exception e) {
            Log.warn(e);
        }
    }

    public static @Nullable LoginStreak get() {
        return instance;
    }

    /** 將玩家進行中的上線時段累計入加入當天的時長，並檢查是否達標。 */
    private void accumulate(@NotNull UUID uuid, long now, boolean removeSession) {
        final Long start = sessionStarts.get(uuid);
        if (start == null) {
            return;
        }
        if (removeSession) {
            sessionStarts.remove(uuid);
        } else {
            sessionStarts.put(uuid, now);
        }
        final ConfigurationSection section = playerSection(uuid);
        final long elapsed = now - start;
        if (elapsed > 0) {
            section.set("minutes", readMinutes(section) + elapsed / 60000.0);
        }
        checkQualify(section);
    }

    private void checkQualify(@NotNull ConfigurationSection section) {
        final String day = section.getString("today");
        if (day == null) {
            return;
        }
        if (readMinutes(section) < thresholdMinutes) {
            return;
        }
        final String last = readLastQualified(section);
        if (day.equals(last)) {
            return; // 該日已列入，避免重複計算
        }
        long streak = readStreak(section);
        if (last == null || daysBetween(last, day) > graceDays + 1) {
            streak = 1; // 首次列入，或已中斷超過寬限天數，重新起算
        } else {
            streak += 1; // 連續（或寬限天數內），累加一天
        }
        section.set("streak", streak);
        section.set("last-qualified", day);
    }

    /** 若資料中的「今日」與伺服器今日不同，重置當日時長。 */
    private void rolloverIfNeeded(@NotNull ConfigurationSection section) {
        final String today = today();
        if (!today.equals(section.getString("today"))) {
            section.set("today", today);
            section.set("minutes", 0d);
        }
    }

    /** 在線玩家於查詢時先結算其進行中時段。 */
    private void refresh(@NotNull UUID uuid) {
        final Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline() && sessionStarts.containsKey(uuid)) {
            accumulate(uuid, System.currentTimeMillis(), false);
            save();
        }
    }

    private ConfigurationSection playerSection(@NotNull UUID uuid) {
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) {
            players = data.createSection("players");
        }
        final String id = uuid.toString();
        ConfigurationSection section = players.getConfigurationSection(id);
        if (section == null) {
            section = players.createSection(id);
        }
        return section;
    }

    private String today() {
        return LocalDate.now(zone).toString();
    }

    private static long daysBetween(@NotNull String from, @NotNull String to) {
        return ChronoUnit.DAYS.between(LocalDate.parse(from), LocalDate.parse(to));
    }

    private static long readStreak(@NotNull ConfigurationSection section) {
        return section.getLong("streak", 0L);
    }

    private static @Nullable String readLastQualified(@NotNull ConfigurationSection section) {
        return section.getString("last-qualified");
    }

    private static double readMinutes(@NotNull ConfigurationSection section) {
        return section.getDouble("minutes", 0d);
    }

    private static ZoneId parseZone(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(id.trim());
        } catch (DateTimeException e) {
            Log.warn("login_streak.timezone 無效（" + id + "），已改用系統時區", null);
            return ZoneId.systemDefault();
        }
    }
}
