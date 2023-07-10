package com.github.lucky44x.luckyutil.chat.versions;


import com.github.lucky44x.luckyutil.chat.abstraction.NMSChatProvider;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSChatProvider_1_19_R3 extends NMSChatProvider {
    @Override
    protected String convertItemToChatString(ItemStack originalItem) {
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(originalItem);
        NBTTagCompound compound = new NBTTagCompound();
        nmsItem.b(compound);
        return compound.toString();
    }
}
