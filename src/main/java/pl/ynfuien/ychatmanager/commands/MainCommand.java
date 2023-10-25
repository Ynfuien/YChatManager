package pl.ynfuien.ychatmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.commands.subcommands.HelpSubcommand;
import pl.ynfuien.ychatmanager.commands.subcommands.ReloadSubcommand;
import pl.ynfuien.ychatmanager.commands.subcommands.VersionSubcommand;
import pl.ynfuien.ychatmanager.commands.subcommands.antiswear.AntiSwearSubcommand;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    private static final HelpSubcommand helpCommand = new HelpSubcommand();
    public static Subcommand[] subcommands = {
            new AntiSwearSubcommand(),
            helpCommand,
            new ReloadSubcommand(),
            new VersionSubcommand()
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Return if plugin is reloading
        if (YChatManager.getInstance().isReloading()) {
            Lang.Message.PLUGIN_IS_RELOADING.send(sender);
            return true;
        }

        // Run help subcommand if none is provided
        if (args.length == 0) {
            helpCommand.run(sender, command, label, args);
            return true;
        }

        HashMap<String, Object> placeholders = new HashMap<>() {{put("command", label);}};

        // Loop through and check every subcommand
        String arg1 = args[0].toLowerCase();
        for (Subcommand cmd : subcommands) {
            if (!cmd.name().equals(arg1)) continue;

            if (!sender.hasPermission(cmd.permission())) {
                Lang.Message.COMMANDS_NO_PERMISSION.send(sender, placeholders);
                return true;
            }

            String[] argsLeft = Arrays.copyOfRange(args, 1, args.length);
            cmd.run(sender, command, label, argsLeft);
            return true;
        }

        Lang.Message.COMMANDS_INCORRECT.send(sender, placeholders);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (YChatManager.getInstance().isReloading()) return completions;
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
