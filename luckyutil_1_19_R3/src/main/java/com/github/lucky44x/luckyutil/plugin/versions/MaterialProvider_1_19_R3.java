package com.github.lucky44x.luckyutil.plugin.versions;


import com.github.lucky44x.luckyutil.plugin.abstraction.SpecialItemProvider;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialProvider_1_19_R3 extends SpecialItemProvider {
    @Override
    public ItemStack getItemFromMaterial(String materialName) {
        return new ItemStack(Objects.requireNonNull(Material.getMaterial(materialName)));
    }
}
