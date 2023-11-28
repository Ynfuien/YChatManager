package pl.ynfuien.ychatmanager.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.chat.ColorFormatter;
import pl.ynfuien.ychatmanager.hooks.Hooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messenger {
    private final static boolean PAPI_ENABLED = Hooks.isPluginEnabled(Hooks.Plugin.PAPI);

    private final static Pattern PLUGIN_PLACEHOLDER_PATTERN = Pattern.compile("[{]([^{}]+)[}]");
    private final static Pattern PLUGIN_PLACEHOLDER_FLAGS_PATTERN = Pattern.compile("(?<=[{])[!@#]+(?=([^{}]+)[}])");


    public static void send(CommandSender receiver, String message) {
        message = message.replace('§', '&');
        message = parsePAPI(receiver, message);

        Component formatted = ColorFormatter.SERIALIZER.deserialize(message);
        if (formatted.equals(Component.empty())) return;

        receiver.sendMessage(formatted);
    }

    public static void send(CommandSender receiver, String message, HashMap<String, Object> placeholders) {
        if (receiver == null) return;
        if (message == null) return;

        Component formatted = parseMessage(receiver, message, placeholders);
        if (formatted.equals(Component.empty())) return;

        receiver.sendMessage(formatted);
    }

    public static Component parseMessage(CommandSender sender, String message, HashMap<String, Object> placeholders) {
        Set<PluginPlaceholder> usedPlaceholders = getUsedPlaceholders(message, placeholders);

        // Placeholders with all colors, formats, PAPI and also with escaped quotes
        Set<PluginPlaceholder> replaced = new HashSet<>();
        for (PluginPlaceholder placeholder : usedPlaceholders) {
            if (placeholder.isFlagNoFormatting() || placeholder.isFlagNoAPI()) continue;

            String value = placeholder.value();
            message = message.replace(placeholder.exactMatch(), value);

            replaced.add(placeholder);
        }
        usedPlaceholders.removeAll(replaced);
        replaced.clear();

        message = message.replace('§', '&');
        message = parsePAPI(sender, message);

        // Placeholders without parsed PAPI
        for (PluginPlaceholder placeholder : usedPlaceholders) {
            if (!placeholder.isFlagNoAPI()) continue;

            String value = placeholder.value();
            message = message.replace(placeholder.exactMatch(), value);

            replaced.add(placeholder);
        }
        usedPlaceholders.removeAll(replaced);
        replaced.clear();

        Component formatted = MiniMessage.miniMessage().deserialize(message);

        // Unformatted placeholders, without colors, formats and PAPI
        for (PluginPlaceholder placeholder : usedPlaceholders) {
            String value = placeholder.value();
            Component unformattedValue = Component.text(value);

            TextReplacementConfig replacement = TextReplacementConfig
                    .builder()
                    .matchLiteral(placeholder.exactMatch())
                    .replacement(unformattedValue)
                    .build();
            formatted = formatted.replaceText(replacement);
        }

        return formatted;
    }

    public static String parsePAPI(CommandSender sender, String message) {
        if (message.isBlank()) return message;
        if (!PAPI_ENABLED) return message;
        if (!(sender instanceof Player p)) return message;
        if (!PlaceholderAPI.containsPlaceholders(message)) return message;

        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
        while (matcher.find()) {
            String match = matcher.group();

            String parsed = PlaceholderAPI.setPlaceholders(p, match);
            if (match.equals(parsed)) continue;

            parsed = parsed.replace('§', '&');
            String formatted = MiniMessage.miniMessage().serialize(ColorFormatter.SERIALIZER.deserialize(parsed));
            message = message.replace(match, formatted);
        }

        return message;
    }


    public static String parsePluginPlaceholders(String text, HashMap<String, Object> placeholders) {
        if (text == null) return null;
        if (text.isBlank()) return text;
        if (placeholders.isEmpty()) return text;

        Set<PluginPlaceholder> usedPlaceholders = getUsedPlaceholders(text, placeholders);
        if (usedPlaceholders.isEmpty()) return text;

        for (PluginPlaceholder placeholder : usedPlaceholders) {
            if (placeholder.isFlagNoFormatting() || placeholder.isFlagNoAPI()) continue;

            String value = placeholder.value();
            text = text.replace(placeholder.exactMatch(), value);
        }

        return text;
    }

    private static Set<PluginPlaceholder> getUsedPlaceholders(String text, HashMap<String, Object> placeholders) {
        Set<PluginPlaceholder> usedPlaceholders = new HashSet<>();

        if (text == null) return usedPlaceholders;
        if (text.isBlank()) return usedPlaceholders;
        if (placeholders == null) return usedPlaceholders;
        if (placeholders.isEmpty()) return usedPlaceholders;

        Matcher matcher = PLUGIN_PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String match = matcher.group();

            // If there is no provided value for used placeholder
            PluginPlaceholder placeholder = new PluginPlaceholder(match);
            Object value = placeholders.get(placeholder.name());
            if (value == null) continue;

            placeholder.setValue(value);
            usedPlaceholders.add(placeholder);
        }

        return usedPlaceholders;
    }

    private static class PluginPlaceholder {
        private final String exactMatch;
        private final String name;
        private final Set<PlaceholderFlag> flags = new HashSet<>();
        private String value = null;

        public PluginPlaceholder(String placeholder) {
            this.exactMatch = placeholder;

            Matcher matcher = PLUGIN_PLACEHOLDER_FLAGS_PATTERN.matcher(placeholder);
            if (!matcher.find()) {
                name = placeholder.substring(1, placeholder.length() - 1);
                return;
            }

            String match = matcher.group();
            for (PlaceholderFlag flag : PlaceholderFlag.values()) {
                if (match.contains(String.valueOf(flag.flagChar()))) flags.add(flag);
            }

            name = placeholder.substring(1 + match.length(), placeholder.length() - 1);
        }

        public String exactMatch() {
            return exactMatch;
        }
        public String name() {
            return name;
        }
        public Set<PlaceholderFlag> flags() {
            return flags;
        }
        public String value() {
            return value;
        }
        public boolean isFlagNoAPI() {
            return flags.contains(PlaceholderFlag.NO_PAPI);
        }
        public boolean isFlagNoFormatting() {
            return flags.contains(PlaceholderFlag.NO_FORMATTING);
        }
        public boolean isFlagEscapeQuotes() {
            return flags.contains(PlaceholderFlag.ESCAPE_QUOTES);
        }

        public void setValue(Object value) {
            this.value = value.toString();
            if (isFlagEscapeQuotes()) this.value = this.value.replace("\"", "\\\"");
        }

    }

    private enum PlaceholderFlag {
        NO_FORMATTING('!'),
        NO_PAPI('@'),
        ESCAPE_QUOTES('#');

        private final char flagChar;

        PlaceholderFlag(char flagChar) {
            this.flagChar = flagChar;
        }

        public char flagChar() {
            return flagChar;
        }
    }
}
