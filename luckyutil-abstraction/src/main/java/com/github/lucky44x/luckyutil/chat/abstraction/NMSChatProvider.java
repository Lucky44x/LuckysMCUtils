package com.github.lucky44x.luckyutil.chat.abstraction;


import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

public abstract class NMSChatProvider {
    protected abstract String convertItemToChatString(ItemStack originalItem);

    public final String getChatItemString(ItemStack originalItem) {
        if (originalItem == null) return "";

        return convertItemToChatString(originalItem);
    }

    public final HoverEvent generateItemHoverEvent(ItemStack item) {
        // Deprecated, I know, but I couldn't be bothered to figure out how to do this with a HoverContent Item thingy
        return new HoverEvent(
                HoverEvent.Action.SHOW_ITEM, new BaseComponent[] {new TextComponent(getChatItemString(item))});
    }
}
