package pl.ynfuien.ychatmanager.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Index;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.hooks.Hooks;
import pl.ynfuien.ychatmanager.hooks.vault.VaultHook;
import pl.ynfuien.ychatmanager.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public class ChatFormatter {
    public static final MiniMessage SERIALIZER = MiniMessage.builder()
            .postProcessor(new LegacyPostProcessor())
            .build();

    private static final String PERMISSION_BASE = "ychatmanager.chat";


    // Tag resolvers by permission
    private static final HashMap<String, TagResolver> TAG_RESOLVERS = getTagResolvers(PERMISSION_BASE);

    // Gets tag resolvers with provided permission base
    public static HashMap<String, TagResolver> getTagResolvers(String permissionBase) {
        HashMap<String, TagResolver> tagResolvers = new HashMap<>() {{
            String pb = permissionBase;

            put(pb + ".color.hex", HexColorTagResolver.get());
            put(pb + ".nbt", StandardTags.nbt());
            put(pb + ".click", StandardTags.clickEvent());
            put(pb + ".font", StandardTags.font());
            put(pb + ".gradient", StandardTags.gradient());
            put(pb + ".hover", StandardTags.hoverEvent());
            put(pb + ".insertion", StandardTags.insertion());
            put(pb + ".keybind", StandardTags.keybind());
            put(pb + ".newline", StandardTags.newline());
            put(pb + ".rainbow", StandardTags.rainbow());
            put(pb + ".reset", StandardTags.reset());
            put(pb + ".score", StandardTags.score());
            put(pb + ".selector", StandardTags.selector());
            put(pb + ".transition", StandardTags.transition());
            put(pb + ".translatable", StandardTags.translatable());
        }};

        Index<String, NamedTextColor> colorsIndex = NamedTextColor.NAMES;
        for (String colorName : colorsIndex.keys()) {
            tagResolvers.put(String.format("%s.color.%s", permissionBase, colorName), SingleColorTagResolver.of(colorsIndex.value(colorName)));
        }

        for (TextDecoration decoration : TextDecoration.values()) {
            tagResolvers.put(String.format("%s.decoration.%s", permissionBase, decoration.name()), StandardTags.decorations(decoration));
        }

        return tagResolvers;
    }


    // Formats provided chat message sent by provided player using provided chat template
    public static Component format(String chatTemplate, Player p, Component originalMessage, boolean playerFormats) {
        // Placeholders to be used in chat template
        HashMap<String, Object> phs = createPlayerPlaceholders(p);

        Component formattedMessage = originalMessage;
        // Parses player's message
        if (playerFormats) {
            formattedMessage = parsePlayerMessage(p, originalMessage);
            if (PlainTextComponentSerializer.plainText().serialize(formattedMessage).isBlank()) return null;
        }

        // Replaces template and PAPI placeholders
        chatTemplate = parseTemplatePlaceholders(chatTemplate, phs);
        chatTemplate = parsePAPI(p, chatTemplate);
        chatTemplate = chatTemplate.replace('§', '&');

        // Formats template with MiniMessage
        Component formattedTemplate = SERIALIZER.deserialize(chatTemplate);

        // Returns template with {message} placeholder replaced
        return formattedTemplate.replaceText(TextReplacementConfig
                .builder()
                .matchLiteral("{message}")
                .replacement(formattedMessage)
                .build());
    }

    // Parses {some-name} placeholders used in templates
    public static String parseTemplatePlaceholders(String template, HashMap<String, Object> placeholders) {
        for (String placeholder : placeholders.keySet()) {
            String value = String.valueOf(placeholders.get(placeholder));
            template = template.replace(String.format("{%s}", placeholder), value);
        }

        return template;
    }

    // Parses PAPI placeholders in provided component
    public static Component parsePAPI(Player p, Component messageComponent, String message) {
        if (!Hooks.isPapiEnabled()) return messageComponent;
        if (!PlaceholderAPI.containsPlaceholders(message)) return messageComponent;

        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
        while (matcher.find()) {
            String placeholder = matcher.group(0);

            String parsedPlaceholder = PlaceholderAPI.setPlaceholders(p, placeholder);
            if (parsedPlaceholder.equals(placeholder)) continue;

            Component formattedPlaceholder = SERIALIZER.deserialize(parsedPlaceholder.replace('§', '&'));

            TextReplacementConfig replacementConfig = TextReplacementConfig
                    .builder()
                    .matchLiteral(placeholder)
                    .replacement(formattedPlaceholder)
                    .build();
            messageComponent = messageComponent.replaceText(replacementConfig);
        }

        return messageComponent;
    }

    // Parses PAPI placeholders in provided string
    public static String parsePAPI(Player p, String message) {
        if (!Hooks.isPapiEnabled()) return message;
        if (!PlaceholderAPI.containsPlaceholders(message)) return message;

        return PlaceholderAPI.setPlaceholders(p, message);
    }

    // Parses player's message with legacy, MiniMessage and PAPI formats
    private static Component parsePlayerMessage(Player p, Component message) {
        String msg = PlainTextComponentSerializer.plainText().serialize(message);

        Component formatted = parseFormats(p, msg);
        if (p.hasPermission(PERMISSION_BASE + ".papi")) formatted = parsePAPI(p, formatted, msg);

        return formatted;
    }

    // Checks player's permissions for colors/styles and parses message using those
    private static final Pattern MM_TAG_PATTERN = Pattern.compile("<.+>");
    private static Component parseFormats(Player p, String message) {
        MiniMessage serializer = MiniMessage.builder()
                .postProcessor(new LegacyPostProcessor(p, PERMISSION_BASE))
                .tags(TagResolver.empty())
                .build();

        if (!MM_TAG_PATTERN.matcher(message).find()) return serializer.deserialize(message);

        List<TagResolver> permittedResolvers = new ArrayList<>();
        for (String perm : TAG_RESOLVERS.keySet()) {
            if (p.hasPermission(perm)) permittedResolvers.add(TAG_RESOLVERS.get(perm));
        }

        return serializer.deserialize(message, TagResolver.resolver(permittedResolvers));
    }


    public static HashMap<String, Object> createPlayerPlaceholders(CommandSender sender) {
        return createPlayerPlaceholders(sender, null);
    }
    public static HashMap<String, Object> createPlayerPlaceholders(CommandSender sender, String placeholderPrefix) {
        return new HashMap<>() {{
            String pp = placeholderPrefix != null ? placeholderPrefix + "-" : "";

            Player p = sender instanceof Player ? (Player) sender : null;
            String name = sender.getName();
            put(pp+"nick", p != null ? Storage.getNick(p.getUniqueId()).serialized() : name);
            put(pp+"uuid", p != null ? p.getUniqueId() : name);
            put(pp+"username", name);
            put(pp+"displayname", p != null ? MiniMessage.miniMessage().serialize(p.displayName()) : name);

            if (p != null && VaultHook.isEnabled()) {
                Chat chat = VaultHook.getChat();
                put(pp+"prefix", chat.getPlayerPrefix(p));
                put(pp+"suffix", chat.getPlayerSuffix(p));
                put(pp+"group", chat.getPrimaryGroup(p));
            }
        }};
    }
}
