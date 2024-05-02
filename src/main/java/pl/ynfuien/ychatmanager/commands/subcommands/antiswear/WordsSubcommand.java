package pl.ynfuien.ychatmanager.commands.subcommands.antiswear;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.commands.Subcommand;
import pl.ynfuien.ychatmanager.modules.AntiSwearModule;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class WordsSubcommand implements Subcommand {
    private final Subcommand command;
    private final AntiSwearModule antiSwearModule;

    public WordsSubcommand(Subcommand command) {
        this.command = command;
        this.antiSwearModule = YChatManager.getInstance().getModules().getAntiSwearModule();
    }

    @Override
    public String permission() {
        return String.format("%s.%s", command.permission(), name());
    }

    @Override
    public String name() {
        return "words";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        String arg1 = args.length > 0 ? args[0].toLowerCase() : "";
        placeholders.put("argument", arg1);
        if (arg1.equals("add")) {
            if (args.length < 2) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_NO_WORD.send(sender, placeholders);
                return;
            }

            String word = args[1].toLowerCase();
            placeholders.put("word", word);
            if (!antiSwearModule.addSwear(word)) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_ALREADY_EXISTS.send(sender, placeholders);
                return;
            }
            Lang.Message.COMMAND_ANTI_SWEAR_WORDS_ADDED.send(sender, placeholders);
            return;
        }

        if (arg1.equals("remove")) {
            if (args.length < 2) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_NO_WORD.send(sender, placeholders);
                return;
            }

            String word = args[1].toLowerCase();
            placeholders.put("word", word);
            if (!antiSwearModule.removeSwear(word)) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_DOESNT_EXIST.send(sender, placeholders);
                return;
            }
            Lang.Message.COMMAND_ANTI_SWEAR_WORDS_REMOVED.send(sender, placeholders);
            return;
        }

        if (arg1.equals("get")) {
            if (args.length < 2) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_NO_WORD.send(sender, placeholders);
                return;
            }

            String word = args[1].toLowerCase();
            placeholders.put("word", word);

            Pattern pattern = antiSwearModule.getSwearWords().get(word);
            if (pattern == null) {
                Lang.Message.COMMAND_ANTI_SWEAR_WORDS_FAIL_DOESNT_EXIST.send(sender, placeholders);
                return;
            }

            String regexReplacements = antiSwearModule.getSwearWordsWithReplacement().get(word).pattern();
            placeholders.put("pattern-normal", MiniMessage.miniMessage().escapeTags(pattern.pattern()));
            placeholders.put("pattern-replacements", MiniMessage.miniMessage().escapeTags(regexReplacements));
            Lang.Message.COMMAND_ANTI_SWEAR_WORDS_GOT.send(sender, placeholders);
            return;
        }


        Lang.Message.COMMAND_ANTI_SWEAR_WORDS_USAGE.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 0) return completions;
        if (args.length > 2) return completions;

        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            for (String subcommand : new String[] {"add", "get", "remove"}) {
                if (subcommand.startsWith(arg1)) completions.add(subcommand);
            }

            return completions;
        }

        String arg2 = args[1].toLowerCase();
        if (arg1.equals("add")) {
            if ("<new word>".startsWith(arg2)) completions.add("<new word>");
            return completions;
        }
        if (arg1.equals("remove") || arg1.equals("get")) {
            for (String word : antiSwearModule.getSwearWords().keySet()) {
                if (word.startsWith(arg2)) completions.add(word);
            }
            return completions;
        }

        return completions;
    }
}
