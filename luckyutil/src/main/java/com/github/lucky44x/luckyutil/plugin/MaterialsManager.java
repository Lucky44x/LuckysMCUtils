package com.github.lucky44x.luckyutil.plugin;


import com.github.lucky44x.luckyutil.plugin.abstraction.SpecialItemProvider;
import com.github.lucky44x.luckyutil.plugin.versions.MaterialProvider_1_12_R1;
import com.github.lucky44x.luckyutil.plugin.versions.MaterialProvider_1_19_R3;
import org.bukkit.inventory.ItemStack;

public class MaterialsManager {

    SpecialItemProvider provider = getProvider();

    public SpecialItemProvider getProvider() {
        if (PluginUtilities.isVersionGreaterEquals(PluginUtilities.getServerVersion(), "1_13")) {
            return new MaterialProvider_1_19_R3();
        }

        return new MaterialProvider_1_12_R1();
    }

    public ItemStack getItem(String name) {
        return provider.getItemFromMaterial(name);
    }
}
