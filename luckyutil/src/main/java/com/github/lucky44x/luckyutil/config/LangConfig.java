package com.github.lucky44x.luckyutil.config;


import com.github.lucky44x.luckyutil.color.ColorUtilities;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 * The ConfigFile class specific to LANG-files
 * @author Nick Balischewski
 */
public class LangConfig extends ConfigFile {
    /**
     * Creates a new config-file instance
     *
     * @param instance   the main-plugin instance
     * @param configName the name of the config-file
     */
    public LangConfig(Plugin instance, String configName) {
        super(instance, configName);
    }

    @Override
    protected void reloadFile() {
        // noop
    }

    /**
     * Gets the Text with the specified tag from the config-file and automatically translates spigot- and custom-colorCodes
     * @param id the tag of the text
     * @param callers the object/s which called the method (use with {@link LangData}LangData annotation to auto complete)
     * @return the text from the lang file, complete with translation of the [tags]
     */
    public String getText(String id, Object... callers) {
        if (!config.contains(id)) return ChatColor.RED + "No value for \"" + id + "\" found in lang-file";

        return ColorUtilities.translateColors(translateKeys(config.getString(id), callers));
    }

    /**
     * Gets the Text with the specified tag from the config-file
     * @param id the tag of the text
     * @param callers the object which called the method (use with {@link LangData}LangData annotation to auto complete)
     * @return the text from the lang file, complete with translation of the [tags]
     */
    public String getTextWithoutColors(String id, Object... callers) {
        if (!config.contains(id)) return ChatColor.RED + "No value for \"" + id + "\" found in lang-file";

        return translateKeys(config.getString(id), callers);
    }

    public BaseComponent[] getPostProcessedText(String id, Object... callers) {
        return new PostProcessor(id, callers).translate();
    }

    /**
     * Gets the Text-List with the specified tag from the config-file
     * @param id the tag of the text-list
     * @param callers the object which called the method (use with {@link LangData}LangData annotation to auto complete)
     * @return the text-list from the lang file, complete with translation of the [tags]
     */
    public List<String> getTextList(String id, Object... callers) {
        if (!config.contains(id)) return List.of(ChatColor.RED + "No value for \"" + id + "\" found in lang-file");

        List<String> strings = new ArrayList<>();
        for (String s : config.getStringList(id)) {
            strings.add(ColorUtilities.translateColors(translateKeys(s, callers)));
        }

        return strings;
    }

    public String getRawText(String id) {
        if (!config.contains(id)) return ChatColor.RED + "No value for \"" + id + "\" found in lang-file";

        return config.getString(id);
    }

    public List<String> getRawTextList(String id) {
        if (!config.contains(id)) return List.of(ChatColor.RED + "No value for \"" + id + "\" found in lang-file");

        return new ArrayList<String>(config.getStringList(id));
    }

    public String translateKeys(String text, Object... callers) {
        Map<String, String> toReplace = new HashMap<>();

        for (Object caller : callers) {
            for (Field f : getAllFields(new ArrayList<>(), caller.getClass())) {
                f.setAccessible(true);
                if (!f.isAnnotationPresent(LangData.class)) continue;

                LangData annotation = f.getAnnotation(LangData.class);

                if (annotation.langKey() == null) continue;

                if (!f.getType().equals(String.class)) {
                    if (annotation.stringMethodNames().equals("NULL")) continue;

                    String[] methods = annotation.stringMethodNames().split("-");
                    try {
                        Object previousReturn = f.get(caller);

                        if (previousReturn == null) {
                            Bukkit.getLogger().severe(f.getName() + " is NULL");
                            continue;
                        }

                        for (String name : methods) {
                            try {
                                Method m = previousReturn.getClass().getMethod(name);
                                m.setAccessible(true);
                                previousReturn = m.invoke(previousReturn);
                            } catch (Exception e) {
                                Bukkit.getLogger()
                                        .severe("Could not find method " + name + " in object "
                                                + previousReturn.getClass().getSimpleName());
                                e.printStackTrace();
                            }
                        }

                        if (previousReturn.getClass() == String.class) {
                            toReplace.put(annotation.langKey(), (String) previousReturn);
                        } else Bukkit.getLogger().severe("Could not find String-returning method at designated path");
                    } catch (Exception e) {
                        Bukkit.getLogger()
                                .severe("Failed to receive Object Instance from field " + f.getName() + " on "
                                        + caller.getClass().getSimpleName());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        toReplace.put(annotation.langKey(), (String) f.get(caller));
                    } catch (IllegalAccessException e) {
                        Bukkit.getLogger().severe("Exception during LANG: " + e.getCause());
                        throw new RuntimeException(e);
                    }
                }
            }

            for (Method m : getAllMethods(new ArrayList<>(), caller.getClass())) {
                m.setAccessible(true);

                if (!m.isAnnotationPresent(LangData.class)) continue;

                LangData annotation = m.getAnnotation(LangData.class);

                if (!m.getReturnType().equals(String.class)) continue;

                if (annotation.langKey() == null) continue;

                try {
                    toReplace.put(annotation.langKey(), (String) m.invoke(caller));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Map.Entry<String, String> entry : toReplace.entrySet()) {
            // Bukkit.getLogger().info("Replacing " + entry.getKey() + " with " + entry.getValue() + " in " + text);
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return text;
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> clazz) {
        // Bukkit.getLogger().warning("Getting Fields for [" + clazz.getPackageName() + "]." + clazz.getSimpleName());
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            /*Bukkit.getLogger()
            .warning("Checking if [" + clazz.getSuperclass().getPackageName() + "] starts with ["
                    + instance.getClass().getPackageName() + "]");*/
            if (!clazz.getSuperclass()
                    .getPackageName()
                    .startsWith(instance.getClass().getPackageName())) return fields;
            else return getAllFields(fields, clazz.getSuperclass());
        }

        return fields;
    }

    private List<Method> getAllMethods(List<Method> methods, Class<?> clazz) {
        // Bukkit.getLogger().info("Getting Methods for [" + clazz.getPackageName() + "]." + clazz.getSimpleName());
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

        if (clazz.getSuperclass() != null) {
            /*Bukkit.getLogger()
            .warning("Checking if [" + clazz.getSuperclass().getPackageName() + "] starts with ["
                    + instance.getClass().getPackageName() + "]");*/
            if (!clazz.getSuperclass()
                    .getPackageName()
                    .startsWith(instance.getClass().getPackageName())) return methods;
            else return getAllMethods(methods, clazz.getSuperclass());
        }

        return methods;
    }

    /**
     * The LangData annotation which allows for automatic completion of tags inside the lang-file
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    public @interface LangData {
        boolean isPostProcessor() default false;

        String stringMethodNames() default "NULL";

        String langKey();
    }

    private class PostProcessor {
        private final String textID;
        private final Object[] callers;

        private final Map<String, methodCarrier> postProcessors = new HashMap<>();

        public PostProcessor(String textID, Object... callers) {
            this.textID = textID;
            this.callers = callers;

            for (Object instance : callers) {
                for (Method m : getAllMethods(new ArrayList<>(), instance.getClass())) {
                    m.setAccessible(true);

                    if (m.isAnnotationPresent(LangData.class)) {
                        if (!m.getReturnType().equals(String.class)
                                && !m.getReturnType().equals(BaseComponent[].class)) continue;

                        LangData data = m.getAnnotation(LangData.class);
                        if (data.langKey() == null) continue;
                        if (!data.isPostProcessor()) continue;

                        postProcessors.put(data.langKey(), new methodCarrier(m, instance));
                    }
                }
            }
        }

        public BaseComponent[] translate() {
            String message = getTextWithoutColors(textID, callers);
            List<String> subStrings = new ArrayList<>();

            List<BaseComponent> ret = new ArrayList<>();

            for (int start = 0; start < message.length(); start++) {
                if (message.charAt(start) == '{') {
                    for (int end = start; end < message.length(); end++) {
                        if (message.charAt(end) == '}') {
                            subStrings.add(message.substring(start + 1, end));
                            /*
                            Bukkit.getLogger()
                                    .info("Found subString: (" + start + " : " + end + ") "
                                            + message.substring(start + 1, end));
                            */
                            start = end;
                            break;
                        }
                    }
                }
            }

            // Bukkit.getLogger().info("END substring search --------------------- BEGIN segment search");

            // Get all the different segments
            List<String> segments = new ArrayList<>();
            String segmentingMessage = message;
            for (int u = 0; u < subStrings.size(); u++) {
                String subString = subStrings.get(u);
                segments.add(segmentingMessage.split("\\{" + subString + "}")[0]);

                /*
                Bukkit.getLogger()
                        .info("Segment " + u + " : " + segmentingMessage.split("\\{" + subString + "}")[0]);
                 */
                if (segmentingMessage.split("\\{" + subString + "}").length < 2) {
                    instance.getLogger()
                            .warning("Cannot find second part of split action: {" + subString + "}: "
                                    + Arrays.toString(segmentingMessage.split("\\{" + subString + "}")));
                } else {
                    segmentingMessage = segmentingMessage.split("\\{" + subString + "}")[1];
                    // Bukkit.getLogger().info("New Segmentmessage : " + segmentingMessage);
                }
                if (u == subStrings.size() - 1) {
                    // Bukkit.getLogger().info("Last Segment : " + segmentingMessage);
                    segments.add(segmentingMessage);
                }
            }

            if (segments.size() == 0) {
                return TextComponent.fromLegacyText(getText(textID, callers));
            }

            // Bukkit.getLogger().info("END segment-search --------------------- BEGIN color-translation");

            // translate colors
            String combinedSegments = String.join("|", segments);
            // Bukkit.getLogger().info("Combined Segments: " + combinedSegments);
            combinedSegments = ColorUtilities.translateColors(combinedSegments);
            // Bukkit.getLogger().info("Combined Segments with color: " + combinedSegments);

            segments.clear();
            segments.addAll(Arrays.stream(combinedSegments.split("\\|")).toList());
            // Bukkit.getLogger().info("Split Segments with color: " + Arrays.toString(combinedSegments.split("\\|")));

            // Bukkit.getLogger().info("END color-translation ---------------------- BEGIN post-processing");

            for (int i = 0; i < segments.size(); i++) {
                // Bukkit.getLogger().info("Adding " + segments.get(i) + " to message");
                ret.addAll(List.of(TextComponent.fromLegacyText(segments.get(i))));

                if (i >= subStrings.size()) {
                    // Bukkit.getLogger().info("No more substrings... continue");
                    continue;
                }

                String subString = subStrings.get(i);
                String payload = subString;
                BaseComponent[] componentPayload = null;

                String[] args = subString.split(":");
                if (args.length > 0) payload = args[0];

                /*
                Bukkit.getLogger()
                        .info("Init Method search: payloadText: " + payload + " args: " + Arrays.toString(args));
                 */
                for (int u = 1; u < args.length; u++) {

                    // Bukkit.getLogger().info("BEGIN -------------- loop-revision " + u);

                    if (!postProcessors.containsKey(args[u])) {
                        // Bukkit.getLogger().severe("No Post-Processor found for " + args[u]);
                        ret.add(new TextComponent(ChatColor.RED + "Could not get post-processor for key " + args[u]));
                        continue;
                    }

                    methodCarrier carrier = postProcessors.get(args[u]);
                    /*
                    Bukkit.getLogger()
                            .info("Post-Processor for: " + args[u] + "\n{\nreturnType: "
                                    + carrier.method.getReturnType().getSimpleName() + "\nparameterType: "
                                    + carrier.method.getParameterTypes()[0].getSimpleName() + "\nname: "
                                    + carrier.method.getName() + "\n}\n");
                     */
                    try {
                        if (carrier.method.getReturnType().equals(BaseComponent[].class)) {
                            if (carrier.method.getParameterTypes()[0].equals(String.class)) {
                                componentPayload = (BaseComponent[]) carrier.method.invoke(carrier.instance, payload);
                            } else {
                                if (componentPayload == null) componentPayload = TextComponent.fromLegacyText(payload);

                                componentPayload = (BaseComponent[])
                                        carrier.method.invoke(carrier.instance, (Object) componentPayload);
                            }
                        } else {
                            if (carrier.method.getParameterTypes()[0].equals(String.class)) {
                                payload = (String) carrier.method.invoke(carrier.instance, payload);
                            } else {
                                if (componentPayload == null) componentPayload = TextComponent.fromLegacyText(payload);

                                payload = (String) carrier.method.invoke(carrier.instance, (Object) componentPayload);
                            }
                        }

                        /*
                        Bukkit.getLogger().info("New payload: " + payload);
                        if (componentPayload != null)
                            Bukkit.getLogger().info("New componentPayload: " + Arrays.toString(componentPayload));

                        Bukkit.getLogger().info("END loop-revision " + u);
                        */
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                // Bukkit.getLogger().info("Adding all components to ret");
                if (componentPayload != null) ret.addAll(List.of(componentPayload));
                // Bukkit.getLogger().info("END ------------- translation-loop");
            }

            return ret.toArray(BaseComponent[]::new);
        }
    }

    private record methodCarrier(Method method, Object instance) {}

    private record fieldCarrier(Method method, Object instance) {}
}
