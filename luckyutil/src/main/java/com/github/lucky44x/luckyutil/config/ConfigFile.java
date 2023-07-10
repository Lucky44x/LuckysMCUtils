package com.github.lucky44x.luckyutil.config;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public abstract class ConfigFile {
    protected final Plugin instance;
    private final String configName;
    private final File configFile;
    protected YamlConfiguration config;

    /**
     * Creates a new config-file instance
     * @param instance the main-plugin instance
     * @param configName the name of the config-file
     */
    public ConfigFile(Plugin instance, String configName) {
        this.configName = configName;
        this.instance = instance;

        configFile = new File(instance.getDataFolder(), configName + ".yml");
    }

    /**
     * reloads the config and updates all values by reflection
     */
    protected abstract void reloadFile();

    /**
     * Gets the FileConfiguration
     * @return the FileConfig of the
     */
    public final FileConfiguration getConfigFile() {
        return config;
    }

    public final void reload() {
        loadConfigFile();
        reloadFile();
    }

    private void loadConfigFile() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves the default version of the config file if it doesn't already exist
     */
    public final void saveDefault() {
        if (configFile.exists()) return;

        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream inputStream = instance.getResource(configName + ".yml")) {

            if (inputStream == null)
                throw new RuntimeException("Could not locate " + configName + ".yml in the plugin resources");

            try (FileOutputStream outputStream = new FileOutputStream(configFile, false)) {
                int read;
                byte[] byteBuffer = new byte[1024];
                while ((read = inputStream.read(byteBuffer)) != -1) {
                    outputStream.write(byteBuffer, 0, read);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
