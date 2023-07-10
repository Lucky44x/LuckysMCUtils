package com.github.lucky44x.luckyutil.plugin.abstraction;


import org.bukkit.inventory.ItemStack;

public abstract class SpecialItemProvider {
    public abstract ItemStack getItemFromMaterial(String materialName);
}
