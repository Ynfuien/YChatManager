package pl.ynfuien.ychatmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.api.events.NicknameChangeEvent;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.modules.DisplaynameModule;
import pl.ynfuien.ychatmanager.storage.Nickname;
import pl.ynfuien.ychatmanager.storage.Storage;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class NickCommand implements CommandExecutor, TabCompleter {
    private final YChatManager instance;
    private final DisplaynameModule displaynameModule;
    private static final String PERMISSION_BASE = "ychatmanager.command.nick";
    private static final String PERMISSION_NICK_OTHERS = PERMISSION_BASE + ".others";
    private static final HashMap<String, TagResolver> TAG_RESOLVERS = ChatFormatter.getTagResolvers(PERMISSION_BASE);
    private static final HashMap<String, ChatColor> LEGACY_FORMATS = ChatFormatter.getLegacyFormats(PERMISSION_BASE);
    private static final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
//    private static final Pattern formatsPattern = Pattern.compile("(&[a-f0-9k-o]|<.+?>)", Pattern.CASE_INSENSITIVE);

    public NickCommand(YChatManager instance) {
        this.instance = instance;
        this.displaynameModule = instance.getModules().getDisplaynameModule();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        if (args.length == 0) {
            if (sender instanceof Player) Lang.Message.COMMAND_NICK_USAGE.send(sender, placeholders);
            if (sender.hasPermission(PERMISSION_BASE + ".others")) Lang.Message.COMMAND_NICK_USAGE_OTHERS.send(sender, placeholders);
            return true;
        }

        // Own nick change
        if (args.length == 1 || !sender.hasPermission(PERMISSION_NICK_OTHERS)) {
            if (!(sender instanceof Player)) {
                Lang.Message.COMMAND_NICK_USAGE_OTHERS.send(sender, placeholders);
                return true;
            }

            Player p = (Player) sender;
            String inputNick = args[0];

            Nickname nick = getNickname(p, p, inputNick);
            if (nick == null) return true;

            Storage.setNick(p.getUniqueId(), nick);
            displaynameModule.updateDisplayname(p);

            placeholders.put("nick", nick.serialized());
            Lang.Message.COMMAND_NICK_SUCCESS.send(sender, placeholders);
            return true;
        }

        // Nick change for other player
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        if (!p.hasPlayedBefore()) {
            placeholders.put("player", args[0]);
            Lang.Message.COMMAND_NICK_FAIL_PLAYER_DOESNT_EXIST.send(sender, placeholders);
            return true;
        }

        String inputNick = args[1];
        Nickname nick = getNickname(sender, p, inputNick);
        if (nick == null) return true;

        NicknameChangeEvent event = new NicknameChangeEvent(p, nick);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;
        nick = event.getNickname();

        Storage.setNick(p.getUniqueId(), nick);
        if (p.isOnline()) displaynameModule.updateDisplayname((Player) p);

        placeholders.put("player", p.getName());
        placeholders.put("nick", nick.serialized());
        Lang.Message.COMMAND_NICK_SUCCESS_OTHER.send(sender, placeholders);
        return true;
    }

    public Nickname getNickname(CommandSender sender, OfflinePlayer p, String input) {
        String nick = parseLegacyFormats(sender, input);

        Component formatted = parseMiniMessageFormats(sender, nick);
        if (nick.contains(ChatFormatter.AMPERSAND_PLACEHOLDER)) formatted = formatted.replaceText(ChatFormatter.AMPERSAND_REPLACEMENT);

        String plainText = PlainTextComponentSerializer.plainText().serialize(formatted);
        HashMap<String, Object> phs = new HashMap<>() {{put("nick", plainText);}};


        if (!plainText.equals(p.getName())) {
            boolean custom = sender.hasPermission(PERMISSION_BASE + ".custom");
            boolean unsafe = sender.hasPermission(PERMISSION_BASE + ".unsafe");

            // If player used formats that he don't have access to
            if ((!unsafe || !custom) && !plainText.equals(PlainTextComponentSerializer.plainText().serialize(ChatFormatter.SERIALIZER.deserialize(plainText, StandardTags.defaults())))) {
                Lang.Message.COMMAND_NICK_FAIL_NOT_PERMITTED.send(sender, phs);
                return null;
            }

            if (!unsafe) {
                if (!plainText.matches(usernamePattern.pattern())) {
                    Lang.Message.COMMAND_NICK_FAIL_UNSAFE.send(sender, phs);
                    return null;
                }

                if (plainText.length() > 16) {
                    Lang.Message.COMMAND_NICK_FAIL_TOO_LONG.send(sender, phs);
                    return null;
                }

                if (plainText.length() < 2) {
                    Lang.Message.COMMAND_NICK_FAIL_TOO_SHORT.send(sender, phs);
                    return null;
                }
            }

            if (!custom) {Lang.Message.COMMAND_NICK_FAIL_ONLY_FORMATS.send(sender, phs);
                return null;
            }
        }

        return new Nickname(MiniMessage.miniMessage().serialize(formatted), input);
    }

    // Checks player's permissions for colors/styles and parses message using those
    private final static Pattern MM_TAG_PATTERN = Pattern.compile("<.+>");
    private static Component parseMiniMessageFormats(CommandSender sender, String text) {
        if (!MM_TAG_PATTERN.matcher(text).find()) return ChatFormatter.SERIALIZER.deserialize(text);

        List<TagResolver> permittedResolvers = new ArrayList<>();
        for (String perm : TAG_RESOLVERS.keySet()) {
            if (sender.hasPermission(perm)) permittedResolvers.add(TAG_RESOLVERS.get(perm));
        }

        return ChatFormatter.SERIALIZER.deserialize(text, TagResolver.resolver(permittedResolvers));
    }

    // Replaces all & with a placeholder, and then back only these that player has permission for
    private static String parseLegacyFormats(CommandSender sender, String text) {
        if (!text.contains("&")) return text;
        text = text.replace("&", ChatFormatter.AMPERSAND_PLACEHOLDER);

        for (String perm : LEGACY_FORMATS.keySet()) {
            if (!sender.hasPermission(perm)) continue;

            char colorChar = LEGACY_FORMATS.get(perm).getChar();
            text = text.replace(ChatFormatter.AMPERSAND_PLACEHOLDER + colorChar, "&" + colorChar);
        }

        return text;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 2) return completions;

        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            if (sender.hasPermission(PERMISSION_NICK_OTHERS)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String name = p.getName();
                    if (name.toLowerCase().startsWith(arg1)) completions.add(name);
                }
            }
            if (!(sender instanceof Player)) return completions;

            Player p = (Player) sender;
            String nick = Storage.getNick(p.getUniqueId()).input();
            if (nick.startsWith(arg1)) completions.add(nick);
            return completions;
        }

        if (!sender.hasPermission(PERMISSION_NICK_OTHERS)) return completions;
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        if (!p.hasPlayedBefore()) return completions;
        String arg2 = args[1].toLowerCase();
        String nick = Storage.getNick(p.getUniqueId()).input();
        if (nick.startsWith(arg2)) completions.add(nick);

        return completions;
    }
}
