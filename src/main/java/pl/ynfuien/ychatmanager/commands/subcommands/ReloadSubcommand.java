package pl.ynfuien.ychatmanager.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.Lang;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.commands.Subcommand;

import java.util.ArrayList;
import java.util.List;

public class ReloadSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "ychatmanager.command.main."+name();
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return Lang.Message.COMMAND_ADMIN_RELOAD_DESCRIPTION.get();
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        // Reload plugin
        boolean success = YChatManager.getInstance().reloadPlugin();

        // Check if reload was success
        if (success) {
            // Send success message to console if sender is player
            if (sender instanceof Player) {
                Lang.Message.COMMAND_MAIN_RELOAD_SUCCESS.send(Bukkit.getConsoleSender());
            }
            // Send success message to sender
            Lang.Message.COMMAND_MAIN_RELOAD_SUCCESS.send(sender);
            return;
        }

        // Send fail message to console if sender is player
        if (sender instanceof Player) {
            Lang.Message.COMMAND_MAIN_RELOAD_FAIL.send(Bukkit.getConsoleSender());
        }
        // Send fail message to sender
        Lang.Message.COMMAND_MAIN_RELOAD_FAIL.send(sender);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
