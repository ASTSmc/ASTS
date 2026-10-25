package tw.asts.mc.asts.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.loginstreak.LoginStreakAPI;
import tw.asts.mc.asts.util.ExternalClass;
import tw.asts.mc.asts.util.Log;
import tw.asts.mc.asts.util.UserConfig;
import tw.asts.mc.asts.util.text;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家飛行（/pfly）。
 *
 * <ul>
 *   <li>啟用後可像創造模式一樣飛行（setAllowFlight），僅在真正飛行（isFlying）時每秒扣款。</li>
 *   <li>掉落傷害改為收取每顆心費用（四捨五入至小數點後第二位）。</li>
 *   <li>飛行期間以 actionbar 顯示飛行時長與已扣/剩餘金額。</li>
 *   <li>剩餘可飛行時間低於特定閾值時，以訊息與 title 提醒。</li>
 * </ul>
 */
public final class Pfly implements Listener {

    private static Pfly instance;

    /** 剩餘時間提醒閾值（秒），由大到小。 */
    private static final long[] REMIND_THRESHOLDS = {3600, 1800, 600, 60, 30, 15, 10, 5, 3, 2, 1};

    private final Plugin plugin;
    private final FileConfiguration config;
    private final UserConfig userConfig;

    private double cost;
    private final double discount;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private BukkitRunnable task;

    public Pfly(@NotNull Plugin plugin, @NotNull FileConfiguration config, @NotNull UserConfig userConfig) {
        this.plugin = plugin;
        this.config = config;
        this.userConfig = userConfig;
        this.cost = Math.max(0, config.getDouble("pfly.cost", 200.0));
        this.discount = Math.max(0, config.getDouble("pfly.discount", 0.9));
        ExternalClass.plugin(plugin, "Vault", "tw.asts.mc.asts.command.PflyVault", List.of(Plugin.class), List.of(plugin));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startTask();
        instance = this;
        Log.info("已載入玩家飛行系統（每秒費用=" + formatMoney(cost) + (hasFee() ? "，已連接 Economy" : "，無 Economy（免費飛行）") + "）");
    }

    // ---------- 對外 API ----------

    /** /pfly：啟用或關閉。 */
    public void toggle(@NotNull Player player) {
        final Session session = getSession(player);
        if (session.enabled) {
            endSession(player, session, true);
            return;
        }
        if (hasFee() && !hasAccepted(player)) {
            sendAcceptPrompt(player);
            return;
        }
        enable(player, session);
    }

    /** /pfly accept：同意費用並啟用。 */
    public void accept(@NotNull Player player) {
        final Session session = getSession(player);
        if (session.enabled) {
            return;
        }
        if (hasFee()) {
            setAccepted(player);
        }
        enable(player, session);
    }

    /** 調整每秒飛行費用，並清除所有玩家的同意紀錄。 */
    public void setCost(double amount) {
        this.cost = Math.max(0, amount);
        config.set("pfly.cost", this.cost);
        plugin.saveConfig();
        clearAccepts();
    }

    public double getCost() {
        return cost;
    }

    /** 依玩家連續登入天數打折後的每秒費用：cost × (n ^ streak)。 */
    public double getCost(@NotNull Player player) {
        if (cost <= 0) {
            return cost;
        }
        final int streak = Math.max(0, LoginStreakAPI.getStreak(player));
        return round2(cost * Math.pow(discount, streak));
    }

    public boolean isActionbarEnabled(@NotNull Player player) {
        return userConfig.config.getBoolean("pflyinfo." + player.getName(), true);
    }

    /** 插件停用時結算所有飛行中玩家。 */
    public void disable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            final Session session = sessions.remove(player.getUniqueId());
            if (session != null && session.enabled) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    public static @Nullable Pfly get() {
        return instance;
    }

    /** 四捨五入至小數點後第二位，並去除多餘的零（供顯示）。 */
    public static String formatMoney(double value) {
        final double rounded = round2(value);
        if (rounded == (long) rounded) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }

    // ---------- 內部 ----------

    private void startTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        task.runTaskTimer(plugin, 10L, 10L);
    }

    private void tick() {
        final long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            final Session session = sessions.get(player.getUniqueId());
            if (session == null || !session.enabled) {
                continue;
            }
            // 被其他插件關閉飛行（allowFlight 被設為 false）時，結束 pfly
            if (!player.getAllowFlight()) {
                endSession(player, session, true);
                continue;
            }
            final boolean flying = player.isFlying();

            // 累計真正飛行時間
            if (flying && session.lastTickMillis > 0) {
                session.totalFlyMillis += now - session.lastTickMillis;
            }
            session.lastTickMillis = now;

            // 每秒扣款（僅飛行中；首秒預先扣款，避免多飛一秒）
            if (flying) {
                if (session.lastChargeMillis == 0) {
                    session.lastChargeMillis = now - 1000;
                }
                while (now - session.lastChargeMillis >= 1000) {
                    session.lastChargeMillis += 1000;
                    if (!chargeOneSecond(player, session)) {
                        break; // 餘額不足，已取消 pfly
                    }
                }
            } else {
                session.lastChargeMillis = 0;
            }

            if (!session.enabled) {
                continue; // 剛剛被取消
            }

            updateActionbar(player, session, flying);
            checkReminders(player, session, false);
        }
    }

    private boolean chargeOneSecond(@NotNull Player player, @NotNull Session session) {
        final PflyVault vault = PflyVault.get();
        if (vault == null || !vault.isEnabled() || getCost() <= 0) {
            return true;
        }
        if (!vault.has(player, getCost(player)) || !vault.withdraw(player, getCost(player))) {
            endSession(player, session, false);
            return false;
        }
        session.totalCharged += getCost(player);
        return true;
    }

    private void enable(@NotNull Player player, @NotNull Session session) {
        final PflyVault vault = PflyVault.get();
        if (vault != null && vault.isEnabled() && getCost() > 0 && !vault.has(player, getCost(player))) {
            player.sendMessage(text.prefix(text.t("asts.cmd.pfly.insufficient.enable")));
            return;
        }
        session.enabled = true;
        session.totalFlyMillis = 0;
        session.totalCharged = 0;
        session.lastChargeMillis = 0;
        session.lastTickMillis = System.currentTimeMillis();
        session.falling = false;
        session.peakFallDistance = 0;
        session.reminded.clear();
        player.setAllowFlight(true);
        player.sendMessage(text.prefix(hasFee()
                ? text.t("asts.cmd.pfly.enable.fee", text.l(formatMoney(getCost(player))))
                : text.t("asts.cmd.pfly.enable")));
        checkReminders(player, session, true);
    }

    private void endSession(@NotNull Player player, @NotNull Session session, boolean manual) {
        player.setFlying(false);
        player.setAllowFlight(false);
        session.enabled = false;
        session.falling = false;
        sessions.remove(player.getUniqueId());
        if (manual) {
            player.sendMessage(text.prefix(text.t("asts.cmd.pfly.disable")));
            player.sendMessage(text.t("asts.cmd.pfly.disable.duration", formatDuration(session.totalFlyMillis)));
            player.sendMessage(text.t("asts.cmd.pfly.disable.charged", text.l(formatMoney(session.totalCharged))));
        } else {
            player.sendMessage(text.prefix(text.t("asts.cmd.pfly.insufficient")));
        }
    }

    private void updateActionbar(@NotNull Player player, @NotNull Session session, boolean flying) {
        if (!isActionbarEnabled(player)) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (!flying && now - session.lastActionbarMillis < 10000) {
            return; // 非飛行時每 10 秒一次
        }
        session.lastActionbarMillis = now;
        final PflyVault vault = PflyVault.get();
        final String remaining = (vault != null && vault.isEnabled()) ? formatMoney(vault.getBalance(player)) : "∞";
        player.sendActionBar(text.t("asts.cmd.pfly.actionbar",
                formatDuration(session.totalFlyMillis),
                text.l(formatMoney(session.totalCharged)),
                text.l(remaining)));
    }

    private void checkReminders(@NotNull Player player, @NotNull Session session, boolean initial) {
        final PflyVault vault = PflyVault.get();
        if (vault == null || !vault.isEnabled() || getCost() <= 0) {
            return;
        }
        final double remaining = vault.getBalance(player) / getCost(player);
        final long remainingSeconds = (long) Math.ceil(remaining);
        if (initial) {
            // 啟用時：標記目前已低於的閾值，只提醒一次實際剩餘時間
            boolean shouldRemind = false;
            for (long threshold : REMIND_THRESHOLDS) {
                if (remaining < threshold) {
                    session.reminded.add(threshold);
                    shouldRemind = true;
                }
            }
            if (shouldRemind) {
                remind(player, remainingSeconds);
            }
            return;
        }
        for (long threshold : REMIND_THRESHOLDS) {
            if (remaining < threshold && session.reminded.add(threshold)) {
                remind(player, remainingSeconds);
            }
        }
    }

    private void remind(@NotNull Player player, long remainingSeconds) {
        final Component label = thresholdLabel(remainingSeconds);
        player.sendMessage(text.prefix(text.t("asts.cmd.pfly.remind", label)));
        player.showTitle(Title.title(text.t("asts.cmd.pfly.remind.title"), text.t("asts.cmd.pfly.remind.subtitle", label)));
    }

    private void sendAcceptPrompt(@NotNull Player player) {
        final Component agree = text.t("asts.cmd.pfly.accept.agree")
                .clickEvent(ClickEvent.runCommand("/pfly accept"));
        player.sendMessage(text.o(
                text.t("asts.cmd.pfly.accept.header"),
                text.nl(),
                text.t("asts.cmd.pfly.accept.cost", text.l(formatMoney(getCost()))),
                text.nl(),
                text.t("asts.cmd.pfly.accept.fall", text.l(formatMoney(getCost()))),
                text.nl(),
                text.t("asts.cmd.pfly.accept.discount", text.l(formatMoney(discount))),
                text.nl(),
                text.t("asts.cmd.pfly.accept.insufficient"),
                text.nl(),
                agree,
                text.t("asts.cmd.pfly.accept.hint")
        ));
    }

    private boolean hasFee() {
        final PflyVault vault = PflyVault.get();
        return vault != null && vault.isEnabled() && getCost() > 0;
    }

    private boolean hasAccepted(@NotNull Player player) {
        return userConfig.config.getBoolean("pfly_accept." + player.getName(), false);
    }

    private void setAccepted(@NotNull Player player) {
        userConfig.config.set("pfly_accept." + player.getName(), true);
        userConfig.save();
    }

    private void clearAccepts() {
        userConfig.config.set("pfly_accept", null);
        userConfig.save();
    }

    private Session getSession(@NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        Session session = sessions.get(uuid);
        if (session == null) {
            session = new Session();
            sessions.put(uuid, session);
        }
        return session;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Session session = sessions.get(player.getUniqueId());
        if (session == null || !session.enabled) {
            return;
        }
        if (player.isFlying()) {
            // 重新飛行：重置掉落，不計費
            session.falling = false;
            session.peakFallDistance = 0;
            return;
        }
        final float fallDistance = player.getFallDistance();
        if (fallDistance > 0) {
            session.falling = true;
            session.peakFallDistance = Math.max(session.peakFallDistance, fallDistance);
        } else if (session.falling) {
            session.falling = false;
            if (!player.isInWater() && !player.isInLava()) {
                chargeFallDamage(player, session);
            }
            session.peakFallDistance = 0;
        }
    }

    private void chargeFallDamage(@NotNull Player player, @NotNull Session session) {
        // 緩降藥水：不受掉落傷害，不計費
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return;
        }
        // 落地減傷：依落地方塊打折（原版在 checkFallDamage 先乘上倍率）
        final Material landedType = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        if (landedType == Material.SLIME_BLOCK && !player.isSneaking()) {
            return; // 史萊姆方塊：彈跳，免傷
        }
        double fallDistance = session.peakFallDistance;
        if (landedType == Material.HAY_BLOCK || landedType == Material.HONEY_BLOCK) {
            fallDistance *= 0.2; // 減 80%
        } else if (Tag.BEDS.isTagged(landedType) || landedType == Material.POWDER_SNOW) {
            fallDistance *= 0.5; // 減 50%
        }
        // 輕盈附魔：每級減少 1 格掉落距離
        final ItemStack boots = player.getInventory().getBoots();
        fallDistance -= boots.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
        // 跳躍藥水：每級減少 1 HP 掉落傷害
        final PotionEffect jumpBoost = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        final int jumpBoostLevel = jumpBoost == null ? 0 : jumpBoost.getAmplifier() + 1;
        final double damageHp = Math.max(0, Math.ceil(fallDistance - 3.0) - jumpBoostLevel);
        final double hearts = damageHp / 2.0;
        if (hearts <= 0) {
            return;
        }
        final PflyVault vault = PflyVault.get();
        if (vault == null || !vault.isEnabled() || getCost() <= 0) {
            return;
        }
        final double charge = round2(hearts * getCost(player));
        if (charge <= 0) {
            return;
        }
        if (!vault.withdraw(player, charge)) {
            endSession(player, session, false);
            return;
        }
        session.totalCharged += charge;
        // 扣款後若餘額不足以支付下一秒，關閉飛行
        if (!vault.has(player, getCost(player))) {
            endSession(player, session, false);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    // ---------- 工具 ----------

    private static Component formatDuration(long millis) {
        final long totalSeconds = millis / 1000;
        final long hours = totalSeconds / 3600;
        final long minutes = (totalSeconds % 3600) / 60;
        final long seconds = totalSeconds % 60;
        if (hours > 0) {
            return text.t("asts.cmd.pfly.duration.full", text.l(String.valueOf(hours)), text.l(String.valueOf(minutes)), text.l(String.valueOf(seconds)));
        }
        if (minutes > 0) {
            return text.t("asts.cmd.pfly.duration.min", text.l(String.valueOf(minutes)), text.l(String.valueOf(seconds)));
        }
        return text.t("asts.cmd.pfly.duration.sec", text.l(String.valueOf(seconds)));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static Component thresholdLabel(long seconds) {
        if (seconds >= 3600) {
            return text.t("asts.cmd.pfly.time.hour", text.l(String.valueOf(seconds / 3600)));
        }
        if (seconds >= 60) {
            return text.t("asts.cmd.pfly.time.minute", text.l(String.valueOf(seconds / 60)));
        }
        return text.t("asts.cmd.pfly.time.second", text.l(String.valueOf(seconds)));
    }

    private static final class Session {
        boolean enabled;
        long totalFlyMillis;
        double totalCharged;
        long lastChargeMillis;
        long lastTickMillis;
        boolean falling;
        double peakFallDistance;
        long lastActionbarMillis;
        final Set<Long> reminded = new HashSet<>();
    }
}
