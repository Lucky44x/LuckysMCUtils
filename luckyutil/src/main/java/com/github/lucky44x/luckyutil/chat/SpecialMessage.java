package com.github.lucky44x.luckyutil.chat;


import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class SpecialMessage {
    private final BaseComponent[] components;

    public SpecialMessage(Builder builder) {
        this.components = builder.buildComponents();
    }

    public void send(Player player) {
        player.spigot().sendMessage(components);
    }

    public interface Builder {
        BaseComponent[] buildComponents();
    }
}
