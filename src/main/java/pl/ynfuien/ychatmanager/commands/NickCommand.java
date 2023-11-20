package pl.ynfuien.ychatmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.api.event.NicknameChangeEvent;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.chat.LegacyPostProcessor;
import pl.ynfuien.ychatmanager.modules.DisplaynameModule;
import pl.ynfuien.ychatmanager.storage.Nickname;
import pl.ynfuien.ychatmanager.storage.Storage;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class NickCommand implements CommandExecutor, TabCompleter {
    private final YChatManager instance;
    private final DisplaynameModule displaynameModule;
    private static final String PERMISSION_BASE = "ychatmanager.command.nick";
    private static final String PERMISSION_NICK_OTHERS = PERMISSION_BASE + ".others";
    private static final HashMap<String, TagResolver> TAG_RESOLVERS = ChatFormatter.getTagResolvers(PERMISSION_BASE);
    private static final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");

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
        Component formatted = parseFormats(sender, input);
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
                if (plainText.isBlank()) {
                    Lang.Message.COMMAND_NICK_FAIL_BLANK.send(sender, phs);
                    return null;
                }

                if (!plainText.matches(usernamePattern.pattern())) {
                    Lang.Message.COMMAND_NICK_FAIL_UNSAFE.send(sender, phs);
                    return null;
                }

                ConfigurationSection config = instance.getConfig().getConfigurationSection("commands.nickname");
                int maxLength = config.getInt("max-length");
                phs.put("max-length", maxLength);
                if (plainText.length() > maxLength) {
                    Lang.Message.COMMAND_NICK_FAIL_TOO_LONG.send(sender, phs);
                    return null;
                }

                int minLength = config.getInt("min-length");
                phs.put("min-length", minLength);
                if (plainText.length() < minLength) {
                    Lang.Message.COMMAND_NICK_FAIL_TOO_SHORT.send(sender, phs);
                    return null;
                }
            }

            if (!custom) {
                Lang.Message.COMMAND_NICK_FAIL_ONLY_FORMATS.send(sender, phs);
                return null;
            }
        }

        return new Nickname(MiniMessage.miniMessage().serialize(formatted), input);
    }

    // Checks player's permissions for colors/styles and parses message using those
    private final static Pattern MM_TAG_PATTERN = Pattern.compile("<.+>");
    private static Component parseFormats(CommandSender sender, String text) {
        MiniMessage serializer = MiniMessage.builder()
                .postProcessor(new LegacyPostProcessor(sender, PERMISSION_BASE))
                .tags(TagResolver.empty())
                .build();

        if (!MM_TAG_PATTERN.matcher(text).find()) return serializer.deserialize(text);

        List<TagResolver> permittedResolvers = new ArrayList<>();
        for (String perm : TAG_RESOLVERS.keySet()) {
            if (sender.hasPermission(perm)) permittedResolvers.add(TAG_RESOLVERS.get(perm));
        }

        return serializer.deserialize(text, TagResolver.resolver(permittedResolvers));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 2) return completions;

        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            if (sender.hasPermission(PERMISSION_NICK_OTHERS)) {
                Player player = null;
                if (sender instanceof Player) player = (Player) sender;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (player != null && !player.canSee(p)) continue;

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
