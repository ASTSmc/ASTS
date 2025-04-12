package tw.asts.mc.asts.event;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;

import tw.asts.mc.asts.util.UserConfig;

public class MobAttack {

    private final UserConfig userConfig;

    public MobAttack(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    public void onMobAttack(@NotNull EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (List.of(EntityType.WOLF, EntityType.AXOLOTL, EntityType.STRIDER, EntityType.SNOW_GOLEM, EntityType.IRON_GOLEM, EntityType.VILLAGER, EntityType.FROG).contains(entity.getType())) {
            return;
        }
        else if (target == null) {
            return;
        }
        else if (List.of(EntityType.ENDERMITE, EntityType.SNOW_GOLEM, EntityType.IRON_GOLEM).contains(target.getType())) {
            return;
        }
        else if (target.getType() == EntityType.PLAYER && userConfig.config.getBoolean("mob_attack." + target.getName())) {
            return;
        }
        event.setCancelled(true);
    }
}