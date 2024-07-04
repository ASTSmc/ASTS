package tw.asts.mc.asts.recipe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.plugin.Plugin;

public class Furnace {
    public Furnace(Plugin plugin) {
        addRecipe(plugin, Material.RAW_IRON_BLOCK, Material.IRON_BLOCK, 6.3f, 1800, true, true);
        addRecipe(plugin, Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK, 6.3f, 1800, true, true);
        addRecipe(plugin, Material.RAW_GOLD_BLOCK, Material.GOLD_BLOCK, 9.0f, 1800, true, true);
    }
    private void addRecipe(Plugin plugin, Material fromMaterial, Material toMaterial, float experience, int cookingTime, boolean blastFurnace, boolean smoker) {
        FurnaceRecipe recipe = new FurnaceRecipe(new NamespacedKey(plugin, "asts_furnace_"+ fromMaterial.name().toLowerCase() + "_" + toMaterial.name().toLowerCase()), new ItemStack(toMaterial, 1), fromMaterial, experience, cookingTime);
        plugin.getServer().addRecipe(recipe);
        if (blastFurnace) {
            BlastingRecipe blastingRecipe = new BlastingRecipe(new NamespacedKey(plugin, "asts_blast_"+ fromMaterial.name().toLowerCase() + "_" + toMaterial.name().toLowerCase()), new ItemStack(toMaterial, 1), fromMaterial, experience, cookingTime / 2);
            plugin.getServer().addRecipe(blastingRecipe);
        }
        if (smoker) {
            SmokingRecipe smokingRecipe = new SmokingRecipe(new NamespacedKey(plugin, "asts_smoke_"+ fromMaterial.name().toLowerCase() + "_" + toMaterial.name().toLowerCase()), new ItemStack(toMaterial, 1), fromMaterial, experience, cookingTime / 2);
            plugin.getServer().addRecipe(smokingRecipe);
        }
    }
}