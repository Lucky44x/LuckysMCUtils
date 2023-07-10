package com.github.lucky44x.luckyutil.chat;


import com.github.lucky44x.luckyutil.chat.abstraction.NMSChatProvider;
import com.github.lucky44x.luckyutil.plugin.PluginUtilities;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ChatManager {
    private final NMSChatProvider chatProvider;
    private final Plugin instance;

    public ChatManager(@Nonnull Plugin instance) {
        this.instance = instance;
        chatProvider = getChatProvider();
    }

    public HoverEvent generateItemHoverEvent(ItemStack item) {
        if (chatProvider == null)
            return new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.RED + "This feature is not supported on version "
                            + PluginUtilities.getServerVersion()));

        return chatProvider.generateItemHoverEvent(item);
    }

    public Item generateItemHoverContent(ItemStack item) {
        if (chatProvider == null) return null;

        return new Item(
                item.getType().getKey().getKey(),
                item.getAmount(),
                ItemTag.ofNbt(chatProvider.getChatItemString(item)));
    }

    private NMSChatProvider getChatProvider() {
        if (chatProvider != null) return chatProvider;

        try {
            return (NMSChatProvider) Class.forName(getClass().getPackage().getName() + ".versions.NMSChatProvider_"
                            + PluginUtilities.getServerVersion())
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InvocationTargetException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException e) {
            instance.getLogger()
                    .severe("Could not initialize ChatProvider for server version " + PluginUtilities.getServerVersion()
                            + " :");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            instance.getLogger()
                    .warning("Server version " + PluginUtilities.getServerVersion()
                            + " is not supported -> some chat related features might not work");
        }

        return null;
    }
}
