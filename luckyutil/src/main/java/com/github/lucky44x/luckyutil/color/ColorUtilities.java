package com.github.lucky44x.luckyutil.color;


import com.github.lucky44x.luckyutil.plugin.PluginUtilities;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtilities {
    private static final Pattern colorPattern = Pattern.compile("<#[a-fA-F0-9]{6}>");
    private static final Pattern gradientPattern = Pattern.compile("<#[a-fA-F0-9]{6}.*?#[a-fA-F0-9]{6}>");

    public static String translateColors(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (PluginUtilities.isVersionGreaterEquals(PluginUtilities.getServerVersion(), "1.16")) {

            // Bukkit.getLogger().info("Translating colors for: " + message);

            Matcher colorMatcher = colorPattern.matcher(message);
            while (colorMatcher.find()) {
                String colorCode = message.substring(colorMatcher.start() + 1, colorMatcher.end() - 1);
                String matched = message.substring(colorMatcher.start(), colorMatcher.end());

                // Bukkit.getLogger().info("Found color tag at: " + message + " - " + matched);

                message = message.replace(matched, String.valueOf(ChatColor.of(colorCode)));
                colorMatcher = colorPattern.matcher(message);
                /*
                Bukkit.getLogger().info("New text: " + message);
                Bukkit.getLogger().info("------");
                */
            }

            // Bukkit.getLogger().info("Searching for Gradients in: " + message);
            Matcher gradientMatcher = gradientPattern.matcher(message);
            while (gradientMatcher.find()) {
                String matched = message.substring(gradientMatcher.start(), gradientMatcher.end());

                // Bukkit.getLogger().info("Found gradient tag at: " + message + " - " + matched);

                message = message.replace(matched, translateGradient(matched));
                gradientMatcher = gradientPattern.matcher(message);
            }
        }

        return message;
    }

    private static String translateGradient(String regex) {

        String endColorSubString = regex.substring(regex.length() - 8, regex.length() - 1);
        String startColorSubstring = regex.substring(1, 8);

        /*
        Bukkit.getLogger().info("Translating gradient for " + regex);
        Bukkit.getLogger().info("StartColor: " + startColorSubstring);
        Bukkit.getLogger().info("EndColor: " + endColorSubString);
         */
        Color start = fromHex(endColorSubString);
        Color end = fromHex(startColorSubstring);
        String content = regex.substring(8, regex.length() - 8);

        /*
        Bukkit.getLogger().info("Content: " + content);

        Bukkit.getLogger().info("Applying color -->");
        */

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) != ' ' && content.charAt(i) != '␀') {
                float blending = (float) i / (float) (content.length() - 1);
                float inverseBlending = 1 - blending;

                float red = start.getRed() * blending + end.getRed() * inverseBlending;
                red = red > 255 ? 255 : red < 0 ? 0 : red;
                float green = start.getGreen() * blending + end.getGreen() * inverseBlending;
                green = green > 255 ? 255 : green < 0 ? 0 : green;
                float blue = start.getBlue() * blending + end.getBlue() * inverseBlending;
                blue = blue > 255 ? 255 : blue < 0 ? 0 : blue;

                // Bukkit.getLogger().info(i + " : (" + red + " | " + green + " | " + blue + ")");

                output.append(ChatColor.of(new Color(red / 255, green / 255, blue / 255)));
            }

            output.append(content.charAt(i));
        }
        // Bukkit.getLogger().info("------");

        return output.toString();
    }

    private static Color fromHex(String hex) {

        // Bukkit.getLogger().info("Getting color from hex: " + hex);

        if (hex.contains("#")) {
            return new Color(
                    Integer.valueOf(hex.substring(1, 3), 16),
                    Integer.valueOf(hex.substring(3, 5), 16),
                    Integer.valueOf(hex.substring(5, 7), 16));
        } else {
            return new Color(
                    Integer.valueOf(hex.substring(0, 2), 16),
                    Integer.valueOf(hex.substring(2, 4), 16),
                    Integer.valueOf(hex.substring(4, 6), 16));
        }
    }

    public static String stripColor(String message) {
        Matcher matcher = colorPattern.matcher(message);
        while (matcher.find()) {
            String matched = message.substring(matcher.start(), matcher.end());
            message = message.replace(matched, "");
            matcher = colorPattern.matcher(message);
        }

        return ChatColor.stripColor(message);
    }

    private static boolean contains(String input, String... array) {
        for (String arr : array) if (input.contains(arr)) return true;

        return false;
    }
}
