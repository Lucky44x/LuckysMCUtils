package com.github.lucky44x.luckyutil.chat.versions;


import com.github.lucky44x.luckyutil.chat.abstraction.NMSChatProvider;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;

public class NMSChatProvider_1_10_R1 extends NMSChatProvider {
    @Override
    protected String convertItemToChatString(org.bukkit.inventory.ItemStack originalItem) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(originalItem);
        NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItem.a(compound.toString(), false);
        assert compound != null;
        return compound.toString();
    }
}
