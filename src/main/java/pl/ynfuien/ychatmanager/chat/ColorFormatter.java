package pl.ynfuien.ychatmanager.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.Index;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.hooks.Hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public class ColorFormatter {
    /**
     * MiniMessage serializer with support for the legacy format.
     */
    public static final MiniMessage SERIALIZER = MiniMessage.builder()
            .postProcessor(new LegacyPostProcessor())
            .build();

    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private final static boolean PAPI_ENABLED = Hooks.isPluginEnabled(Hooks.Plugin.PAPI);

    // Tag resolvers by permission
    private final HashMap<String, TagResolver> tagResolvers;


    private final String permissionBase;
    public ColorFormatter(String permissionBase, Set<YTagResolver> excluded) {
        this.permissionBase = permissionBase;

        tagResolvers = createTagResolverMapByPermission(permissionBase, excluded);
    }
    public ColorFormatter(String permissionBase) {
        this.permissionBase = permissionBase;

        tagResolvers = createTagResolverMapByPermission(permissionBase,
                Set.of(YTagResolver.CLICK,
                YTagResolver.HOVER,
                YTagResolver.INSERTION,
                YTagResolver.NEWLINE,
                YTagResolver.SCORE)
        );
    }


    // Gets tag resolvers with provided permission base
    public static HashMap<String, TagResolver> createTagResolverMapByPermission(String permissionBase, Set<YTagResolver> excludedResolvers) {
        HashMap<String, TagResolver> tagResolvers = new HashMap<>();

        for (YTagResolver yResolver : YTagResolver.values()) {
            if (excludedResolvers.contains(yResolver)) continue;
            tagResolvers.put(yResolver.getPermission(permissionBase), yResolver.getResolver());
        }


        Index<String, NamedTextColor> colorsIndex = NamedTextColor.NAMES;
        for (String colorName : colorsIndex.keys()) {
            tagResolvers.put(String.format("%s.color.%s", permissionBase, colorName), SingleColorTagResolver.of(colorsIndex.value(colorName)));
        }

        for (TextDecoration decoration : TextDecoration.values()) {
            tagResolvers.put(String.format("%s.decoration.%s", permissionBase, decoration.name()), StandardTags.decorations(decoration));
        }

        return tagResolvers;
    }


    // Parses player's text with legacy, MiniMessage and PAPI formats
    public Component format(CommandSender p, String text) {
        return format(p, text, true);
    }

    // Parses player's text with legacy, MiniMessage and PAPI formats
    public Component format(CommandSender p, String text, boolean usePapi) {
        if (usePapi && p.hasPermission(permissionBase + ".papi")) text = parsePAPI(p, text);
        return parseFormats(p, text);
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

    // Checks player's permissions for colors/styles and parses message using those
    private static final Pattern MM_TAG_PATTERN = Pattern.compile("<.+>");
    private Component parseFormats(CommandSender p, String message) {
        MiniMessage serializer = MiniMessage.builder()
                .postProcessor(new LegacyPostProcessor(p, permissionBase))
                .tags(TagResolver.empty())
                .build();

        if (!MM_TAG_PATTERN.matcher(message).find()) return serializer.deserialize(message);

        List<TagResolver> permittedResolvers = new ArrayList<>();
        for (String perm : tagResolvers.keySet()) {
            if (p.hasPermission(perm)) permittedResolvers.add(tagResolvers.get(perm));
        }

        return serializer.deserialize(message, TagResolver.resolver(permittedResolvers));
    }

    public enum YTagResolver {
        COLOR_HEX(HexColorTagResolver.get()),
        NBT(StandardTags.nbt()),
        CLICK(StandardTags.clickEvent()),
        FONT(StandardTags.font()),
        GRADIENT(StandardTags.gradient()),
        HOVER(StandardTags.hoverEvent()),
        INSERTION(StandardTags.insertion()),
        KEYBIND(StandardTags.keybind()),
        NEWLINE(StandardTags.newline()),
        RAINBOW(StandardTags.rainbow()),
        RESET(StandardTags.reset()),
        SCORE(StandardTags.score()),
        SELECTOR(StandardTags.selector()),
        TRANSITION(StandardTags.transition()),
        TRANSLATABLE(StandardTags.translatable());

        private final TagResolver resolver;
        YTagResolver(TagResolver resolver) {
            this.resolver = resolver;
        }

        public TagResolver getResolver() {
            return resolver;
        }

        public String getName() {
            return name().toLowerCase().replace('_', '.');
        }

        public String getPermission(String base) {
            return base + "." + getName();
        }
    }
}
