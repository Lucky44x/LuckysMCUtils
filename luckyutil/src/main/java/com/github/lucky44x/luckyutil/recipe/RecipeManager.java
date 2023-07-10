package com.github.lucky44x.luckyutil.recipe;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

@SuppressWarnings("rawtypes")
public class RecipeManager {
    private final HashMap<String, ItemStack> previousRecipes = new HashMap<>();
    private static final HashMap<String, MerchantRecipe> merchantRecipes = new HashMap<>();

    public Recipe loadRecipeFromJson(JsonObject object, ItemStack result, NamespacedKey key, boolean loadToCache) {
        if (loadToCache) previousRecipes.put(key.getKey(), result);

        final String type = object.get("type").getAsString();
        switch (type) {
            case ("blasting") -> {
                return readCookingRecipe(BlastingRecipe.class, object, result, key);
            }
            case ("campfire") -> {
                return readCookingRecipe(CampfireRecipe.class, object, result, key);
            }
            case ("furnace") -> {
                return readCookingRecipe(FurnaceRecipe.class, object, result, key);
            }
            case ("smoking") -> {
                return readCookingRecipe(SmokingRecipe.class, object, result, key);
            }
            case ("smithing-transform") -> {
                return readSmithingTransformRecipe(object, result, key);
            }
            case ("smithing-trim") -> {
                return readSmithingTrimRecipe(object, key);
            }
            case ("shaped") -> {
                return readShapedRecipe(object, result, key);
            }
            case ("shapeless") -> {
                return readShapelessRecipe(object, result, key);
            }
            case ("stone-cutting") -> {
                return readStonecutterRecipe(object, result, key);
            }
            case ("merchant") -> {
                MerchantRecipe recipe = readMerchantRecipe(object, result);
                merchantRecipes.put(key.getKey(), recipe);
                return recipe;
            }
        }
        return null;
    }

    private StonecuttingRecipe readStonecutterRecipe(JsonObject object, ItemStack result, NamespacedKey key) {
        RecipeChoice input = getRecipeChoice(object.get("input").getAsString());
        if (input == null) return null;

        return new StonecuttingRecipe(key, result, input);
    }

    private ShapedRecipe readShapedRecipe(JsonObject object, ItemStack result, NamespacedKey key) {
        ArrayList<Character> keys = new ArrayList<>();

        String top = object.get("topRow").getAsString();
        String middle = object.get("middle").getAsString();
        String bottom = object.get("bottom").getAsString();
        for (int i = 0; i < 9; i++) {
            char c = 'a';
            if (i > 5) {
                c = bottom.toCharArray()[i - 6];
            } else if (i > 2) {
                c = middle.toCharArray()[i - 3];
            } else {
                c = top.toCharArray()[i];
            }

            if (!keys.contains(c)) keys.add(c);
        }

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(top, middle, bottom);
        for (char c : keys) {
            RecipeChoice choice = getRecipeChoice(object.get(String.valueOf(c)).getAsString());
            if (choice == null) continue;

            recipe.setIngredient(c, choice);
        }

        return recipe;
    }

    private ShapelessRecipe readShapelessRecipe(JsonObject object, ItemStack result, NamespacedKey key) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);

        JsonArray ingredients = object.get("ingredients").getAsJsonArray();
        for (int i = 0; i < ingredients.size(); i++) {
            RecipeChoice choice = getRecipeChoice(ingredients.get(i).getAsString());
            if (choice == null) continue;

            recipe.addIngredient(choice);
        }

        return recipe;
    }

    private MerchantRecipe readMerchantRecipe(JsonObject object, ItemStack result) {
        final int maxUses = object.get("max-uses").getAsInt();

        if (object.has("uses") && object.has("experience-reward")) {
            final int uses = object.get("uses").getAsInt();
            final boolean experience = object.get("experience-reward").getAsBoolean();

            if (object.has("experience") && object.has("price-multiplier")) {
                final int villagerExp = object.get("experience").getAsInt();
                final float priceMult = object.get("price-multiplier").getAsFloat();

                if (object.has("demand") && object.has("special-price")) {
                    final int demand = object.get("demand").getAsInt();
                    final int specialPrice = object.get("special-price").getAsInt();

                    return new MerchantRecipe(
                            result, uses, maxUses, experience, villagerExp, priceMult, demand, specialPrice);
                }
                return new MerchantRecipe(result, uses, maxUses, experience, villagerExp, priceMult);
            }
            return new MerchantRecipe(result, uses, maxUses, experience);
        }
        return new MerchantRecipe(result, maxUses);
    }

    private Recipe readCookingRecipe(
            Class<? extends CookingRecipe> subClass, JsonObject object, ItemStack result, NamespacedKey key) {
        try {
            final float experience = object.get("experience").getAsFloat();
            final int time = object.get("time").getAsInt();
            final String input = object.get("base-item").getAsString();
            RecipeChoice choice = getRecipeChoice(input);

            if (choice == null) return null;

            return subClass.getConstructor(
                            NamespacedKey.class, ItemStack.class, RecipeChoice.class, float.class, int.class)
                    .newInstance(key, result, choice, experience, time);

        } catch (InvocationTargetException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SmithingTransformRecipe readSmithingTransformRecipe(
            JsonObject object, ItemStack result, NamespacedKey key) {
        RecipeChoice template = getRecipeChoice(object.get("template").getAsString());
        RecipeChoice base = getRecipeChoice(object.get("base-item").getAsString());
        RecipeChoice addition = getRecipeChoice(object.get("addition-item").getAsString());

        return new SmithingTransformRecipe(key, result, template, base, addition);
    }

    private SmithingTrimRecipe readSmithingTrimRecipe(JsonObject object, NamespacedKey key) {
        RecipeChoice template = getRecipeChoice(object.get("template").getAsString());
        RecipeChoice base = getRecipeChoice(object.get("input").getAsString());
        RecipeChoice addition = getRecipeChoice(object.get("addition").getAsString());

        return new SmithingTrimRecipe(key, template, base, addition);
    }

    private RecipeChoice getRecipeChoice(String input) {
        RecipeChoice choice = null;
        if (input.contains("|")) {
            List<Material> materials = new ArrayList<>();
            String[] args = input.split("\\|");
            for (String type : args) {
                if (Material.getMaterial(type.toUpperCase()) == null) continue;

                materials.add(Material.getMaterial(type.toUpperCase()));
            }
            choice = new RecipeChoice.MaterialChoice(materials);
        } else {
            if (previousRecipes.containsKey(input)) choice = new RecipeChoice.ExactChoice(previousRecipes.get(input));
            else if (Material.getMaterial(input.toUpperCase()) != null)
                choice = new RecipeChoice.MaterialChoice(Material.getMaterial(input.toUpperCase()));
        }

        return choice;
    }

    public final MerchantRecipe getMerchantRecipe(String key) {
        return merchantRecipes.getOrDefault(key, null);
    }

    public final MerchantRecipe getMerchantRecipe(NamespacedKey key) {
        return getMerchantRecipe(key.getKey());
    }
}
