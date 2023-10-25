package pl.ynfuien.ychatmanager.commands.subcommands.antiswear;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.commands.Subcommand;
import pl.ynfuien.ychatmanager.modules.AntiSwearModule;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExceptionsSubcommand implements Subcommand {
    private final Subcommand command;
    private final AntiSwearModule antiSwearModule;

    public ExceptionsSubcommand(Subcommand command) {
        this.command = command;
        this.antiSwearModule = YChatManager.getInstance().getModules().getAntiSwearModule();
    }

    @Override
    public String permission() {
        return String.format("%s.%s", command.permission(), name());
    }

    @Override
    public String name() {
        return "exceptions";
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
        if (arg1.equals("add")) {
            if (args.length < 2) {
                Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_NO_EXCEPTION.send(sender, placeholders);
                return;
            }

            String exception = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            placeholders.put("exception", exception);
            if (!antiSwearModule.addException(exception)) {
                Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_ALREADY_EXISTS.send(sender, placeholders);
                return;
            }
            Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_ADDED.send(sender, placeholders);
            return;
        }

        if (arg1.equals("remove")) {
            if (args.length < 2) {
                Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_NO_EXCEPTION.send(sender, placeholders);
                return;
            }

            String exception = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            placeholders.put("exception", exception);
            if (!antiSwearModule.removeException(exception)) {
                Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_FAIL_DOESNT_EXIST.send(sender, placeholders);
                return;
            }
            Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_REMOVED.send(sender, placeholders);
            return;
        }


        Lang.Message.COMMAND_ANTI_SWEAR_EXCEPTIONS_USAGE.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 0) return completions;
        if (args.length > 2) return completions;

        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            if ("add".startsWith(arg1)) completions.add("add");
            if ("remove".startsWith(arg1)) completions.add("remove");

            return completions;
        }

        String arg2 = args[1].toLowerCase();
        if (arg1.equals("add")) {
            if ("<new exception>".startsWith(arg2)) completions.add("<new exception>");
            return completions;
        }
        if (arg1.equals("remove")) {
            for (String exception : antiSwearModule.getSwearWordExceptions()) {
                if (exception.startsWith(arg2)) completions.add(exception);
            }
            return completions;
        }

        return completions;
    }
}
