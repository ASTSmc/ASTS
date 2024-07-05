package tw.asts.mc.asts.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTargetEvent;
import tw.asts.mc.asts.util.UserConfig;

import java.util.List;

public class MobAttack {

    private final UserConfig userConfig;

    public MobAttack(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    public void onMobAttack(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if (List.of(EntityType.AXOLOTL, EntityType.STRIDER, EntityType.SNOW_GOLEM, EntityType.IRON_GOLEM, EntityType.VILLAGER, EntityType.FROG).contains(entity.getType())) {
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