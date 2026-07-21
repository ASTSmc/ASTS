package tw.asts.mc.asts.recipe;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

final public class Totem {
  public Totem(Plugin plugin) {
      NamespacedKey key = new NamespacedKey(plugin, "totem_map");
      ItemStack item = ItemStack.of(Material.FILLED_MAP);
      item.setData(DataComponentTypes.ITEM_NAME, Component.translatable("item.minecraft.totem_of_undying"));
      item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
      final ShapelessRecipe recipe = new ShapelessRecipe(key, item);
      recipe.addIngredient(1, Material.FILLED_MAP);
      recipe.addIngredient(1, Material.TOTEM_OF_UNDYING);
      recipe.addIngredient(1, Material.PAPER);
      plugin.getServer().addRecipe(recipe);
  }
}
