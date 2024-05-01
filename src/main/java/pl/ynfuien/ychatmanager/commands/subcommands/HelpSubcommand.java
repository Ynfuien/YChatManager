package pl.ynfuien.ychatmanager.commands.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ychatmanager.commands.Subcommand;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.*;

import static pl.ynfuien.ychatmanager.commands.MainCommand.subcommands;

public class HelpSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ychatmanager.command.main";
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return Lang.Message.COMMAND_ADMIN_HELP_DESCRIPTION.get();
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{put("command", label);}};

        // Send top message
        Lang.Message.HELP_TOP.send(sender, placeholders);

        // Get available commands for the sender
        Subcommand[] available = Arrays.stream(subcommands).filter(s -> sender.hasPermission(s.permission())).toArray(Subcommand[]::new);
        if (available.length == 0) {
            Lang.Message.HELP_NO_COMMANDS.send(sender, placeholders);
            return;
        }

        Lang.Message template = Lang.Message.HELP_COMMAND_TEMPLATE;
        // Get the shortest command alias
        String cmdName = command.getName();
        if (!command.getAliases().isEmpty()) {
            String alias = command.getAliases().stream().min(Comparator.comparing(String::length)).get();
            if (alias.length() < cmdName.length()) cmdName = alias;
        }
        String finalCmdName = cmdName;

        // Send help message for every command
        for (Subcommand subcommand : available) {
            if (!sender.hasPermission(subcommand.permission())) continue;

            String subCmdName = subcommand.name();
            String description = subcommand.description();
            String usage = subcommand.usage();

            template.send(sender, new HashMap<>() {{
                put("command", String.format("%s %s%s", finalCmdName, subCmdName, (usage != null ? " "+usage : "")));
                put("description", description);
            }});
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
