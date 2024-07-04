package tw.asts.mc.asts.recipe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DyeSwitch {
    public DyeSwitch(Plugin plugin, String itemName) {
        List<String> colors = List.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");
        for (String color : colors) {
            Material colorDye = Material.getMaterial((color + "_dye").toUpperCase());
            Material colorMaterial = Material.getMaterial((color + "_" + itemName).toUpperCase());
            if (colorDye == null || colorMaterial == null) {
                continue;
            }
            List<String> otherColors = colors.stream().filter(c -> !c.equals(color)).toList();
            List<Material> otherMaterial = otherColors.stream().map(c -> Material.getMaterial((c + "_" + itemName).toUpperCase())).filter(m -> m != null).toList();
            for (int i = 1; i < 9; i++) {
                int finalI = i;
                ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "dye-switch_" + color + "_" + itemName + "_" + finalI), new ItemStack(colorMaterial, finalI));
                recipe.addIngredient(1, colorDye);
                for (int j = 1; j <= finalI; j++) {
                    recipe.addIngredient(new RecipeChoice.MaterialChoice(otherMaterial));
                }
                plugin.getServer().addRecipe(recipe);
            }
        }
    }
}
