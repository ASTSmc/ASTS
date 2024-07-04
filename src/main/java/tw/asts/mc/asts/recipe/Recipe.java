package tw.asts.mc.asts.recipe;

import org.bukkit.plugin.Plugin;

public class Recipe {
    public Recipe(Plugin plugin) {
        // 材料染料顏色轉換
        new DyeSwitch(plugin, "concrete_powder");
        new DyeSwitch(plugin, "concrete");
        new DyeSwitch(plugin, "wool");
        new DyeSwitch(plugin, "carpet");
        new DyeSwitch(plugin, "glazed_terracotta");
        new DyeSwitch(plugin, "bed");
        new DyeSwitch(plugin, "banner");
        new DyeSwitch(plugin, "stained_glass");
        new DyeSwitch(plugin, "stained_glass_pane");
        // 熔爐配方
        new Furnace(plugin);
    }
}
