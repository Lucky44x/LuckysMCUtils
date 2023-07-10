package com.github.lucky44x.luckyutil.config;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public abstract class AutomatedConfig extends ConfigFile {

    private final HashMap<String, Field> fieldsToSet = new HashMap<>();

    /**
     * Creates a new config-file instance
     *
     * @param instance   the main-plugin instance
     * @param configName the name of the config-file
     */
    public AutomatedConfig(Plugin instance, String configName) {
        super(instance, configName);

        for (Field f : getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(ConfigData.class)) continue;

            String yamlFieldName = f.getName();
            if (!f.getAnnotation(ConfigData.class).tag().equals(""))
                yamlFieldName = f.getAnnotation(ConfigData.class).tag();

            f.setAccessible(true);
            fieldsToSet.put(yamlFieldName, f);
        }

        saveDefault();
    }

    @Override
    protected void reloadFile() {

        for (Map.Entry<String, Field> entry : fieldsToSet.entrySet()) {
            String fieldName = entry.getKey();
            Field field = entry.getValue();

            if (!config.contains(fieldName)) continue;

            try {
                if (field.getType().equals(boolean.class)) field.set(this, config.getBoolean(fieldName));
                else if (field.getType().equals(int.class)) field.set(this, config.getInt(fieldName));
                else if (field.getType().equals(double.class)) field.set(this, config.getDouble(fieldName));
                else if (field.getType().equals(long.class)) field.set(this, config.getLong(fieldName));
                else if (field.getType().equals(String.class)) {
                    String text = config.getString(fieldName);
                    field.set(
                            this,
                            ChatColor.translateAlternateColorCodes(
                                    '&', text == null ? "&cNO ENTRY FOR " + fieldName + " FOUND" : text));
                } else if (field.getType().equals(Color.class)) field.set(this, config.getColor(fieldName));
                else if (field.getType().equals(ItemStack.class)) field.set(this, config.getItemStack(fieldName));
                else if (field.getType().equals(Location.class)) field.set(this, config.getLocation(fieldName));
                else if (field.getType().equals(ArrayList.class)) field.set(this, config.getList(fieldName));
                else if (field.getType().equals(OfflinePlayer.class))
                    field.set(this, config.getOfflinePlayer(fieldName));
                else if (field.getType().equals(Vector.class)) field.set(this, config.getVector(fieldName));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigData {
        String tag() default "";
    }
}
