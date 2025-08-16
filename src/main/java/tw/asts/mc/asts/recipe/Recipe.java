package tw.asts.mc.asts.recipe;

import org.bukkit.plugin.Plugin;
import tw.asts.mc.asts.util.Log;

final public class Recipe {
    public Recipe(Plugin plugin) {
        Log.info("正在載入配方");
        // 材料染料顏色轉換
        new DyeSwitch(plugin);
        // 熔爐配方
        new Furnace(plugin);
    }
}
