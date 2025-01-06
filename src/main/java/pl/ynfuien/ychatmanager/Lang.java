package pl.ynfuien.ychatmanager;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ydevlib.messages.LangBase;
import pl.ynfuien.ydevlib.messages.Messenger;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;

import java.util.HashMap;

public class Lang extends LangBase {
    public enum Message implements LangBase.Message {
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
        CHAT_INCORRECT_MESSAGE,
        ;

        /**
         * Gets name/path of this message.
         */
        @Override
        public String getName() {
            return name().toLowerCase().replace('_', '-');
        }

        /**
         * Gets original unformatted message.
         */
        public String get() {
            return Lang.get(getName());
        }

        /**
         * Gets message with parsed:
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public String get(HashMap<String, Object> placeholders) {
            return Lang.get(getName(), placeholders);
        }

        /**
         * Gets message with parsed:
         * - PlaceholderAPI
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public String get(CommandSender sender, HashMap<String, Object> placeholders) {
            return ColorFormatter.parsePAPI(sender, Lang.get(getName(), placeholders));
        }

        /**
         * Gets message as component with parsed:
         * - MiniMessage
         * - PlaceholderAPI
         * - {prefix} placeholder
         * - additional provided placeholders
         */
        public Component getComponent(CommandSender sender, HashMap<String, Object> placeholders) {
            return Messenger.parseMessage(sender, Lang.get(getName()), placeholders);
        }

        /**
         * Sends this message to provided sender.<br/>
         * Parses:<br/>
         * - MiniMessage<br/>
         * - PlaceholderAPI<br/>
         * - {prefix} placeholder
         */
        public void send(CommandSender sender) {
            this.send(sender, new HashMap<>());
        }

        /**
         * Sends this message to provided sender.<br/>
         * Parses:<br/>
         * - MiniMessage<br/>
         * - PlaceholderAPI<br/>
         * - {prefix} placeholder<br/>
         * - additional provided placeholders
         */
        public void send(CommandSender sender, HashMap<String, Object> placeholders) {
            Lang.sendMessage(sender, this, placeholders);
        }
    }
}
