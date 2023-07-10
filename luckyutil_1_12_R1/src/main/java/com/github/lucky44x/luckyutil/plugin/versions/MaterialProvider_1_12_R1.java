package com.github.lucky44x.luckyutil.plugin.versions;


import com.github.lucky44x.luckyutil.plugin.abstraction.SpecialItemProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialProvider_1_12_R1 extends SpecialItemProvider {
    @Override
    public ItemStack getItemFromMaterial(String materialName) {
        Material endMaterial = null;

        byte colorShort = -2;
        String[] args = materialName.toUpperCase().split("_");

        if (materialName.toUpperCase().contains("BED")) endMaterial = Material.BED;
        else if (materialName.contains("BANNER")) endMaterial = Material.BANNER;
        else if (materialName.contains("CONCRETE_POWDER")) endMaterial = Material.CONCRETE_POWDER;
        else if (materialName.contains("CONCRETE")) endMaterial = Material.CONCRETE;
        else if (materialName.contains("CARPET")) endMaterial = Material.CARPET;
        else if (materialName.toUpperCase().contains("STAINED_GLASS")) endMaterial = Material.STAINED_GLASS;
        else if (materialName.toUpperCase().contains("STAINED_GLASS_PANE")) endMaterial = Material.STAINED_GLASS_PANE;
        else if (materialName.toUpperCase().contains("TERRACOTTA")) endMaterial = Material.STAINED_CLAY;
        else if (materialName.contains("WOOL")) endMaterial = Material.WOOL;

        colorShort = switch (args[0].toUpperCase()) {
            case ("BLACK") -> 15;
            case ("BLUE") -> 11;
            case ("BROWN") -> 12;
            case ("CYAN") -> 9;
            case ("GRAY") -> 7;
            case ("GREEN") -> 13;
            case ("LIGHT") -> -2;
            case ("LIME") -> 5;
            case ("MAGENTA") -> 2;
            case ("ORANGE") -> 1;
            case ("PINK") -> 6;
            case ("PURPLE") -> 10;
            case ("RED") -> 14;
            case ("WHITE") -> 0;
            case ("YELLOW") -> 4;
            default -> -1;};

        if (colorShort == -2) {
            colorShort = switch (args[1]) {
                case ("GRAY") -> 8;
                case ("BLUE") -> 3;
                default -> -1;};
        }

        colorShort = switch (colorShort == 8 || colorShort == 3 ? args[2] : args[1]) {
            case ("MUSHROOM"), ("GLAZED"), ("SHULKER"), ("NETHER"), ("SANDSTONE") -> -1;
            case ("BANNER") -> transformBannerColor(colorShort);
            default -> colorShort;};

        if (colorShort == -2 || colorShort == -1) {
            endMaterial = Material.getMaterial(materialName.toUpperCase());
            colorShort = 0;
        }

        return new ItemStack(endMaterial, 1, (short) colorShort);
    }

    public byte transformBannerColor(byte input) {

        if (input < 0) return input;

        byte newByte = (byte) (15 - input);
        if (newByte < 0) return -2;

        return newByte;
    }
}
