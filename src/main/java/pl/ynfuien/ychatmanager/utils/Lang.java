package pl.ynfuien.ychatmanager.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class Lang {
    private static String prefix;
    private static FileConfiguration langConfig;

    public static void loadLang(FileConfiguration langConfig) {
        Lang.langConfig = langConfig;
        prefix = Message.PREFIX.get();
    }

    // Gets message by message enum
    @Nullable
    public static String get(Message message) {
        return get(message.getName());
    }
    // Gets message by path
    @Nullable
    public static String get(String path) {
        return langConfig.getString(path);
    }
    // Gets message by path and replaces placeholders
    @Nullable
    public static String get(String path, HashMap<String, Object> placeholders) {
        placeholders.put("prefix", prefix);
        // Return message with used placeholders
        return Messenger.parsePluginPlaceholders(langConfig.getString(path), placeholders);
    }

    public static void sendMessage(CommandSender sender, Message message) {
        sendMessage(sender, message.getName());
    }
    public static void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, new HashMap<>());
    }
    public static void sendMessage(CommandSender sender, String path, HashMap<String, Object> placeholders) {
        List<String> messages;

        if (langConfig.isList(path)) {
            messages = langConfig.getStringList(path);
        } else {
            messages = List.of(langConfig.getString(path));
            if (messages.get(0) == null) {
                Logger.logError(String.format("There is no message '%s'!", path));
                return;
            }
        }

        for (String message : messages) {
            // Return if message is empty
            if (message.isEmpty()) continue;

            // Get message with used placeholders
            placeholders.put("prefix", prefix);
//            message = MessengerOld.replacePlaceholders(message, placeholders);

            Messenger.send(sender, message, placeholders);
        }
    }

    // Messages enum
    public enum Message {
        PREFIX,
        PLUGIN_IS_RELOADING,
        COMMANDS_INCORRECT,
        COMMANDS_NO_PERMISSION,
        HELP_NO_COMMANDS,
        HELP_TOP,
        HELP_COMMAND_TEMPLATE,
        COMMAND_ADMIN_ANTI_SWEAR_DESCRIPTION,
        COMMAND_ADMIN_RELOAD_DESCRIPTION,
        COMMAND_ADMIN_HELP_DESCRIPTION,
        COMMAND_ADMIN_VERSION_DESCRIPTION,
        COMMAND_MAIN_RELOAD_FAIL,
        COMMAND_MAIN_RELOAD_SUCCESS,
        COMMAND_MAIN_VERSION,
        COMMAND_ANTI_SWEAR_USAGE,
        COMMAND_ANTI_SWEAR_WORDS_USAGE,
        COMMAND_ANTI_SWEAR_WORDS_FAIL_NO_WORD,
        COMMAND_ANTI_SWEAR_WORDS_FAIL_ALREADY_EXISTS,
        COMMAND_ANTI_SWEAR_WORDS_FAIL_DOESNT_EXIST,
        COMMAND_ANTI_SWEAR_WORDS_ADDED,
        COMMAND_ANTI_SWEAR_WORDS_REMOVED,
        COMMAND_ANTI_SWEAR_WORDS_GOT,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_USAGE,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_NO_EXCEPTION,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_ALREADY_EXISTS,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_DOESNT_EXIST,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_ADDED,
        COMMAND_ANTI_SWEAR_EXCEPTIONS_REMOVED,
        COMMAND_MESSAGE_USAGE,
        COMMAND_MESSAGE_FAIL_PLAYER_ISNT_ONLINE,
        COMMAND_REPLY_USAGE,
        COMMAND_REPLY_FAIL_PLAYER_ISNT_ONLINE,
        COMMAND_REPLY_FAIL_NO_RECEIVER,
        PRIVATE_MESSAGE_SENT,
        PRIVATE_MESSAGE_RECEIVED,
        PRIVATE_MESSAGE_SOCIALSPY,
        COMMAND_NICK_USAGE,
        COMMAND_NICK_USAGE_OTHERS,
        COMMAND_NICK_FAIL_NOT_PERMITTED,
        COMMAND_NICK_FAIL_ONLY_FORMATS,
        COMMAND_NICK_FAIL_UNSAFE,
        COMMAND_NICK_FAIL_BLANK,
        COMMAND_NICK_FAIL_TOO_SHORT,
        COMMAND_NICK_FAIL_TOO_LONG,
        COMMAND_NICK_SUCCESS,
        COMMAND_NICK_FAIL_PLAYER_DOESNT_EXIST,
        COMMAND_NICK_SUCCESS_OTHER,
        COMMAND_CLEARCHAT_PREFIX,
        COMMAND_CLEARCHAT_INFO_ADMINS,
        COMMAND_CLEARCHAT_INFO_ADMINS_SENDER,
        COMMAND_CLEARCHAT_INFO_ADMINS_OTHER_ADMIN,
        COMMAND_CLEARCHAT_INFO_PLAYERS,
        COMMAND_SOCIALSPY_USAGE,
        COMMAND_SOCIALSPY_USAGE_OTHERS,
        COMMAND_SOCIALSPY_FAIL_PLAYER_ISNT_ONLINE,
        COMMAND_SOCIALSPY_SUCCESS_ENABLE,
        COMMAND_SOCIALSPY_SUCCESS_DISABLE,
        COMMAND_SOCIALSPY_SUCCESS_OTHER_ENABLE,
        COMMAND_SOCIALSPY_SUCCESS_OTHER_DISABLE,
        COMMAND_COOLDOWN,
        CHAT_ANTI_SWEAR_ALERT,
        CHAT_ANTI_SWEAR_WARNING,
        CHAT_COOLDOWN,
        CHAT_INCORRECT_MESSAGE;

        // Gets message name
        public String getName() {
            return name().toLowerCase().replace('_', '-');
        }

        // Gets message
        public String get() {
            return Lang.get(getName());
        }
        // Gets message with replaced placeholders
        public String get(HashMap<String, Object> placeholders) {
            return Lang.get(getName(), placeholders);
        }

        // Sends message
        public void send(CommandSender sender) {
            Lang.sendMessage(sender, getName());
        }
        // Sends message with replaced placeholders
        public void send(CommandSender sender, HashMap<String, Object> placeholders) {
            Lang.sendMessage(sender, getName(), placeholders);
        }
    }
}
