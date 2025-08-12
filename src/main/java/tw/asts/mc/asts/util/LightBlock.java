package tw.asts.mc.asts.util;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

final public class LightBlock implements Listener {
    public LightBlock(@NotNull Plugin plugin) {
        Log.get().log(Level.INFO, "正在註冊光源使用區域限制事件");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerUse(@NotNull PlayerInteractEvent event){
        if (event.getMaterial() != Material.LIGHT || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Location loc = event.getInteractionPoint();
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        Player player = event.getPlayer();
        if (player.hasPermission(PluginPermission.admin())) {
            return;
        }
        if (res != null) {
            ResidencePermissions perms = res.getPermissions();
            if (perms.playerHas(player, Flags.build, true)) {
                return;
            }
        }
        event.setCancelled(true);
        player.sendMessage(text.miniMessageComponent(text.miniMessage(BasicConfig.prefix("§c你沒有權限在此區域放置光源"))));
    }
}
