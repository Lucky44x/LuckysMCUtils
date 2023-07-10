package com.github.lucky44x.luckyutil.plugin;


import com.github.lucky44x.luckyutil.config.ConfigFile;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.github.lucky44x.luckyutil.reflect.ReflectionUtilities;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LuckyPlugin extends JavaPlugin {
    private final Listener[] listeners = getListeners();
    private final List<CommandData> commands = getCommands();

    @Getter
    private final ConfigFile CONFIG;

    @Getter
    private final LangConfig LANG;

    public LuckyPlugin(Class<?> config, String langName) {
        try {
            this.CONFIG = (ConfigFile) config.getConstructor(this.getClass()).newInstance(this);

        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.LANG = new LangConfig(this, langName);
    }

    @Override
    public final void onEnable() {
        final long startTime = System.nanoTime();

        getLogger().info("Saving default config and lang");
        CONFIG.saveDefault();
        LANG.saveDefault();

        getLogger().info("(" + getElapsedTime(startTime) + ") -> Registering " + listeners.length + " listeners");
        final long listenerStartTime = System.nanoTime();
        for (Listener listener : listeners) {
            getLogger()
                    .info("(" + getElapsedTime(listenerStartTime) + ") -> Registering listener: "
                            + listener.getClass().getSimpleName());
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
        getLogger().info("Registered " + listeners.length + " listeners in " + getElapsedTime(listenerStartTime));

        getLogger().info("(" + getElapsedTime(startTime) + ") -> Registering " + commands.size() + " commands");
        final long commandStartTime = System.nanoTime();
        for (CommandData command : commands) {
            String commandName = command.name();
            Bukkit.getLogger()
                    .info("(" + getElapsedTime(commandStartTime)
                            + ") -> Registering executor and (if exists)completer class "
                            + command.executorObject.getClass().getSimpleName() + " for command " + commandName);

            PluginCommand cmd = getCommand(commandName);
            if (cmd == null) {
                getLogger().warning("Command with name " + commandName + " could not be found");
                continue;
            }

            cmd.setExecutor((CommandExecutor) command.executorObject);
            if (command.isCompleter) cmd.setTabCompleter((TabCompleter) command.executorObject);
        }
        getLogger().info("Registered " + commands.size() + " commands in " + getElapsedTime(commandStartTime));

        onStartup(startTime);
    }

    @Override
    public final void onDisable() {
        final long startTime = System.nanoTime();
        onShutdown(startTime);
    }

    public void reload() {
        CONFIG.reload();
        LANG.reload();
    }

    protected abstract void onStartup(final long startTime);

    protected abstract void onShutdown(final long startTime);

    protected String getElapsedTime(long startTime) {
        return (System.nanoTime() - startTime) / 1000000 + "ms";
    }

    private Listener[] getListeners() {
        ArrayList<Listener> listeners = new ArrayList<>();

        try {
            for (Class<?> clazz :
                    ReflectionUtilities.getClassesByPackageName(this.getClass().getPackageName() + ".events")) {
                if (!Arrays.stream(clazz.getInterfaces()).toList().contains(Listener.class)) continue;

                if (clazz.isAnnotationPresent(IgnoreAutoLoad.class)) continue;

                listeners.add((Listener) clazz.getConstructor(this.getClass()).newInstance(this));
            }
        } catch (ClassNotFoundException
                | IOException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return listeners.toArray(Listener[]::new);
    }

    private List<CommandData> getCommands() {
        List<CommandData> commands = new ArrayList<>();

        try {
            for (Class<?> clazz :
                    ReflectionUtilities.getClassesByPackageName(this.getClass().getPackageName() + ".commands")) {
                if (!Arrays.stream(clazz.getInterfaces()).toList().contains(CommandExecutor.class)) continue;

                if (clazz.isAnnotationPresent(IgnoreAutoLoad.class)
                        || !clazz.isAnnotationPresent(CommandAutoLoad.class)) continue;

                CommandAutoLoad autoLoad = clazz.getAnnotation(CommandAutoLoad.class);

                commands.add(new CommandData(
                        autoLoad.value(),
                        clazz.getConstructor(this.getClass()).newInstance(this),
                        Arrays.stream(clazz.getInterfaces()).toList().contains(TabCompleter.class)));
            }
        } catch (ClassNotFoundException
                | IOException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return commands;
    }

    private record CommandData(String name, Object executorObject, boolean isCompleter) {}
}
