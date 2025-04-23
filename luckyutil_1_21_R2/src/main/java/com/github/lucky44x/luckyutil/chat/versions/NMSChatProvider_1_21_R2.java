package com.github.lucky44x.luckyutil.chat.versions;


import com.github.lucky44x.luckyutil.chat.abstraction.NMSChatProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSChatProvider_1_21_R2 extends NMSChatProvider {
    @Override
    protected String convertItemToChatString(ItemStack originalItem) {
        NBTTagCompound compound = new NBTTagCompound();
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer(); // Cursed but whatever
        HolderLookup.a lookup = server.ba();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(originalItem);

        // IChatBaseComponent component = nmsItem.F();
        // return IChatBaseComponent.ChatSerializer.a(component, lookup);

        compound = (NBTTagCompound) nmsItem.b(lookup, compound);
        return compound.toString();
    }
}
