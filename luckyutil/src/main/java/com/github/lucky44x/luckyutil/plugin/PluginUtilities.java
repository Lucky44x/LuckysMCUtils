package com.github.lucky44x.luckyutil.plugin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PluginUtilities {

    private static final MaterialsManager matsProvider = new MaterialsManager();

    public static String getTranslationKey(Material material) {
        if (material.isBlock()) {
            String id = material.getKey().getKey();
            return "block.minecraft." + id;
        } else if (material.isItem()) {
            String id = material.getKey().getKey();
            return "item.minecraft." + id;
        }

        return "block.minecraft.dirt";
    }

    public static boolean isVersionGreaterEquals(String toCheck, String base) {
        version vA = new version(toCheck);
        version vB = new version(base);

        if (vA.minor > vB.minor) return true;

        if (vA.minor == vB.minor) {
            return vA.revision >= vB.revision;
        }

        return false;
    }

    private record version(int major, int minor, int revision) {
        public version(String in) {
            this(
                    in.contains("_") ? Integer.parseInt(in.split("_")[0]) : Integer.parseInt(in.split("\\.")[0]),
                    in.contains("_") ? Integer.parseInt(in.split("_")[1]) : Integer.parseInt(in.split("\\.")[1]),
                    in.contains("_")
                            ? in.split("_").length == 3 ? Integer.parseInt(in.split("_")[2].substring(1)) : 0
                            : in.split("\\.").length == 3 ? Integer.parseInt(in.split("\\.")[2].substring(1)) : 0);
        }
    }

    public static String getServerVersion() {
        return Bukkit.getServer()
                .getClass()
                .getPackage()
                .getName()
                .split("\\.")[3]
                .substring(1);
    }

    public static ItemStack getItemFromMaterialName(String materialName) {
        return matsProvider.getItem(materialName);
    }
}
