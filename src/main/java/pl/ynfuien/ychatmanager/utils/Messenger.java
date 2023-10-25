package pl.ynfuien.ychatmanager.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.hooks.Hooks;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messenger {
    private final static MiniMessage miniMessage = MiniMessage.miniMessage();
    private final static Pattern placeholderPattern = PlaceholderAPI.getPlaceholderPattern();
    private final static boolean papiEnabled = Hooks.isPapiEnabled();

    private final static Pattern formattedPlaceholderPattern = Pattern.compile("[{]([^{}]+)[}]");
    private final static Pattern unformattedPlaceholderPattern = Pattern.compile("[{]!([^{}]+)[}]");

    // Formats colors and placeholders and sends message to CommandSender
    public static void send(CommandSender sender, String message) {
        Component component = parseMessage(sender, message);

        sender.sendMessage(component);
    }

    // Same as above, but after parsing message, it also replaces "unformatted placeholders"
    public static void send(CommandSender sender, String message, HashMap<String, Object> placeholders) {
        Component component = parseMessage(sender, message);

        if (unformattedPlaceholderPattern.matcher(message).find()) {
            for (String key : placeholders.keySet()) {
                Object value = placeholders.get(key);

                String placeholder = "{!" + key + "}";

                TextReplacementConfig replacementConfig = TextReplacementConfig
                        .builder()
                        .matchLiteral(placeholder)
                        .replacement(value.toString())
                        .build();

                component = component.replaceText(replacementConfig);
            }
        }

        sender.sendMessage(component);
    }

    // Code copied from YouHaveTrouble and slightly modified.
    // Parses PAPI placeholders and MiniMessage formats.
    public static Component parseMessage(CommandSender sender, String message) {
        Component formattedMessage = miniMessage.deserialize(message.replace("§", ""));

        // Return formatted message if there won't be papi parsing
        if (!papiEnabled) return formattedMessage;
        if (!PlaceholderAPI.containsPlaceholders(message)) return formattedMessage;
        if (!(sender instanceof Player p)) return formattedMessage;

        // Parse papi placeholders
        Matcher matcher = placeholderPattern.matcher(message);
        while (matcher.find()) {
            String placeholder = matcher.group(0);

            String parsedPlaceholder = PlaceholderAPI.setPlaceholders(p, placeholder);
            if (parsedPlaceholder.equals(placeholder)) continue;

            Component formattedPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);

            TextReplacementConfig replacementConfig = TextReplacementConfig
                    .builder()
                    .match(placeholder)
                    .replacement(formattedPlaceholder)
                    .build();
            formattedMessage = formattedMessage.replaceText(replacementConfig);
        }
        return formattedMessage;
    }

    // Replaces plugin placeholders in provided text
    public static String replacePlaceholders(String text, HashMap<String, Object> placeholders) {
        if (text == null) return null;
        if (placeholders == null) return text;


        // Loop max 3 times through placeholders,
        // in case there would be placeholders inside placeholders.
        // And in case there would be too many placeholders inside placeholders,
        // it only repeats 3 times, and not using while.
        for (int i = 0; i < 3; i++) {
            if (!formattedPlaceholderPattern.matcher(text).find()) return text;

            // Loop through placeholders
            for (String placeholder : placeholders.keySet()) {
                String value = String.valueOf(placeholders.get(placeholder));
                String ph = "{" + placeholder + "}";

                text = text.replace(ph, value);
            }
        }

        return text;
    }

    public static MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
