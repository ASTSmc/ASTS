package tw.asts.mc.asts.command;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.Log;

/**
 * Vault Economy 適配器（選用依賴）。
 *
 * <p>透過 {@code ExternalClass} 在 Vault 存在時才載入本類別，避免硬依賴。
 * 若 Vault 存在但沒有可用的 Economy，則 {@link #isEnabled()} 回傳 {@code false}。
 */
public final class PflyVault {

    private static PflyVault instance;

    private final Economy economy;

    public PflyVault(@NotNull Plugin plugin) {
        Economy economy = null;
        final RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        this.economy = economy;
        instance = this;
        Log.info(economy == null ? "已載入 Vault，但無可用的 Economy" : "已載入 Vault Economy");
    }

    public static @Nullable PflyVault get() {
        return instance;
    }

    public boolean isEnabled() {
        return economy != null;
    }

    public double getBalance(@NotNull OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }
}
