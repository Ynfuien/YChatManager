package pl.ynfuien.ychatmanager.commands.subcommands.antiswear;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ychatmanager.Lang;
import pl.ynfuien.ychatmanager.commands.Subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AntiSwearSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ychatmanager.command.main."+name();
    }

    @Override
    public String name() {
        return "anti-swear";
    }

    @Override
    public String description() {
        return Lang.Message.COMMAND_ADMIN_ANTI_SWEAR_DESCRIPTION.get();
    }

    @Override
    public String usage() {
        return null;
    }

    private final Subcommand[] subcommands = new Subcommand[] {
        new WordsSubcommand(this),
        new ExceptionsSubcommand(this)
    };

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        // Loop through and check every subcommand
        String arg1 = args.length > 0 ? args[0].toLowerCase() : "";
        for (Subcommand cmd : subcommands) {
            if (!cmd.name().equals(arg1)) continue;

            if (!sender.hasPermission(cmd.permission())) {
                Lang.Message.COMMANDS_NO_PERMISSION.send(sender, placeholders);
                return;
            }

            String[] argsLeft = Arrays.copyOfRange(args, 1, args.length);
            cmd.run(sender, command, label, argsLeft);
            return;
        }


        Lang.Message.COMMAND_ANTI_SWEAR_USAGE.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 0) return completions;

        // Get commands the sender has permissions for
        List<Subcommand> canUse = Arrays.stream(subcommands).filter(cmd -> sender.hasPermission(cmd.permission())).toList();
        if (canUse.size() == 0) return completions;

        //// Tab completion for subcommands
        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            for (Subcommand cmd : canUse) {
                String name = cmd.name();

                if (name.startsWith(args[0])) {
                    completions.add(name);
                }
            }

            return completions;
        }

        //// Tab completion for subcommand arguments

        // Get provided command in first arg
        Subcommand subcommand = canUse.stream().filter(cmd -> cmd.name().equals(arg1)).findAny().orElse(null);
        if (subcommand == null) return completions;

        // Get completions from provided command and return them if there are any
        List<String> subcommandCompletions = subcommand.getTabCompletions(sender, Arrays.copyOfRange(args, 1, args.length));
        if (subcommandCompletions != null) return subcommandCompletions;

        return completions;
    }
}
