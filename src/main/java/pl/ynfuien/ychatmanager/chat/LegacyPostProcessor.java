package pl.ynfuien.ychatmanager.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parses legacy formats to correct component colors/styles, according to player's permissions.
// Strong inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public final class LegacyPostProcessor implements UnaryOperator<Component> {
    private Permissible player = null;
    private String permissionBase = null;

    // Legacy formats with according permissions
    public static final HashMap<ChatColor, String> LEGACY_FORMATS = new HashMap<>();
    static {
        for (ChatColor color : ChatColor.values()) {
            LEGACY_FORMATS.put(color, ".legacy." + color.name().toLowerCase());
        }
    }

    // Constructors
    public LegacyPostProcessor(Permissible player, String permissionBase) {
        this.player = player;
        this.permissionBase = permissionBase;
    }
    public LegacyPostProcessor() {}

    private final Replacer REPLACER = new Replacer();
    @Override
    public Component apply(Component component) {
        return component.replaceText(REPLACER);
    }

    private final class Replacer implements Consumer<TextReplacementConfig.Builder> {
        private final Replacement REPLACEMENT = new Replacement();
        private static final Pattern PATTERN_ANY = Pattern.compile(".*");

        @Override
        public void accept(TextReplacementConfig.Builder builder) {
            builder.match(PATTERN_ANY).replacement(REPLACEMENT);
        }

    }

    private final class Replacement implements BiFunction<MatchResult, TextComponent.Builder, ComponentLike> {

        @Override
        public ComponentLike apply(MatchResult matchResult, TextComponent.Builder builder) {
            String match = matchResult.group();
            if (!match.contains("&")) return Component.text(match);
            if (player == null) return LegacyComponentSerializer.legacyAmpersand().deserialize(match);

            match = parseLegacyFormats(player, match, permissionBase);
            return LegacyComponentSerializer.legacySection().deserialize(match);
        }
    }

    private static final Pattern LEGACY_PATTERN = Pattern.compile("&[0-9a-fA-Fk-oK-OrR]");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#[0-9a-fA-F]{6}");
    private static final String SECTION_CHAR = "§";
    // Parses & format to §, but only for those colors/formats, that player has permission for
    private static String parseLegacyFormats(Permissible p, String message, String permissionBase) {
        // Hex colors
        if (p.hasPermission(permissionBase + ".legacy.hex")) {
            Matcher matcher = LEGACY_HEX_PATTERN.matcher(message);
            while (matcher.find()) {
                String match = matcher.group();
                message = message.replace(match, SECTION_CHAR + match.substring(1));
            }
        }

        // Other colors and formats
        Matcher matcher = LEGACY_PATTERN.matcher(message);
        while (matcher.find()) {
            String match = matcher.group();

            char colorChar = Character.toLowerCase(match.charAt(1));
            ChatColor color = ChatColor.getByChar(colorChar);
            if (p.hasPermission(permissionBase + LEGACY_FORMATS.get(color))) {
                message = message.replace(match, SECTION_CHAR + colorChar);
            }
        }

        return message;
    }
}
