package pl.ynfuien.ychatmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand {
    String permission();

    String name();

    String description();

    String usage();

    void run(CommandSender sender, Command command, String label, String[] args);

    List<String> getTabCompletions(CommandSender sender, String[] args);
}
