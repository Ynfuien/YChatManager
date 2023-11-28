package pl.ynfuien.ychatmanager.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.hooks.vault.VaultHook;
import pl.ynfuien.ychatmanager.modules.ChatModule;
import pl.ynfuien.ychatmanager.storage.Storage;

import java.util.HashMap;
import java.util.HashSet;

// Inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public class ChatFormatter {
    private static final String PERMISSION_BASE = "ychatmanager.chat";
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final ColorFormatter colorFormatter = new ColorFormatter(PERMISSION_BASE + ".formats", new HashSet<>());
    private final ChatModule chatModule;

    public ChatFormatter(ChatModule chatModule) {
        this.chatModule = chatModule;
    }


    // Formats provided chat message sent by provided player using provided chat template
    public Component format(Player p, Component originalMessage) {
        // Placeholders to be used in chat template
        HashMap<String, Object> phs = createPlayerPlaceholders(p);

        Component formattedMessage = originalMessage;
        // Parse player's message
        if (chatModule.isPlayerFormats()) {
            String plainText = PLAIN_TEXT_SERIALIZER.serialize(originalMessage);
            formattedMessage = colorFormatter.format(p, plainText);

            if (PLAIN_TEXT_SERIALIZER.serialize(formattedMessage).isBlank()) return null;
        }

        // Replace template and PAPI placeholders
        String chatTemplate = parseTemplatePlaceholders(chatModule.getFormattingFormat(), phs);
        chatTemplate = ColorFormatter.parsePAPI(p, chatTemplate);

        // Format template with MiniMessage
        Component formattedTemplate = ColorFormatter.SERIALIZER.deserialize(chatTemplate);

        // Return template with {message} placeholder replaced
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



    public static HashMap<String, Object> createPlayerPlaceholders(CommandSender sender) {
        return createPlayerPlaceholders(sender, null, true);
    }
    public static HashMap<String, Object> createPlayerPlaceholders(CommandSender sender, String placeholderPrefix) {
        return createPlayerPlaceholders(sender, placeholderPrefix, true);
    }
    public static HashMap<String, Object> createPlayerPlaceholders(CommandSender sender, String placeholderPrefix, boolean useDisplayName) {
        return new HashMap<>() {{
            String pp = placeholderPrefix != null ? placeholderPrefix + "-" : "";

            Player p = sender instanceof Player ? (Player) sender : null;
            String name = sender.getName();
            put(pp+"nick", p != null ? Storage.getNick(p.getUniqueId()).serialized() : name);
            put(pp+"uuid", p != null ? p.getUniqueId() : name);
            put(pp+"username", name);
            put(pp+"displayname", p != null ? MiniMessage.miniMessage().serialize(p.displayName()) : name);

            put("prefix", "");
            put("suffix", "");
            put("group", "");
            if (p != null && VaultHook.isChat()) {
                Chat chat = VaultHook.getChat();
                put(pp+"prefix", chat.getPlayerPrefix(p));
                put(pp+"suffix", chat.getPlayerSuffix(p));
                put(pp+"group", chat.getPrimaryGroup(p));
            }
        }};
    }
}
